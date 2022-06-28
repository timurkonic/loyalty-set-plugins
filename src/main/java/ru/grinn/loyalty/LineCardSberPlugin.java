package ru.grinn.loyalty;

import java.math.BigDecimal;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import ru.crystals.pos.api.card.*;
import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.card.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.plugin.card.*;
import ru.crystals.pos.spi.receipt.*;

@POSPlugin(id = LineCardSberPlugin.PLUGIN_NAME)
public class LineCardSberPlugin extends LinePlugin implements CardPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.sbercardplugin";

    @PostConstruct
    void init() {
        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    protected boolean isCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^9999\\d{9}$");
    }

    @Override
    public CardSearchResponse searchCard(CardSearchRequest request) {
        String cardNumber = extractCardNumber(request);
        log.debug("searchCard({})", cardNumber);

        if (!isCardNumber(cardNumber))
            return new CardSearchResponse(CardSearchResponseStatus.UNKNOWN_CARD);

        CardSearchResponse result = findCardByNumber(cardNumber);
        if (result.getResponseStatus().equals(CardSearchResponseStatus.OK))
            result.addCashierMessage("Карта сотрудника Сбермаркет");
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
        CardInfo cardInfo = new CardInfo(CardSearchResponseStatus.OK, new HashMap<>());
        cardInfo.setTitle("Карта Сбермаркет");
        return cardInfo;
    }

    @Override
    public BonusWriteOffOperationResponse writeOff(Card bonusCard, BigDecimal bonusesToWriteOff, Receipt receipt) {
        BonusWriteOffOperationResponse response = new BonusWriteOffOperationResponse();
        response.setResponseStatus(BonusOperationStatus.ERROR);
        return response;
    }

    @Override
    public BonusOperationResponse rollback(String txId) {
        BonusOperationResponse response = new BonusOperationResponse();
        response.setResponseStatus(BonusOperationStatus.ERROR);
        return response;
    }

    private String extractCardNumber(CardSearchRequest request) {
        if (request.getEventSource() == CardSearchEventSource.BARCODE) {
            return request.getSearchString();
        }
        return null;
    }

    private CardSearchResponse findCardByNumber(String number) {
        log.debug("findCardByNumber({})", number);
        CardEntity card = new CardEntity();
        CardHolderEntity cardHolder = new CardHolderEntity();
        card.setCardHolder(cardHolder);
            
        card.setId(number);
        card.setCardNumber(number);
        card.setBonusBalance(new BonusBalanceEntity(BigDecimal.ZERO));
        return new CardSearchResponse(CardSearchResponseStatus.OK, card);
    }

}
