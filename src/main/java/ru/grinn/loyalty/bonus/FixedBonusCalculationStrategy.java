package ru.grinn.loyalty.bonus;

import org.slf4j.Logger;
import ru.crystals.pos.spi.receipt.Receipt;

import java.math.BigDecimal;
import java.util.Arrays;

public class FixedBonusCalculationStrategy extends AbstractCalculationStrategy {
    private final Logger log;

    public FixedBonusCalculationStrategy(Logger log) {
        this.log = log;
    }

    private final static int [] fixedBonusAmounts = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 2500, 3000};

    @Override
    public BigDecimal getReceiptBonusAmount(Receipt receipt, CalculationStatus calculationStatus) {
        int result = Arrays.stream(fixedBonusAmounts)
                .filter(amount -> receipt.getAppliedAdvertisingActions().stream().anyMatch(action -> action.getLabels().contains(String.format("BONUS%d", amount))))
                .peek(amount -> log.debug("FixedBonusCalculationStrategy: Начислено {} бонусов", amount))
                .reduce(0, Integer::sum);
        return new BigDecimal(result);
    }

}
