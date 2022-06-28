package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class GroupBonusCalculationStrategy extends AbstractCalculationStrategy {
    private final Logger log;

    public GroupBonusCalculationStrategy(Logger log) {
        this.log = log;
    }

    @Override
    public BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus) {
        return receipt.getAppliedAdvertisingActions().stream()
                .filter(action -> action.getLabels().contains("GROUPBONUS"))
                .map(action -> {
                    BigDecimal chekAmount = receipt.getLineItems().stream()
                            .filter(item -> isItemApplies(item, action.getLabels()))
                            .map(LineItem::getSum)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal percent = action.getLabels().stream()
                            .filter(label -> label.startsWith("P_"))
                            .map(label -> new BigDecimal(label.substring(2)))
                            .reduce(BigDecimal.ZERO, BigDecimal::max);

                    BigDecimal bonusAmount = getBonusAmount(chekAmount, percent);
                    log.debug("GroupBonusCalculationStrategy: Сумма товаров {}, сумма бонусов к начислению {}", chekAmount, bonusAmount);
                    return bonusAmount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isItemApplies(LineItem item, Set<String> labels) {
        String groupCode = item.getMerchandise().getGroupCode();
        return labels.stream()
                .filter(label -> label.startsWith("G_"))
                .map(label -> label.substring(2))
                .anyMatch(groupCode::matches);
    }

    private BigDecimal getBonusAmount(BigDecimal chekAmount, BigDecimal percent) {
        return chekAmount.multiply(percent).divide(new BigDecimal(100), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }


}
