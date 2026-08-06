package example;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HalykCreditMonitorTest {

    @Test
    @DisplayName("Мониторинг условий кредитования Halyk Bank")
    void monitorHalykCreditTerms() throws IOException {
        Document doc = Jsoup.connect("https://halykbank.kz/business/credit/biznes-kredit")
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        Element sumRow = doc.selectFirst("td:contains(Сумма)");
        Element termRow = doc.selectFirst("td:contains(Срок кредитования)");

        String currentSum = (sumRow != null && sumRow.nextElementSibling() != null)
                ? sumRow.nextElementSibling().text().trim() : "";
        String currentTerm = (termRow != null && termRow.nextElementSibling() != null)
                ? termRow.nextElementSibling().text().trim() : "";

        String expectedSum = "до 30 000 000 ₸";
        String expectedTerm = "от 1 до 26 месяцев";

        StringBuilder diffLog = new StringBuilder();

        if (!currentSum.equals(expectedSum)) {
            diffLog.append("Обнаружено отличие по сумме кредита:\n")
                    .append("  Ожидалось: ").append(expectedSum).append("\n")
                    .append("  На сайте:   ").append(currentSum).append("\n\n");
        }

        if (!currentTerm.equals(expectedTerm)) {
            diffLog.append("Обнаружено отличие по сроку кредитования:\n")
                    .append("  Ожидалось: ").append(expectedTerm).append("\n")
                    .append("  На сайте:   ").append(currentTerm).append("\n\n");
        }

        if (diffLog.length() > 0) {
            String message = "⚠️ Условия на сайте отличаются от эталона!\n\n" + diffLog.toString();
            System.out.println(message);
            Allure.step(message);
        } else {
            String message = "✅ Изменений нет. Условия кредитования соответствуют эталону.";
            System.out.println(message);
            Allure.step(message);
        }
    }
}