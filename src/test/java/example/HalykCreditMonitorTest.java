package example;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;

public class HalykCreditMonitorTest {

    private final String BOT_TOKEN = "8986752688:AAEb5JZaQ9c1OwK0EnxQZ-K1sxF8Sskgb2g";
    private final String CHAT_ID = "848405839";

    @Test
    void monitorHalykCreditTerms() {
        Configuration.browser = "chrome";
        Configuration.headless = true;

        open("https://halykbank.kz/business/credit/biznes-kredit");

        String currentSum = $x("//td[text()='Сумма']/following-sibling::td").getText().trim();
        String currentTerm = $x("//td[text()='Срок кредитования']/following-sibling::td").getText().trim();

        String expectedSum = "до 30 000 000 ₸";
        String expectedTerm = "от 1 до 26 месяцев";

        StringBuilder message = new StringBuilder();

        if (!currentSum.equals(expectedSum)) {
            message.append("⚠ **Halyk Bank: Изменилась сумма кредита!**\n")
                    .append("Ожидалось: ").append(expectedSum).append("\n")
                    .append("На сайте сейчас: ").append(currentSum).append("\n\n");
        }

        if (!currentTerm.equals(expectedTerm)) {
            message.append("⚠ **Halyk Bank: Изменился срок кредитования!**\n")
                    .append("Ожидалось: ").append(expectedTerm).append("\n")
                    .append("На сайте сейчас: ").append(currentTerm).append("\n\n");
        }

        if (message.length() > 0) {
            sendTelegramNotification(message.toString());
        } else {
            System.out.println("Изменений нет. Условия кредитования соответствуют эталону.");
        }
    }

    private void sendTelegramNotification(String text) {
        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

        String jsonPayload = String.format("{\"chat_id\": \"%s\", \"text\": \"%s\"}",
                CHAT_ID, text.replace("\n", "\\n"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Статус отправки в Telegram: " + response.statusCode());
            System.out.println("Ответ сервера Telegram: " + response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при отправке уведомления: " + e.getMessage());
        }
    }
}