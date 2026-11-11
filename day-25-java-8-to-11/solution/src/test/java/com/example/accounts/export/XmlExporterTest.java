package com.example.accounts.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.model.Account;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class XmlExporterTest {

    @Test
    void exportsEveryAccountAsXml() throws Exception {
        String xml = new XmlExporter().export(Arrays.asList(
                new Account(1L, "Ada Lovelace", "GBP", new BigDecimal("1250.00")),
                new Account(2L, "Grace Hopper", "USD", new BigDecimal("980.50"))));

        assertThat(xml)
                .contains("<accounts>")
                .contains("<account id=\"1\">")
                .contains("<owner>Ada Lovelace</owner>")
                .contains("<balance>980.50</balance>");
    }

    @Test
    void theExportReadsBackIntoTheSameAccounts() throws Exception {
        XmlExporter exporter = new XmlExporter();
        String xml = exporter.export(Arrays.asList(
                new Account(1L, "Ada Lovelace", "GBP", new BigDecimal("1250.00"))));

        AccountsXml back = exporter.read(xml);

        assertThat(back.getAccounts()).hasSize(1);
        assertThat(back.getAccounts().get(0).getOwner()).isEqualTo("Ada Lovelace");
        assertThat(back.getAccounts().get(0).getBalance()).isEqualByComparingTo("1250.00");
    }
}
