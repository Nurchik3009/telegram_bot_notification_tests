package example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HalykCreditMonitorTest {

    private final String BOT_TOKEN = "8986752688:AAEb5JZaQ9c1OwK0EnxQZ-K1sxF8Sskgb2g";
    private final String CHAT_ID = "848405839";

    @Test
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

        StringBuilder message = new StringBuilder();

        if (!currentSum.equals(expectedSum)) {
            message.append("⚠ *Halyk Bank: Изменилась сумма кредита!* \n")
                    .append("Ожидалось: ").append(expectedSum).append("\n")
                    .append("На сайте: ").append(currentSum).append("\n\n");
        }

        if (!currentTerm.equals(expectedTerm)) {
            message.append("⚠ *Halyk Bank: Изменился срок кредитования!* \n")
                    .append("Ожидалось: ").append(expectedTerm).append("\n")
                    .append("На сайте: ").append(currentTerm).append("\n\n");
        }

        if (message.length() > 0) {
            sendTelegramNotification(message.toString());
            System.out.println("Успех: Обнаружены изменения условий на сайте. Уведомление успешно отправлено в Telegram.");
        } else {
            System.out.println("Изменений нет. Условия кредитования соответствуют эталону.");
        }
    }

    private void sendTelegramNotification(String text) {
        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

        String jsonPayload = String.format("{\"chat_id\": \"%s\", \"text\": \"%s\"}",
                CHAT_ID, text.replace("\n", "\\n"));

        java.net.Proxy socksProxy = new java.net.Proxy(
                java.net.Proxy.Type.SOCKS,
                new java.net.InetSocketAddress("proxy.qaguru.school", 7777)
        );

        java.net.ProxySelector proxySelector = new java.net.ProxySelector() {
            @Override
            public java.util.List<java.net.Proxy> select(java.net.URI uri) {
                return java.util.Collections.singletonList(socksProxy);
            }

            @Override
            public void connectFailed(java.net.URI uri, java.net.SocketAddress sa, java.io.IOException ioe) {
                System.err.println("Ошибка подключения через SOCKS-прокси: " + ioe.getMessage());
            }
        };

        HttpClient client = HttpClient.newBuilder()
                .proxy(proxySelector)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Статус отправки в Telegram: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при отправке уведомления: " + e.toString());
        }
    }
}