package com.example.accounts.export;

import com.example.accounts.model.Account;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

/**
 * Turns accounts into XML for the downstream reconciliation system, with JAXB
 * (Java Architecture for XML Binding), which shipped inside the JDK up to Java 10.
 */
@Component
public class XmlExporter {

    private final JAXBContext context;

    public XmlExporter() throws JAXBException {
        this.context = JAXBContext.newInstance(AccountsXml.class);
    }

    public String export(List<Account> accounts) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter xml = new StringWriter();
        marshaller.marshal(new AccountsXml(accounts), xml);
        return xml.toString();
    }

    public AccountsXml read(String xml) throws JAXBException {
        return (AccountsXml) context.createUnmarshaller().unmarshal(new StringReader(xml));
    }
}
