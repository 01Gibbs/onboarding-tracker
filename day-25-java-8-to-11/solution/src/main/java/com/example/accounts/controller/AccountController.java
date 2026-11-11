package com.example.accounts.controller;

import com.example.accounts.export.XmlExporter;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final XmlExporter xmlExporter;

    public AccountController(AccountService accountService, XmlExporter xmlExporter) {
        this.accountService = accountService;
        this.xmlExporter = xmlExporter;
    }

    @GetMapping
    public List<Account> list() {
        return accountService.findAll();
    }

    @GetMapping("/export")
    public ResponseEntity<String> export() throws JAXBException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xmlExporter.export(accountService.findAll()));
    }
}
