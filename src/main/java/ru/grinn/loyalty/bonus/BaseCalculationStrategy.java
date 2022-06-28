package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BaseCalculationStrategy extends AbstractCalculationStrategy {
    private final Logger log;

    public BaseCalculationStrategy(Logger log) {
        this.log = log;
    }

    @Override
    public BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus) {
        BigDecimal chekAmount = receipt.getLineItems().stream()
                .filter(item -> isItemAccrueBonusAllowedAction(receipt, item))
                .filter(this::isItemAccrueBonusAllowedMinPrice)
                .filter(this::isItemAccrueBonusAllowedPlugin)
                .filter(this::isItemAccrueBonusAllowedTobacco)
                .filter(item-> !isItemAlreadyAccrued(calculationStatus, item))
                .peek(item -> setItemAlreadyAccrued(calculationStatus, item))
                .map(LineItem::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bonusAmount = getBonusAmount(chekAmount);

        log.debug("BaseCalculationStrategy: Сумма товаров {}, сумма бонусов к начислению {}", chekAmount, bonusAmount);

        return bonusAmount;
    }

    private BigDecimal getBonusAmount(BigDecimal chekAmount) {
        if (chekAmount.compareTo(new BigDecimal(3000)) >= 0)
            return chekAmount.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        if (chekAmount.compareTo(new BigDecimal(2000)) >= 0)
            return chekAmount.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
        if (chekAmount.compareTo(new BigDecimal(1000)) >= 0)
            return chekAmount.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
        if (chekAmount.compareTo(new BigDecimal(100)) >= 0)
            return chekAmount.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.ZERO;
    }


}
