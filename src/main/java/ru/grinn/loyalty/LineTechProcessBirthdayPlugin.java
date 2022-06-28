package ru.grinn.loyalty;

import javax.annotation.PostConstruct;

import ru.crystals.pos.api.plugin.*;
import ru.crystals.pos.api.plugin.techprocess.*;
import ru.crystals.pos.api.ui.listener.*;
import ru.crystals.pos.spi.annotation.*;
import ru.crystals.pos.spi.receipt.*;
import ru.crystals.pos.spi.ui.*;

@POSPlugin(id = LineTechProcessBirthdayPlugin.PLUGIN_NAME)
public class LineTechProcessBirthdayPlugin extends LinePlugin implements TechProcessPlugin {
    public static final String PLUGIN_NAME = "grinn.loyalty.techprocessbirthdayplugin";

    @PostConstruct
    void init() {
        log.info("Plugin {} loaded", this.getClass());
    }

    @Override
    public void intercept(InterceptionContext context, InterceptorCallback callback) {
        log.debug("intercept start");
        if (context.getInterceptedStage() == InterceptedStage.CALCULATION_COMPLETE) {
            onCalculationComplete(context, callback);
        } else {
            restoreThisStage(callback);
        }
    }

    private void onCalculationComplete(InterceptionContext context, InterceptorCallback callback) {
        log.debug("onCalculationComplete start");
        if (context.getReceipt().isPresent()) {
            Receipt receipt = context.getReceipt().get();
            boolean actionBirthday = receipt.getCards().stream().anyMatch(card -> "true".equals(card.getExtendedAttributes().get(LineActionBirthdayPlugin.BIRTHDAY_ACTION_KEY)));
            if (actionBirthday && context.getApplicableStatusSet().contains(InterceptionStatus.RETRY) && receipt.getData().get(LineActionBirthdayPlugin.BIRTHDAY_ACTION_ACCEPTED_KEY) == null) {
                ui.showDialogForm(new DialogFormParameters("Возможно применение скидки к дню рождения. Проверьте паспорт и уточните, применить скидку?", "Да", "Нет"), new DialogListener() {
                    @Override
                    public void eventButton1pressed() {
                        InterceptionResult result = new InterceptionResult();
                        result.getReceiptExtendedAttributesMap().put(LineActionBirthdayPlugin.BIRTHDAY_ACTION_ACCEPTED_KEY, "true");
                        callback.completed(InterceptionStatus.RETRY, result);
                    }

                    @Override
                    public void eventButton2pressed() {
                        InterceptionResult result = new InterceptionResult();
                        result.getReceiptExtendedAttributesMap().put(LineActionBirthdayPlugin.BIRTHDAY_ACTION_ACCEPTED_KEY, "false");
                        restoreThisStage(callback);
                    }

                    @Override
                    public void eventCanceled() {
                        restoreThisStage(callback);
                    }
                });
            }
            else {
                restoreThisStage(callback);
            }
        }
        else {
            restoreThisStage(callback);
        }
    }

    private void restoreThisStage(InterceptorCallback callback) {
        callback.completed(InterceptionStatus.CONTINUE, new InterceptionResult());
    }
}
