package ru.grinn.loyalty;

import java.util.HashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.PostConstruct;
import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.goods.*;
import ru.crystals.pos.api.receipt.*;
import ru.crystals.pos.api.ext.loyal.dto.*;
import ru.crystals.pos.api.ext.loyal.dto.auxiliaries.*;
import ru.crystals.pos.api.ui.listener.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.feedback.*;
import ru.crystals.pos.spi.plugin.goods.*;
import ru.crystals.pos.spi.receipt.*;
import ru.grinn.loyalty.dto.AddRubleTransaction;
import ru.grinn.loyalty.dto.RubleTransactionResponse;

@POSPlugin(id = LineGoodsPlugin.PLUGIN_NAME)
public class LineGoodsPlugin extends LinePlugin implements GoodsPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.goodsplugin";

    private HashMap<String,BigDecimal> cardAmountList;

    private APIRequest apiRequest;
    private APIObjectMapper apiObjectMapper;

    @PostConstruct
    void init() {
        cardAmountList = new HashMap<>();
        cardAmountList.put("9999000000015", new BigDecimal(1000));
        cardAmountList.put("9999000000022", new BigDecimal(2000));
        cardAmountList.put("9999000000039", new BigDecimal(3000));
        cardAmountList.put("9999000000053", new BigDecimal(5000));
        cardAmountList.put("9999000000107", new BigDecimal(10000));
        cardAmountList.put("9999000000008", new BigDecimal(1000));
        cardAmountList.put("9999000000114", new BigDecimal(300));

        apiRequest = new APIRequest(properties);
        apiObjectMapper = new APIObjectMapper();

        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    public MerchandiseEntity findByBarcode(String barcode) {
        log.debug("findByBarcode({})", barcode);
        if (cardAmountList.get(barcode) != null) {
            MerchandiseEntity result = new MerchandiseEntity();
            result.setPrice(cardAmountList.get(barcode));
            result.setBarcode(barcode);
            result.setMarking(barcode);
            result.setName(String.format("ПОДАРОЧНАЯ КАРТА %s", result.getPrice()));
            return result;
        }
        return null;
    }

    @Override
    public void addForSale(AddForSaleRequest request) {
        NewLineItem lineItem = new NewLineItem(request.getMerchandise());
        lineItem.setNds(20);
        inputCard(request, lineItem);
    }

    @Override
    public void removeFromSale(RemoveFromSaleRequest request) {
        request.getCallback().completed(true);
    }

    @Override    
    public Feedback eventReceiptFiscalized(Receipt receipt, boolean isCancelReceipt) {
        log.debug("eventReceiptFiscalized({})", receipt.getNumber());
        if (isCancelReceipt)
            return null;

        for (LineItem item: receipt.getLineItems()) {
            if (item.getPluginId().equals(PLUGIN_NAME)) {
                BigDecimal amount = item.getSum().multiply(new BigDecimal(receipt.getType() == ReceiptType.SALE ? 1 : -1));
                AddRubleTransaction transaction = new AddRubleTransaction(item.getData().get("card"), amount, getCassa(), getChekSn(receipt));
                try {
                    log.debug("transaction {}", transaction);
                    RubleTransactionResponse rubleTransactionResponse = apiRequest.addRuble(transaction);
                    log.debug("response {}", rubleTransactionResponse);
                    return null;
                }
                catch (Exception e) {
                    try {
                        LoyProviderFeedback feedback = new LoyProviderFeedback();
                        feedback.setPayload(apiObjectMapper.writeValueAsString(transaction));
                        log.debug("return feedback {}", feedback.getPayload());
                        return feedback;
                    }
                    catch (Exception er) {
                        log.debug("error generate feedback", er);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onRepeatSend(Feedback feedback) throws Exception {
        log.debug("onRepeatSend({}) called, payload {}", feedback, feedback.getPayload());
        AddRubleTransaction transaction = apiObjectMapper.readValue(feedback.getPayload(), AddRubleTransaction.class);
        log.debug("transaction {}", transaction);
        RubleTransactionResponse rubleTransactionResponse = apiRequest.addRuble(transaction);
        log.debug("response {}", rubleTransactionResponse);
    }

    private void inputCard(AddForSaleRequest request, NewLineItem lineItem) {
        ui.getInputForms().showInputScanNumberForm("Зачисление на подарочную карту ЛИНИЯ", "Номер карты", "", 13, new InputScanNumberFormListener() {
            @Override
            public void eventBarcodeScanned(String barcode) {
                setCardNumber(request, lineItem, barcode);
            }
        
            @Override
            public void eventInputComplete(String number) {
                setCardNumber(request, lineItem, number);
            }

            @Override
            public void eventCanceled() {
                tryNotCompleted(request);
            }

        });
    }

    private void setCardNumber(AddForSaleRequest request, NewLineItem lineItem, String number) {
        if (isCardNumber(number)) {
            lineItem.getData().put("card", number);
            inputAmount(request, lineItem);
        }
        else {
            ui.showErrorForm("Ошибка в номере карты!", () -> tryNotCompleted(request));
        }
    }

    private void inputAmount(AddForSaleRequest request, NewLineItem lineItem) {
        if (!lineItem.getBarcode().equals("9999000000008")) {
            addSlips(lineItem);
            tryCompleted(request, lineItem);
            return;
        }
        ui.getInputForms().showInputNumberForm("Зачисление на подарочную карту ЛИНИЯ", "Сумма", "1000", 6, new InputScanNumberFormListener() {
            @Override
            public void eventInputComplete(String number) {
                setAmount(request, lineItem, number);
            }

            @Override
            public void eventBarcodeScanned(String barcode) {
            }

            @Override
            public void eventCanceled() {
                tryNotCompleted(request);
            }
        });
    }

    private void setAmount(AddForSaleRequest request, NewLineItem lineItem, String number) {
        int amount = Integer.parseInt(number);
        int giftCardAmountBy = Integer.parseInt(properties.getServiceProperties().get("giftCardAmountBy"));
        if (amount % giftCardAmountBy == 0) {
            lineItem.setQuantity(amount * 1000L / lineItem.getPrice().intValue());
            addSlips(lineItem);
            tryCompleted(request, lineItem);
        }
        else {
            ui.showErrorForm("Сумма не кратна заданной!", () -> tryNotCompleted(request));
        }
    }

    private void addSlips(NewLineItem lineItem) {
        Slip slip = new Slip();
        BigDecimal amount = lineItem.getPrice().multiply(new BigDecimal(lineItem.getQuantity())).divide(new BigDecimal(1000), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
        slip.getParagraphs().add(new SlipParagraph(SlipParagraphType.TEXT, String.format("Начислено на %s - %s руб.", lineItem.getData().get("card"), amount)));
        lineItem.getSlips().add(slip);
    }

    private void tryCompleted(AddForSaleRequest request, NewLineItem lineItem) {
        try {
            request.getCallback().completed(lineItem, null);
        }
        catch (InvalidLineItemException e) {
            log.debug("tryCompleted error", e);
        }
    }

    private void tryNotCompleted(AddForSaleRequest request) {
        try {
            request.getCallback().notCompleted();
        }
        catch (CancelDeniedException e) {
            log.debug("tryNotCompleted error", e);
        }
    }
}