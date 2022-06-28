package ru.grinn.loyalty;

import org.slf4j.Logger;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.POSInfo;
import ru.crystals.pos.spi.annotation.Inject;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.spi.ui.UIForms;
import ru.grinn.loyalty.dto.PluginConfiguration;

public class LinePlugin {
    @Inject
    protected static Logger log;

    @Inject
    protected static IntegrationProperties properties;

    @Inject
    protected static POSInfo posInfo;

    @Inject
    protected UIForms ui;

    protected PluginConfiguration pluginConfiguration;

    void init() {
        pluginConfiguration = new PluginConfigurationLoader(log).getPluginConfiguration();
    }

    protected boolean isCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^9900\\d{9}$");
    }

    protected int getCassa() {
        return posInfo.getShopNumber() * 1000 + posInfo.getPOSNumber();
    }

    protected int getChekSn(Receipt receipt) {
        return receipt.getShiftNo() * 10000 + receipt.getNumber();
    }

}
