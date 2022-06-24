package ru.grinn.loyalty;

import org.slf4j.Logger;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.crystals.pos.spi.POSInfo;
import ru.crystals.pos.spi.annotation.Inject;

public class LinePlugin {
    private static final String PLUGIN_CONFIGURATION_PATH = "/home/tc/storage/crystal-cash/config/plugins/Loyalty-grinn.xml";

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
        return cardNumber != null && cardNumber.matches("^99\\d{11}$");
    }

    protected PluginConfiguration getPluginConfiguration() {
        if (pluginConfiguration == null)
            pluginConfiguration = PluginConfigurationLoader.loadPluginConfiguration(log);
        return pluginConfiguration;
    }

}
