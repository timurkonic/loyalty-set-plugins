package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class RetRubleTransaction extends Transaction {

    private final String pass;

    public RetRubleTransaction(String account, BigDecimal amount, int cassa, int chekSn, String pass) {
        super("retruble", account, amount, cassa, chekSn);
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "RetRubleTransaction{" +
                "pass='" + pass + '\'' +
                ", type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
