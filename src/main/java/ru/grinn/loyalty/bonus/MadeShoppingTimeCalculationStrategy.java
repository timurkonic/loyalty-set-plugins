package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class MadeShoppingTimeCalculationStrategy extends AbstractCalculationStrategy {
    private final static String[] madeShoppingTimeGroups = {"500203", "500204", "500205", "500206", "500210", "500211", "500301", "500304", "500305", "500306", "500307", "500308", "500401", "500403", "500404", "500405", "500406", "500407", "500408", "500409", "500415", "500416", "500417", "500418", "500419"};
    private final Logger log;

    public MadeShoppingTimeCalculationStrategy(Logger log) {
        this.log = log;
    }

    @Override
    public BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus) {
        if (receipt.getAppliedAdvertisingActions().stream().noneMatch(action -> action.getLabels().contains("MADESHOPPINGTIME")))
            return BigDecimal.ZERO;

        BigDecimal chekAmount = receipt.getLineItems().stream()
            .filter(item -> isItemAccrueBonusAllowedAction(receipt, item))
            .filter(this::isItemMadeShoppingTime)
            .map(LineItem::getSum)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bonusAmount = getBonusAmount(chekAmount);

        log.debug("MadeShoppingTimeCalculationStrategy: Сумма товаров {}, сумма бонусов к начислению {}", chekAmount, bonusAmount);

        return bonusAmount;
    }

    private boolean isItemMadeShoppingTime(LineItem item) {
        return Arrays.asList(madeShoppingTimeGroups).contains(item.getMerchandise().getGroupCode());
    }

    private BigDecimal getBonusAmount(BigDecimal chekAmount) {
        return chekAmount.multiply(new BigDecimal("0.3")).setScale(2, RoundingMode.HALF_UP);
    }


}
