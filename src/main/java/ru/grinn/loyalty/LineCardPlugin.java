package ru.grinn.loyalty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.annotation.PostConstruct;

import ru.crystals.pos.api.card.*;
import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.card.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.plugin.card.*;
import ru.crystals.pos.spi.receipt.*;

import ru.grinn.loyalty.dto.Account;
import ru.grinn.loyalty.dto.BonusTransactionResponse;
import ru.grinn.loyalty.dto.RollbackTransactionResponse;
import ru.grinn.loyalty.dto.WriteOffBonusTransaction;

@POSPlugin(id = LineCardPlugin.PLUGIN_NAME)
public class LineCardPlugin extends LinePlugin implements CardPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.cardplugin";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private APIRequest apiRequest;

    @PostConstruct
    void init() {
        super.init();
        apiRequest = new APIRequest(properties);
        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    public CardSearchResponse searchCard(CardSearchRequest request) {
        String cardNumber = extractCardNumber(request);
        log.debug("searchCard({})", cardNumber);

        if (!isCardNumber(cardNumber))
            return new CardSearchResponse(CardSearchResponseStatus.UNKNOWN_CARD);

        CardSearchResponse result = findCardByNumber(cardNumber);
        if (result.getResponseStatus() == CardSearchResponseStatus.OK) {
            String balance = result.getCard().getExtendedAttributes().get("balance");
            String discount = result.getCard().getExtendedAttributes().get("discount");
            if (balance != null && new BigDecimal(balance).compareTo(BigDecimal.ZERO) > 0) {
                if (pluginConfiguration.isCardDiscountAllowed() && discount != null && new BigDecimal(discount).compareTo(BigDecimal.ZERO) > 0)
                    result.addCashierMessage(String.format("Баланс карты в рублях: %s, скидка: %s%%\nЧек необходимо оплатить балансом с карты", balance, discount));
                else
                    result.addCashierMessage(String.format("Баланс карты в рублях: %s", balance));
            }
        }
        return result;
    }

    @Override
    public CardInfo getCardInfo(CardSearchRequest request) {
        String cardNumber = extractCardNumber(request);

        if (!isCardNumber(cardNumber))
            return new CardInfo(CardSearchResponseStatus.UNKNOWN_CARD, null);

        CardSearchResponse response = findCardByNumber(cardNumber);
        if (!response.getResponseStatus().equals(CardSearchResponseStatus.OK))
            return new CardInfo(response.getResponseStatus(), null);

        Card card = response.getCard();
        LinkedHashMap<String, String> cardInfoMap = new LinkedHashMap<>();
        if (card.getCardHolder() != null) {
            if (card.getCardHolder().getLastName() != null && card.getCardHolder().getFirstName() != null && card.getCardHolder().getMiddleName() != null)
                cardInfoMap.put("Покупатель", String.format("%s %s %s", card.getCardHolder().getLastName(), card.getCardHolder().getFirstName(), card.getCardHolder().getMiddleName()));
            if (card.getCardHolder().getBirthDate() != null)
                cardInfoMap.put("Дата рождения", dateFormat.format(card.getCardHolder().getBirthDate()));
            if (card.getCardHolder().getPhone() != null)
                cardInfoMap.put("Телефон", card.getCardHolder().getPhone() +
                    (card.getCardHolder().getReceiptFeedbackTypes().contains(ReceiptFeedbackType.BY_PHONE) ? " <- ЧЕК" : ""));
            if (card.getCardHolder().getEmail() != null)
                cardInfoMap.put("Email", card.getCardHolder().getEmail() +
                    (card.getCardHolder().getReceiptFeedbackTypes().contains(ReceiptFeedbackType.BY_EMAIL) ? " <- ЧЕК" : ""));
        }

        cardInfoMap.put("Статус карты", (card.getCardStatus() == CardStatus.ACTIVE ? "Активна" : "Заблокирована") +
                (Integer.parseInt(card.getExtendedAttributes().getOrDefault("ownerFilled", "0")) == 0 ? ", не активирована" : ", активирована"));

        cardInfoMap.put("Баланс бонусов", card.getBonusBalance() != null ? String.valueOf(card.getBonusBalance().getBalance()) : "0.00");
        cardInfoMap.put("Баланс рублей", card.getExtendedAttributes().get("balance"));
        cardInfoMap.put("Скидка", card.getExtendedAttributes().get("discount"));

        if (Integer.parseInt(card.getExtendedAttributes().getOrDefault("block", "0")) > 0)
            cardInfoMap.put("Блокировка", card.getExtendedAttributes().get("blockName"));

        if (Boolean.parseBoolean(card.getExtendedAttributes().getOrDefault("birthdayAction", "false")))
            cardInfoMap.put("Акция к ДР", "Активна");

        CardInfo cardInfo = new CardInfo(CardSearchResponseStatus.OK, cardInfoMap);
        cardInfo.setTitle(card.getExtendedAttributes().get("typeName"));
        return cardInfo;
    }

    @Override
    public BonusWriteOffOperationResponse writeOff(Card bonusCard, BigDecimal bonusesToWriteOff, Receipt receipt) {
        log.debug("writeOff({}, {})", bonusCard.getCardNumber(), bonusesToWriteOff);
        BonusWriteOffOperationResponse response = new BonusWriteOffOperationResponse();
        try {
            WriteOffBonusTransaction transaction = new WriteOffBonusTransaction(bonusCard.getCardNumber(), bonusesToWriteOff, getCassa(), getChekSn(receipt));

            log.debug("transaction {}", transaction);
            BonusTransactionResponse bonusTransactionResponse = apiRequest.writeOffBonus(transaction);
            log.debug("response {}", bonusTransactionResponse);

            if (bonusTransactionResponse.getError() == null) {
                response.setResponseStatus(BonusOperationStatus.OK);
                response.setTxId(bonusTransactionResponse.getTransactionId());
            }
            else {
                response.setResponseStatus(BonusOperationStatus.ERROR);
                response.setOperatorMessage(bonusTransactionResponse.getError());
            }
        }
        catch (Exception e) {
            log.debug("writeoff error", e);
            response.setResponseStatus(BonusOperationStatus.ERROR);
        }
        return response;
    }

    @Override
    public BonusOperationResponse rollback(String txId) {
        log.debug("rollback({})", txId);
        BonusOperationResponse response = new BonusOperationResponse();
        try {
            RollbackTransactionResponse rollbackTransactionResponse = apiRequest.rollback(txId);
            log.debug("response {}", rollbackTransactionResponse);
            if (rollbackTransactionResponse.getError() == null) {
                response.setResponseStatus(BonusOperationStatus.OK);
                response.setTxId("R" + txId);
            }
            else {
                response.setResponseStatus(BonusOperationStatus.ERROR);
            }
        }
        catch (Exception e) {
            log.debug("rollback error", e);
            response.setResponseStatus(BonusOperationStatus.ERROR);
        }
        return response;
    }

    private String extractCardNumber(CardSearchRequest request) {
        if (request.getEventSource() == CardSearchEventSource.BARCODE) {
            return request.getSearchString();
        }
        return null;
    }

    private CardSearchResponse findCardByNumber(String number) {
        try {
            log.debug("findCardByNumber({})", number);

            Account account = apiRequest.getAccount(number);
            log.debug("getAccount: {}", account);

            CardEntity card = new CardEntity();

            HashSet<ReceiptFeedbackType> receiptFeedbackTypes = new HashSet<>();
            if (account.isReceiptPhone())
                receiptFeedbackTypes.add(ReceiptFeedbackType.BY_PHONE);
            if (account.isReceiptEmail())
                receiptFeedbackTypes.add(ReceiptFeedbackType.BY_EMAIL);

            CardHolderEntity cardHolder = new CardHolderEntity() { // Crystals sucks
                public Set<ReceiptFeedbackType> getReceiptFeedbackTypes() {
                    return receiptFeedbackTypes;
                }
            };
            card.setCardHolder(cardHolder);

            card.setId(account.getId());
            card.setCardNumber(account.getId());

            card.getExtendedAttributes().put("type", String.valueOf(account.getType()));
            card.getExtendedAttributes().put("typeName", account.getTypeName());

            if (account.getActive() == 0 || account.getBlock() != 0)
                card.setCardStatus(CardStatus.BLOCKED);

            card.getExtendedAttributes().put("active", String.valueOf(account.getActive()));
            card.getExtendedAttributes().put("block", String.valueOf(account.getBlock()));
            card.getExtendedAttributes().put("blockName", account.getBlockName());

            cardHolder.setLastName(account.getOwnerFamilyName());
            cardHolder.setFirstName(account.getOwnerFirstName());
            cardHolder.setMiddleName(account.getOwnerThirdName());

            cardHolder.setBirthDate(account.getOwnerBirthday());

            cardHolder.setPhone(account.getOwnerPhone());
            cardHolder.setEmail(account.getOwnerEmail());

            card.getExtendedAttributes().put("ownerFilled", String.valueOf(account.getOwnerFilled()));

            card.getExtendedAttributes().put("balance", account.getBalance().toString());

            card.setBonusBalance(new BonusBalanceEntity(account.getBalanceBns()));

            card.getExtendedAttributes().put("discount", String.valueOf(account.getDiscount()));

            card.getExtendedAttributes().put("birthdayAction", String.valueOf(account.getBirthdayAction()));

            if (account.getWtmpass() != null)
                card.getExtendedAttributes().put("wtmpass", account.getWtmpass());

            card.getExtendedAttributes().put("block", String.valueOf(account.getBlock()));

            return new CardSearchResponse(CardSearchResponseStatus.OK, card);
        }
        catch (Exception e) {
            log.debug("findCardByNumber error", e);
            CardEntity card = new CardEntity();
            card.setId(number);
            card.setCardNumber(number);
            card.setBonusBalance(new BonusBalanceEntity(BigDecimal.ZERO));
            CardSearchResponse result = new CardSearchResponse(CardSearchResponseStatus.OK, card);
            result.addCashierMessage("Ошибка подключения к процессингу");
            return result;
        }
    }

}
