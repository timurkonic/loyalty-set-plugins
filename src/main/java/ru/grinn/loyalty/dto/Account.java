package ru.grinn.loyalty.dto;

import java.math.BigDecimal;
import java.util.Date;

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
    private Date ownerBirthday;
    private String ownerPhone;
    private String ownerEmail;
    private int ownerFilled;
    private BigDecimal discount;
    private String birthdayAction;
    private String wtmpass;

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getBalanceBns() {
        return balanceBns;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getActive() {
        return active;
    }

    public int getBlock() {
        return block;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getOwnerFamilyName() {
        return ownerFamilyName;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public String getOwnerThirdName() {
        return ownerThirdName;
    }

    public Date getOwnerBirthday() {
        return ownerBirthday;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public int getOwnerFilled() {
        return ownerFilled;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public String getBirthdayAction() {
        return birthdayAction;
    }

    public String getWtmpass() {
        return wtmpass;
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
                ", birthdayAction='" + birthdayAction + '\'' +
                ", wtmpass='" + wtmpass + '\'' +
                '}';
    }
}
