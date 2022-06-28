package ru.grinn.loyalty.bonus;

import ru.crystals.pos.spi.receipt.Receipt;

import org.slf4j.Logger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CalculationStrategyService {

    private final List<AbstractCalculationStrategy> calculationStrategies;

    public CalculationStrategyService(Logger log) {
        calculationStrategies = new ArrayList<>();
        calculationStrategies.add(new MadeShoppingTimeCalculationStrategy(log));
        calculationStrategies.add(new BrkCalculationStrategy(log));
        calculationStrategies.add(new BaseCalculationStrategy(log));
        calculationStrategies.add(new FixedBonusCalculationStrategy(log));
        calculationStrategies.add(new GroupBonusCalculationStrategy(log));
    }

     public BigDecimal getReceiptBonusAmount(Receipt receipt) {
        CalculationStatus calculationStatus = new CalculationStatus();
        return calculationStrategies.stream().map(item -> item.getReceiptBonusAmount(receipt, calculationStatus)).reduce(BigDecimal.ZERO, BigDecimal::add);
     }

}
