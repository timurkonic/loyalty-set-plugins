package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BrkCalculationStrategy extends AbstractCalculationStrategy {
    private final Logger log;

    public BrkCalculationStrategy(Logger log) {
        this.log = log;
    }

    @Override
    public BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus) {
        if (receipt.getAppliedAdvertisingActions().stream().noneMatch(action -> action.getLabels().contains("BRKBONUS")))
            return BigDecimal.ZERO;

        BigDecimal chekAmount = receipt.getLineItems().stream()
                .filter(this::isItemAccrueBonusAllowedMinPrice)
                .filter(this::isItemAccrueBonusAllowedPlugin)
                .filter(this::isItemAccrueBonusAllowedTobacco)
                .filter(item-> !isItemAlreadyAccrued(calculationStatus, item))
                .peek(item -> setItemAlreadyAccrued(calculationStatus, item))
                .map(LineItem::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bonusAmount = getBonusAmount(chekAmount);

        log.debug("BrkCalculationStrategy: Сумма товаров {}, сумма бонусов к начислению {}", chekAmount, bonusAmount);

        return bonusAmount;
    }

    private BigDecimal getBonusAmount(BigDecimal chekAmount) {
        return chekAmount.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP);
    }


}
