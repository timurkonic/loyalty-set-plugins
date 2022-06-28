package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class AddBonusTransaction extends Transaction {

    public AddBonusTransaction(String account, BigDecimal amount, int cassa, int chekSn) {
        super("addbonus", account, amount, cassa, chekSn);
    }

    @Override
    public String toString() {
        return "AddBonusTransaction{" +
                "account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
