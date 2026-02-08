package dev.sisi.service;

import dev.sisi.model.PropertyListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class EstateCrawlerService {

    private final EstateParserService parserService;
    private final Executor executor;
    private final Set<Long> processedIds = ConcurrentHashMap.newKeySet();
    private static final String BASE_URL = "https://www.imot.bg";

    public EstateCrawlerService(EstateParserService parserService,
                                @Qualifier("crawlerExecutor") Executor executor) {
        this.parserService = parserService;
        this.executor = executor;
    }

    public List<PropertyListing> crawl(String startUrl, int maxPages) {
        List<PropertyListing> allResults = Collections.synchronizedList(new ArrayList<>());
        String nextUrl = startUrl;
        int currentPage = 1;

        while (nextUrl != null && currentPage <= maxPages) {
            try {
                String html = parserService.fetchHtml(nextUrl);
                Document doc = Jsoup.parse(html);

                Set<String> adLinks = extractAdLinks(doc);

                List<CompletableFuture<Void>> futures = adLinks.stream()
                        .map(link -> CompletableFuture.runAsync(() -> processLink(link, allResults), executor))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                nextUrl = findNextPageUrl(doc);
                currentPage++;

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return allResults;
    }

    private void processLink(String link, List<PropertyListing> allResults) {
        long id = extractIdFromUrl(link);
        if (processedIds.add(id)) {
            try {
                PropertyListing listing = parserService.parse(link);
                allResults.add(listing);
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Set<String> extractAdLinks(Document doc) {
        Set<String> links = new HashSet<>();
        Elements items = doc.select("div.item, table.search_results");
        Elements allPotentalLinks = items.select("a[href*=/obiava-]");

        for (Element el : allPotentalLinks) {
            String href = el.attr("href");
            if (!href.contains("act=14") && !isInsideForbiddenContainer(el)) {
                links.add(formatUrl(href));
            }
        }
        return links;
    }

    private boolean isInsideForbiddenContainer(Element el) {
        Element parent = el.parent();
        while (parent != null) {
            String className = parent.className();
            if (className.contains("iMenu") || className.contains("submenu") || parent.tagName().equalsIgnoreCase("section")) {
                return true;
            }
            parent = parent.parent();
        }
        return false;
    }

    private String findNextPageUrl(Document doc) {
        Element nextBtn = doc.selectFirst("a.next");
        if (nextBtn != null && !isInsideForbiddenContainer(nextBtn)) {
            return formatUrl(nextBtn.attr("href"));
        }
        return null;
    }

    private String formatUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("//")) return "https:" + url;
        if (url.startsWith("/")) return BASE_URL + url;
        return url;
    }

    private long extractIdFromUrl(String url) {
        Matcher m = java.util.regex.Pattern.compile("(\\d{10,})").matcher(url);
        return m.find() ? Long.parseLong(m.group(1)) : (long) url.hashCode();
    }
}