package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class PayRubleTransaction extends Transaction {

    private final String pass;

    public PayRubleTransaction(String account, BigDecimal amount, int cassa, int chekSn, String pass) {
        super("payruble", account, amount, cassa, chekSn);
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "PayRubleTransaction{" +
                "pass='" + pass + '\'' +
                ", type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
