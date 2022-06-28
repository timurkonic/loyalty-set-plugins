package ru.grinn.loyalty;

import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import org.slf4j.Logger;
import ru.grinn.loyalty.dto.PluginConfiguration;

public final class PluginConfigurationLoader {
    private static final String PLUGIN_CONFIGURATION_PATH = "/home/tc/storage/crystal-cash/config/plugins/Loyalty-grinn.xml";

    private static PluginConfiguration pluginConfiguration;

    private final Logger log;

    public PluginConfigurationLoader(Logger log) {
        this.log = log;
    }

    private PluginConfiguration loadPluginConfiguration() {
        try {
            JAXBContext context = JAXBContext.newInstance(PluginConfiguration.class);
            return (PluginConfiguration) context.createUnmarshaller().unmarshal(new FileInputStream(PLUGIN_CONFIGURATION_PATH));
        } catch (IOException e) {
            log.info("Config file not found. Loading default configuration...");
            return new PluginConfiguration();
        } catch (Exception e) {
            log.error("Config file load error", e);
            return new PluginConfiguration();
        }
    }

    public PluginConfiguration getPluginConfiguration() {
        if (pluginConfiguration == null) {
            pluginConfiguration = loadPluginConfiguration();
            log.info("Plugin configuration: {}", pluginConfiguration);
        }
        return pluginConfiguration;
    }
}
