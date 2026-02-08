package dev.sisi.service;

import dev.sisi.model.PropertyListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EstateParserService {

    @Value("${crawler.user-agent}")
    private String userAgent;

    @Value("${crawler.connection-timeout-ms:5000}")
    private int timeout;

    private static final Pattern CLEAN_TEXT_PATTERN =
            Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36";

    public String fetchHtml(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .get()
                .html();
    }

    private String parseDistrict(String fullLocation) {
        if (fullLocation == null || fullLocation.isEmpty()) {
            return "Неизвестен";
        }
        String[] parts = fullLocation.split(",");
        String district = parts.length > 1 ? parts[1].trim() : fullLocation.trim();
        String districtOnly = district.split("ул\\.|бул\\.|улица|булевард|Ул\\.|к-с|-")[0].trim();
        return districtOnly.isEmpty() ? "Неизвестен" : districtOnly;
    }

    public PropertyListing parse(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
        String bodyText = doc.text();

        String fullLoc = doc.selectFirst(".location").text();
        String district = parseDistrict(fullLoc);

        int rooms = extractFirstInt(doc.select(".advHeader .title").text());

        Element priceDiv = doc.selectFirst("div.cena");
        int price = 0;
        if (priceDiv != null
                && !priceDiv.childNodes().isEmpty()
                && priceDiv.childNode(0) instanceof TextNode textNode) {
            String rawPrice = textNode.text().replaceAll("\\D", "");
            price = rawPrice.isEmpty() ? 0 : Integer.parseInt(rawPrice);
        }


        String areaContext = doc.title() + " " + doc.select(".adParams").text();
        int area = extractAreaStrict(areaContext);

        Element floorEl = doc.selectFirst(".adParams div:contains(Етаж) strong");
        int floor = 0;
        int totalFloors = 0;
        if (floorEl != null) {
            String fText = floorEl.text();
            floor = extractFirstInt(fText);
            totalFloors = extractLastInt(fText);
        }

        boolean isFirstFloor = (floor == 0 || floor == 1);
        boolean isLastFloor = (floor == totalFloors);

        Element constrEl = doc.selectFirst(".adParams div:contains(Строителство)");
        String constructionType = "Неизвестен";
        int constructionYear = 0;
        if (constrEl != null) {
            String rawConstr = constrEl.text().replace("Строителство:", "").trim();
            constructionType = rawConstr.contains(",") ? rawConstr.split(",")[0].trim() : rawConstr;
            constructionYear = extractFirstInt(rawConstr);
        }

        boolean hasGas = bodyText.contains("Газ: ДА");
        boolean hasTec = bodyText.contains("ТЕЦ: ДА");
        boolean hasGarage = bodyText.toLowerCase().contains("гараж") || bodyText.toLowerCase().contains("паркинг");
        boolean isClosedComplex = bodyText.contains("затворен комплекс");
        String description = extractDescription(doc);

        return new PropertyListing(
                district,
                rooms,
                price,
                area,
                floor,
                totalFloors,
                isFirstFloor,
                isLastFloor,
                hasGas,
                hasTec,
                constructionType,
                constructionYear,
                hasGarage,
                isClosedComplex,
                description,
                url
        );
    }

    private int extractAreaStrict(String text) {
        Matcher m = Pattern.compile("(\\d+)\\s*кв\\.м").matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private int extractFirstInt(String text) {
        if (text == null) return 0;
        Matcher m = Pattern.compile("(\\d+)").matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private int extractLastInt(String text) {
        if (text == null) return 0;
        Matcher m = Pattern.compile("(\\d+)").matcher(text);
        int last = 0;
        while (m.find()) {
            last = Integer.parseInt(m.group(1));
        }
        return last;
    }

    private String extractDescription(Document doc) {
        Element header = doc.selectFirst("h2:contains(Описание на имота)");
        if (header != null) {
            Element descDiv = header.closest(".moreInfo").selectFirst(".text");
            if (descDiv != null) {
                String html = descDiv.html();
                return cleanText(Jsoup.parse(html.replaceAll("(?i)<br\\s*/?>", "\n")).text().trim());
            }
        }

        return "";
    }

    private String cleanText(String input) {
        if (input == null) {
            return "";
        }
        String cleaned = CLEAN_TEXT_PATTERN.matcher(input).replaceAll("");
        return cleaned.replaceAll("\\s+", " ").trim();
    }
}