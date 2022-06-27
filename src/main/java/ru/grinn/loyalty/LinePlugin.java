package ru.grinn.loyalty;

import org.slf4j.Logger;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.POSInfo;
import ru.crystals.pos.spi.annotation.Inject;
import ru.grinn.loyalty.dto.PluginConfiguration;

public class LinePlugin {
    @Inject
    protected static Logger log;

    @Inject
    protected static IntegrationProperties properties;

    @Inject
    protected static POSInfo posInfo;

    protected PluginConfiguration pluginConfiguration;

    public LinePlugin() {
    }

    void init() {
        pluginConfiguration = new PluginConfigurationLoader(log).getPluginConfiguration();
    }

    protected boolean isCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^9900\\d{9}$");
    }

}
