package com.example.accounts.export;

import com.example.accounts.model.Account;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** The root of the export document: {@code <accounts><account id="1">...</account></accounts>}. */
@XmlRootElement(name = "accounts")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountsXml {

    @XmlElement(name = "account")
    private List<Account> accounts = new ArrayList<>();

    protected AccountsXml() {
    }

    public AccountsXml(List<Account> accounts) {
        this.accounts = new ArrayList<>(accounts);
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}
