package ru.grinn.loyalty;

import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import java.io.FileInputStream;
import java.io.IOException;

public final class PluginConfigurationLoader {
    private static final String PLUGIN_CONFIGURATION_PATH = "/home/tc/storage/crystal-cash/config/plugins/Loyalty-grinn.xml";

    public static PluginConfiguration loadPluginConfiguration(Logger log) {
        try {
            JAXBContext context = JAXBContext.newInstance(PluginConfiguration.class);
            return (PluginConfiguration) context.createUnmarshaller().unmarshal(new FileInputStream(PLUGIN_CONFIGURATION_PATH));
        }
        catch (IOException e) {
            log.info("Config file not found. Loading default configuration...");
            return new PluginConfiguration();
        }
        catch (Exception e) {
            log.error("Config file load error: {}", e);
            return new PluginConfiguration();
        }
    }

}
