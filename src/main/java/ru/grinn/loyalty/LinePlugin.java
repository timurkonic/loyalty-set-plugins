package ru.grinn.loyalty;

import org.slf4j.Logger;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.POSInfo;
import ru.crystals.pos.spi.annotation.Inject;

public class LinePlugin {
    @Inject
    protected Logger log;

    @Inject
    protected IntegrationProperties properties;

    @Inject
    protected POSInfo posInfo;

    private PluginConfiguration pluginConfiguration;

    public LinePlugin() {
    }

    protected boolean isCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^9900\\d{9}$");
    }

    protected PluginConfiguration getPluginConfiguration() {
        if (pluginConfiguration == null)
            pluginConfiguration = PluginConfigurationLoader.loadPluginConfiguration(log);
        return pluginConfiguration;
    }

}
