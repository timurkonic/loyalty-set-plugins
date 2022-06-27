package ru.grinn.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Account {
    private String id;
    private BigDecimal balance;
    private BigDecimal balanceBns;
    private int type;
    private String typeName;
    private int active;
    private int block;
    private String blockName;
    private String ownerFamilyName;
    private String ownerFirstName;
    private String ownerThirdName;
    private String ownerBirthday;
    private String ownerPhone;
    private String ownerEmail;
    private int ownerFilled;
    private BigDecimal discount;
    private String wtmpass;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalanceBns() {
        return balanceBns;
    }

    public void setBalanceBns(BigDecimal balanceBns) {
        this.balanceBns = balanceBns;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getOwnerFamilyName() {
        return ownerFamilyName;
    }

    public void setOwnerFamilyName(String ownerFamilyName) {
        this.ownerFamilyName = ownerFamilyName;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public void setOwnerFirstName(String ownerFirstName) {
        this.ownerFirstName = ownerFirstName;
    }

    public String getOwnerThirdName() {
        return ownerThirdName;
    }

    public void setOwnerThirdName(String ownerThirdName) {
        this.ownerThirdName = ownerThirdName;
    }

    public String getOwnerBirthday() {
        return ownerBirthday;
    }

    public void setOwnerBirthday(String ownerBirthday) {
        this.ownerBirthday = ownerBirthday;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public int getOwnerFilled() {
        return ownerFilled;
    }

    public void setOwnerFilled(int ownerFilled) {
        this.ownerFilled = ownerFilled;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getWtmpass() {
        return wtmpass;
    }

    public void setWtmpass(String wtmpass) {
        this.wtmpass = wtmpass;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                ", balanceBns=" + balanceBns +
                ", type=" + type +
                ", typeName='" + typeName + '\'' +
                ", active=" + active +
                ", block=" + block +
                ", blockName='" + blockName + '\'' +
                ", ownerFamilyName='" + ownerFamilyName + '\'' +
                ", ownerFirstName='" + ownerFirstName + '\'' +
                ", ownerThirdName='" + ownerThirdName + '\'' +
                ", ownerBirthday='" + ownerBirthday + '\'' +
                ", ownerPhone='" + ownerPhone + '\'' +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", ownerFilled=" + ownerFilled +
                ", discount=" + discount +
                ", wtmpass='" + wtmpass + '\'' +
                '}';
    }
}
