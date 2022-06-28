package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class AddRubleTransaction extends Transaction {

    public AddRubleTransaction(String account, BigDecimal amount, int cassa, int chekSn) {
        super("addruble", account, amount, cassa, chekSn);
    }

    @Override
    public String toString() {
        return "AddRubleTransaction{" +
                "account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
