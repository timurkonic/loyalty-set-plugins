package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class Transaction {
    protected final String type;
    protected final String account;
    protected final BigDecimal amount;
    protected final int cassa;
    protected final int chekSn;

    public Transaction(String type, String account, BigDecimal amount, int cassa, int chekSn) {
        this.type = type;
        this.account = account;
        this.amount = amount;
        this.cassa = cassa;
        this.chekSn = chekSn;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
