package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class WriteOffBonusTransaction extends Transaction {

    public WriteOffBonusTransaction(String account, BigDecimal amount, int cassa, int chekSn) {
        super("paybonus", account, amount, cassa, chekSn);
    }

    @Override
    public String toString() {
        return "WriteOffBonusTransaction{" +
                "account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
