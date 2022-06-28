package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class AddRubleTransaction {
    private final String type;
    private String account;
    private BigDecimal amount;
    private int cassa;
    private int chekSn;

    public AddRubleTransaction() {
        type = "addruble";
    }

    public AddRubleTransaction(String account, BigDecimal amount, int cassa, int chekSn) {
        this.type = "addruble";
        this.account = account;
        this.amount = amount;
        this.cassa = cassa;
        this.chekSn = chekSn;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getCassa() {
        return cassa;
    }

    public void setCassa(int cassa) {
        this.cassa = cassa;
    }

    public int getChekSn() {
        return chekSn;
    }

    public void setChekSn(int chek_sn) {
        this.chekSn = chek_sn;
    }

    @Override
    public String toString() {
        return "AddRubleTransaction{" +
                "type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", amount=" + amount +
                ", cassa=" + cassa +
                ", chekSn=" + chekSn +
                '}';
    }
}
