package ru.grinn.loyalty;

import ru.crystals.pos.api.ext.loyal.dto.AdvertisingAction;
import ru.crystals.pos.api.plugin.AdvertisingActionConditionPlugin;
import ru.crystals.pos.spi.annotation.POSPlugin;
import ru.crystals.pos.spi.receipt.Receipt;

import javax.annotation.PostConstruct;

@POSPlugin(id = LineActionDinnerDiscountPlugin.PLUGIN_NAME)
public class LineActionDinnerDiscountPlugin extends LinePlugin implements AdvertisingActionConditionPlugin {

    public static final String PLUGIN_NAME = "grinn.loyalty.dinnerdiscountplugin";

    @PostConstruct
    void init() {
        super.init();
        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    public void onDiscountCalculationStarted(Receipt receipt) {
    }

    @Override
    public boolean isConditionExecuted(AdvertisingAction action, Receipt receipt) {
        return pluginConfiguration.isDinnerDiscountAllowed() &&
                receipt.getCards().stream().filter(card -> card.getProcessingId().equals(LineCardPlugin.PLUGIN_NAME))
                        .anyMatch(card -> card.getExtendedAttributes().getOrDefault("typeName", "").contains("сотрудник"));
    }

    @Override
    public void onDiscountCalculationFinished(Receipt receipt) {
    }

}