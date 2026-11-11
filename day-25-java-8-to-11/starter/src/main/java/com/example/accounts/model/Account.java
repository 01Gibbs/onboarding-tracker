package com.example.accounts.model;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/** One account. The JAXB annotations decide how it looks in the XML export. */
@XmlAccessorType(XmlAccessType.FIELD)
public class Account {

    @XmlAttribute
    private Long id;

    @XmlElement
    private String owner;

    @XmlElement
    private String currency;

    @XmlElement
    private BigDecimal balance;

    /** JAXB needs a no-argument constructor to read XML back into an object. */
    protected Account() {
    }

    public Account(Long id, String owner, String currency, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
