package ru.grinn.loyalty;

import javax.annotation.PostConstruct;

import ru.crystals.pos.api.ext.loyal.dto.AdvertisingAction;
import ru.crystals.pos.api.plugin.AdvertisingActionConditionPlugin;
import ru.crystals.pos.spi.annotation.POSPlugin;
import ru.crystals.pos.spi.receipt.Receipt;

@POSPlugin(id = LineActionBirthdayPlugin.PLUGIN_NAME)
public class LineActionBirthdayPlugin extends LinePlugin implements AdvertisingActionConditionPlugin {

    public static final String PLUGIN_NAME = "grinn.loyalty.actionbirthdayplugin";
    public static final String BIRTHDAY_ACTION_KEY = "birthdayAction";
    public static final String BIRTHDAY_ACTION_ACCEPTED_KEY = "birthdayActionAccepted";

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
        if ("true".equals(receipt.getData().get(BIRTHDAY_ACTION_ACCEPTED_KEY))) {
            log.debug("Скидка в день рождения применяется");
            return true;
        }
        return false;
    }

    @Override
    public void onDiscountCalculationFinished(Receipt receipt) {
    }

}