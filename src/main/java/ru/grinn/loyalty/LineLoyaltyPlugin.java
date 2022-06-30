package ru.grinn.loyalty;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.PostConstruct;
import ru.crystals.pos.api.ext.loyal.dto.*;
import ru.crystals.pos.api.ext.loyal.dto.auxiliaries.*;
import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.receipt.*;
import ru.grinn.loyalty.bonus.CalculationStrategyService;
import ru.grinn.loyalty.dto.AddBonusTransaction;
import ru.grinn.loyalty.dto.BonusTransactionResponse;

@POSPlugin(id = LineLoyaltyPlugin.PLUGIN_NAME)
public class LineLoyaltyPlugin extends LinePlugin implements LoyaltyPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.loyaltyplugin";

    public static final BigDecimal MAX_RATIO_TO_WRITEOFF = new BigDecimal("0.99");
    public static final BigDecimal MIN_SUM_AFTER_WRITEOFF = new BigDecimal("0.01");

    private CalculationStrategyService calculationStrategyService;

    private APIRequest apiRequest;

    @PostConstruct
    void init() {
        super.init();

        calculationStrategyService = new CalculationStrategyService(log);
        apiRequest = new APIRequest(properties);

        log.info("Plugin {} loaded", this.getClass());
        log.info("Версия от 30.06.2022");
    }

    @Override
    public LoyaltyResult doDiscount(Receipt receipt) {
        log.debug("doDiscount start");
        LoyaltyResult result = new LoyaltyResult();
        if (receipt == null)
            return result;

        log.debug("doDiscount({})", receipt.getNumber());

        for (Card card : receipt.getCards()) {
            if (!card.getProcessingId().equals(LineCardPlugin.PLUGIN_NAME)) {
                log.debug("Карта \"{}\" (\"{}\") не этого процессинга.", card.getCardNumber(), card.getProcessingId());
                continue;
            }

            if (pluginConfiguration.isCardDiscountAllowed() && card.getExtendedAttributes().get("discount") != null && new BigDecimal(card.getExtendedAttributes().get("discount")).compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal balance = new BigDecimal(card.getExtendedAttributes().get("balance"));
                BigDecimal discountPercent = new BigDecimal(card.getExtendedAttributes().get("discount")).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                for (LineItem item: receipt.getLineItems()) {
                    if (item.isDiscountable() && item.getMinPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        BigDecimal sumToDiscount = item.getSum().min(balance);
                        BigDecimal discountValue = sumToDiscount.multiply(discountPercent).setScale(2, RoundingMode.HALF_UP);
                        result.getDiscounts().add(new Discount(item.getNumber(), card.getCardNumber(), discountValue, "card-discount"));
                        balance = balance.subtract(sumToDiscount);
                    }
                }
            }
            if (card.getBonusBalance() != null && card.getBonusBalance().getSumToChargeOff() != null && card.getBonusBalance().getSumToChargeOff().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal sumToChargeOff = card.getBonusBalance().getSumToChargeOff();
                for (LineItem item: receipt.getLineItems()) {
                    BigDecimal sumCanCharge = getCanChargeOffItem(item, result.getDiscounts()).min(sumToChargeOff).setScale(2, RoundingMode.HALF_UP);
                    if (sumCanCharge.compareTo(BigDecimal.ZERO) > 0) {
                        result.getDiscounts().add(new Discount(item.getNumber(), card.getCardNumber(), sumCanCharge, "bonus-discount"));
                        sumToChargeOff = sumToChargeOff.subtract(sumCanCharge).setScale(2, RoundingMode.HALF_UP);
                    }
                }
            }
            else {
                BigDecimal canChargeOffItems = getCanChargeOffItems(receipt.getLineItems(), result.getDiscounts());
                log.debug("canChargeOffItems {}", canChargeOffItems); // stingfire
                BigDecimal cardBalance = card.getBonusBalance().getBalance();
                BigDecimal maxBonusToWriteOff = canChargeOffItems.min(cardBalance);
                if (Integer.parseInt(card.getExtendedAttributes().getOrDefault("ownerFilled", "0")) == 0)
                    maxBonusToWriteOff = BigDecimal.ZERO;
                log.debug("maxBonusToWriteOff {}", maxBonusToWriteOff); // stingfire
                BigDecimal maxBonusToWriteOffCents = maxBonusToWriteOff.subtract(maxBonusToWriteOff.setScale(0, RoundingMode.DOWN));

                BigDecimal chekAmount = getReceiptSumMinusDiscounts(receipt, result.getDiscounts());
                log.debug("chekAmount {}", chekAmount); // stingfire
                BigDecimal chekAmountCents = chekAmount.subtract(chekAmount.setScale(0, RoundingMode.DOWN));

                if (maxBonusToWriteOffCents.compareTo(chekAmountCents) > 0) {
                    maxBonusToWriteOff = maxBonusToWriteOff.setScale(0, RoundingMode.DOWN).add(chekAmountCents);
                }
                else {
                    if (maxBonusToWriteOff.compareTo(BigDecimal.ONE) >= 0) {
                        maxBonusToWriteOff = maxBonusToWriteOff.setScale(0, RoundingMode.DOWN).subtract(BigDecimal.ONE).add(chekAmountCents);
                    }
                    else {
                        maxBonusToWriteOff = BigDecimal.ZERO;
                    }
                }

                log.debug("Сумма, возможная к списанию = {}", maxBonusToWriteOff);
                WriteOffInfo writeOffInfo = new WriteOffInfo(card.getBonusBalance(), maxBonusToWriteOff);
                Collection<WriteOffInfo> writeOffInfos = new HashSet<>();
                writeOffInfos.add(writeOffInfo);
                result.getWriteOffsLimits().put(card.getCardNumber(), writeOffInfos);
            }
            log.debug("Карта {}, баланс бонусов {}, к списанию {}", card.getCardNumber(), card.getBonusBalance().getBalance(), card.getBonusBalance().getSumToChargeOff());
        }
        return result;
    }

    @Override
    public PreFiscalizationFeedback eventBeforeReceiptFiscalized(Receipt receipt, LoyaltyResult loyaltyResult) {
        log.debug("eventBeforeReceiptFiscalized start");
        for (Card card : receipt.getCards()) {
            if (!card.getProcessingId().equals(LineCardPlugin.PLUGIN_NAME))
                continue;
            if(receipt.getType() != ReceiptType.SALE)
                return null;
            BigDecimal balance = card.getBonusBalance().getBalance();
            BigDecimal bonusAmount = getReceiptBonusAmount(receipt);
            BigDecimal bonusToChargeOff = card.getBonusBalance() != null && card.getBonusBalance().getSumToChargeOff() != null ? card.getBonusBalance().getSumToChargeOff() : BigDecimal.ZERO;
            BigDecimal bonusTotal = balance.add(bonusAmount).subtract(bonusToChargeOff);

            PreFiscalizationFeedback feedback = new PreFiscalizationFeedback();
            Slip slip = new Slip();
            slip.setDisableCut(true);
            slip.setDisableRequisites(true);
            slip.setSeparated(false);
            if (card.getExtendedAttributes().get("wtmpass") != null)
                slip.getParagraphs().add(new SlipParagraph(SlipParagraphType.TEXT, String.format("Код для регистрации в личном кабинете lk.grinn-corp.ru: %s", card.getExtendedAttributes().get("wtmpass"))));
            if (bonusAmount.compareTo(BigDecimal.ZERO) > 0)
                slip.getParagraphs().add(new SlipParagraph(SlipParagraphType.TEXT, String.format("Начислено бонусов: %s", bonusAmount)));
            if (bonusToChargeOff.compareTo(BigDecimal.ZERO) > 0)
                slip.getParagraphs().add(new SlipParagraph(SlipParagraphType.TEXT, String.format("Списано бонусов: %s", bonusToChargeOff)));
            if (bonusTotal.compareTo(BigDecimal.ZERO) > 0)
                slip.getParagraphs().add(new SlipParagraph(SlipParagraphType.TEXT, String.format("Бонусов на карте: %s", bonusTotal)));
            feedback.getSlips().add(slip);
            return feedback;
        }
        return null;
    }

    @Override
    public FiscalizationFeedback onReceiptFiscalized(Receipt receipt, LoyaltyResult loyaltyResult) {
        log.debug("eventReceiptFiscalized({})", receipt.getNumber());
        if(receipt.getType() != ReceiptType.SALE)
            return null;
        for (Card card : receipt.getCards()) {
            if (!card.getProcessingId().equals(LineCardPlugin.PLUGIN_NAME))
                continue;

            BigDecimal bonusAmount = getReceiptBonusAmount(receipt);
            if (bonusAmount.compareTo(BigDecimal.ZERO) > 0) {
                AddBonusTransaction transaction = new AddBonusTransaction(card.getCardNumber(), bonusAmount, getCassa(), getChekSn(receipt));

                try {
                    log.debug("transaction {}", transaction);
                    BonusTransactionResponse bonusTransactionResponse = apiRequest.addBonus(transaction);
                    log.debug("response {}", bonusTransactionResponse);
                    return null;
                }
                catch (Exception e) {
                    try {
                        LoyProviderFeedback loyProviderFeedback = new LoyProviderFeedback();
                        loyProviderFeedback.setPayload(new APIObjectMapper().writeValueAsString(transaction));
                        log.debug("return feedback {}", loyProviderFeedback.getPayload());
                        FiscalizationFeedback fiscalizationFeedback = new FiscalizationFeedback();
                        fiscalizationFeedback.getFeedbacks().add(loyProviderFeedback);
                        return fiscalizationFeedback;
                    }
                    catch (Exception err) {
                        log.debug("error generate feedback", err);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Collection<LoyProviderFeedback> onSendFeedback(Collection<LoyProviderFeedback> feedbacks) {
        log.debug("onSendFeedback start");
        Collection<LoyProviderFeedback> result = new HashSet<>();
        feedbacks.forEach(feedback -> {
            log.debug("onSendFeedback({}) called, payload {}", feedback, feedback.getPayload());
            try {
                AddBonusTransaction transaction = new APIObjectMapper().readValue(feedback.getPayload(), AddBonusTransaction.class);
                log.debug("transaction {}", transaction);
                BonusTransactionResponse bonusTransactionResponse = apiRequest.addBonus(transaction);
                log.debug("response {}", bonusTransactionResponse);
            }
            catch (Exception e) {
                feedback.setAttemptsCount(feedback.getAttemptsCount() + 1);
                result.add(feedback);
            }
        });
        return result;
    }

    private BigDecimal getReceiptBonusAmount(Receipt receipt) {
        if (!pluginConfiguration.isAccrueBonusAllowed())
            return BigDecimal.ZERO;
        return calculationStrategyService.getReceiptBonusAmount(receipt);
    }

    private BigDecimal getCanChargeOffItem(LineItem item, Collection<Discount> discounts) {
        if (!pluginConfiguration.isPayBonusAllowed() || !item.isPayBonusAllowed() || !item.isDiscountable() || item.getMinPrice().compareTo(BigDecimal.ZERO) > 0 || item.getPluginId().equals(LineGoodsPlugin.PLUGIN_NAME))
            return BigDecimal.ZERO;

        BigDecimal sumOfPosition = getItemSumMinusDiscounts(item, discounts);
        BigDecimal sumOfDiscount = sumOfPosition.multiply(MAX_RATIO_TO_WRITEOFF).setScale(2, RoundingMode.HALF_UP);
        BigDecimal minSum = sumOfPosition.subtract(MIN_SUM_AFTER_WRITEOFF).setScale(2, RoundingMode.HALF_UP);
        return sumOfDiscount.min(minSum);
    }

    private BigDecimal getCanChargeOffItems(List<LineItem> items, Collection<Discount> discounts) {
        return items.stream().map(item -> getCanChargeOffItem(item, discounts)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getReceiptSumMinusDiscounts(Receipt receipt, Collection<Discount> discounts) {
        return receipt.getLineItems().stream().map(item -> getItemSumMinusDiscounts(item, discounts)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getItemSumMinusDiscounts(LineItem item, Collection<Discount> discounts) {
        return item.getSum().subtract(
                discounts.stream().filter(discount -> discount.getPosNo() == item.getNumber())
                        .map(Discount::getValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

}
