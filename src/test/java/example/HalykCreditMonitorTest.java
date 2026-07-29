package example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HalykCreditMonitorTest {

    @Test
    void monitorHalykCreditTerms() throws IOException {
        Document doc = Jsoup.connect("https://halykbank.kz/business/credit/biznes-kredit")
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        Element sumRow = doc.selectFirst("td:contains(Сумма)");
        Element termRow = doc.selectFirst("td:contains(Срок кредитования)");

        String currentSum = (sumRow != null && sumRow.nextElementSibling() != null)
                ? sumRow.nextElementSibling().text().trim() : "";
        String currentTerm = (termRow != null && termRow.nextElementSibling() != null)
                ? termRow.nextElementSibling().text().trim() : "";

        String expectedSum = "до 30 000 000 ₸";
        String expectedTerm = "от 1 до 26 месяцев";

        Assertions.assertEquals(expectedSum, currentSum, "⚠ Halyk Bank: Изменилась сумма кредита!");
        Assertions.assertEquals(expectedTerm, currentTerm, "⚠ Halyk Bank: Изменился срок кредитования!");

        System.out.println("Изменений нет. Условия кредитования соответствуют эталону.");
    }
}