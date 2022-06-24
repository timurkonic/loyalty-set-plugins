package ru.grinn.loyalty;

import java.math.BigDecimal;
import java.util.TreeMap;

import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.card.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.plugin.card.*;
import ru.crystals.pos.spi.receipt.*;

import javax.annotation.PostConstruct;

@POSPlugin(id = LineCardPlugin.PLUGIN_NAME)
public class LineCardPlugin extends LinePlugin implements CardPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.cardplugin";

    @PostConstruct
    void init() {
        log.info("Plugin {} loaded", this.getClass());
        log.info("Plugin configuration: {}", getPluginConfiguration());
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
                if (getPluginConfiguration().isCardDiscountAllowed() && discount != null && new BigDecimal(discount).compareTo(BigDecimal.ZERO) > 0)
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
        if (!response.getResponseStatus().equals(CardSearchResponseStatus.OK)) {
            return new CardInfo(response.getResponseStatus(), null);
        }
        Card card = response.getCard();
        TreeMap<String, String> cardInfoMap = new TreeMap<>();
        cardInfoMap.put("Покупатель", String.format("%s %s %s", card.getCardHolder().getLastName(), card.getCardHolder().getFirstName(), card.getCardHolder().getMiddleName()));
        cardInfoMap.put("Дата рождения", card.getExtendedAttributes().get("birthday"));
        cardInfoMap.put("Акция к ДР", card.getExtendedAttributes().get("birthdayAction").equals("yes") ? "да" : "нет");
        cardInfoMap.put("Статус карты", card.getCardStatus() == CardStatus.ACTIVE ? "Активна" : "Заблокирована");
        cardInfoMap.put("Баланс бонусов", card.getBonusBalance() != null ? String.valueOf(card.getBonusBalance().getBalance()) : "0.00");
        cardInfoMap.put("Баланс рублей", card.getExtendedAttributes().get("balance"));
        cardInfoMap.put("Скидка", card.getExtendedAttributes().get("discount"));
        if (card.getExtendedAttributes().get("blockName").length() > 0)
            cardInfoMap.put("Блокировка", card.getExtendedAttributes().get("blockName"));

        CardInfo cardInfo = new CardInfo(CardSearchResponseStatus.OK, cardInfoMap);
        cardInfo.setTitle(card.getExtendedAttributes().get("type"));
        return cardInfo;
    }

    @Override
    public BonusWriteOffOperationResponse writeOff(Card bonusCard, BigDecimal bonusesToWriteOff, Receipt receipt) {
        return null;
    }

    @Override
    public BonusOperationResponse rollback(String txId) {
        return null;
    }

    private String extractCardNumber(CardSearchRequest request) {
        if (request.getEventSource() == CardSearchEventSource.BARCODE) {
            return request.getSearchString();
        }
        return null;
    }

    private CardSearchResponse findCardByNumber(String number) {
        return null;
    }

}
