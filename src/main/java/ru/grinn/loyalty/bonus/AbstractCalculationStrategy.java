package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.MarkedProductType;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.grinn.loyalty.LineGoodsPlugin;

import java.math.BigDecimal;

public abstract class AbstractCalculationStrategy {

    public abstract BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus);

    public boolean isItemAccrueBonusAllowedAction(Receipt receipt, LineItem item) {
        return receipt.getAppliedAdvertisingActions()
                .stream()
                .filter(action -> !action.isAccrueBonusAllowed())
                .noneMatch(action -> item.getAppliedDiscounts().stream().anyMatch(discount -> action.getGuid().equals(discount.getAdvertisingActionGuid())));
    }

    public boolean isItemAccrueBonusAllowedMinPrice(LineItem item) {
        return item.getMinPrice().compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isItemAccrueBonusAllowedPlugin(LineItem item) {
        return item.getPluginId() == null || !item.getPluginId().equals(LineGoodsPlugin.PLUGIN_NAME);
    }

    public boolean isItemAccrueBonusAllowedTobacco(LineItem item) {
        try {
            if (item.getMarkedProductType() != null && item.getMarkedProductType().equals(MarkedProductType.TOBACCO)) {
                return false;
            }
        }
        catch (IllegalArgumentException e) { // bug in sdk, do nothing
        }
        return true;
    }

    public boolean isItemAlreadyAccrued(CalculationStatus calculationStatus, LineItem item) {
        return calculationStatus.contains(String.format("ItemAlreadyAccrued%d", item.getNumber()));
    }

    public void setItemAlreadyAccrued(CalculationStatus calculationStatus, LineItem item) {
        calculationStatus.add(String.format("ItemAlreadyAccrued%d", item.getNumber()));
    }

    public void printItem(LineItem item, CalculationStatus calculationStatus, Logger log) {
        log.debug("item #{} barcode={} sum=P{} alreadyAccrued={}", item.getNumber(), item.getMerchandise().getBarcode(), item.getSum(), isItemAlreadyAccrued(calculationStatus, item));
    }
}