package com.eme22.bolo.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    private static final Logger log = LoggerFactory.getLogger(SearchUtils.class);

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 6000;

    public static class SearchResult {
        private final String title;
        private final String url;
        private final String snippet;

        public SearchResult(String title, String url, String snippet) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getSnippet() { return snippet; }

        @Override
        public String toString() {
            return String.format("- **%s**\n  URL: %s\n  Resumen: %s\n", title, url, snippet);
        }
    }

    /**
     * Performs a web search using Google with fallback to DuckDuckGo if Google is blocked or fails.
     *
     * @param query The search term.
     * @param limit The maximum number of results to return.
     * @return A formatted string of results.
     */
    public static String performSearch(String query, int limit) {
        log.info("Iniciando búsqueda web para: '{}' (límite: {})", query, limit);
        List<SearchResult> results = new ArrayList<>();
        String engineUsed = "Google";

        try {
            results = scrapeGoogle(query, limit);
            if (results.isEmpty()) {
                throw new IOException("Google no devolvió ningún resultado div.g (posible CAPTCHA o bloqueo).");
            }
        } catch (Exception e) {
            log.warn("La búsqueda en Google falló ({}). Intentando fallback con DuckDuckGo...", e.getMessage());
            engineUsed = "DuckDuckGo";
            try {
                results = scrapeDuckDuckGo(query, limit);
            } catch (Exception ex) {
                log.error("La búsqueda de fallback en DuckDuckGo también falló.", ex);
                return "❌ No se pudo completar la búsqueda en internet. Ambos motores de búsqueda (Google y DuckDuckGo) fallaron o bloquearon la solicitud.";
            }
        }

        if (results.isEmpty()) {
            return "No se encontraron resultados en internet para: \"" + query + "\"";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### Resultados de Búsqueda Web (Motor: ").append(engineUsed).append("):\n\n");
        for (SearchResult result : results) {
            sb.append(result.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    private static List<SearchResult> scrapeGoogle(String query, int limit) throws IOException {
        String url = "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();

        Elements elements = doc.select("div.g");
        List<SearchResult> list = new ArrayList<>();

        for (Element el : elements) {
            if (list.size() >= limit) {
                break;
            }

            Element titleEl = el.selectFirst("h3");
            Element linkEl = el.selectFirst("a[href]");
            Element snippetEl = el.selectFirst(".VwiC3b, .yDYNvb, .MUxGfe, .lEBKkf");

            if (titleEl != null && linkEl != null) {
                String title = titleEl.text();
                String href = linkEl.attr("abs:href");
                String snippet = snippetEl != null ? snippetEl.text() : "";

                // Skip google specific/internal links
                if (href.startsWith("http") && !href.contains("google.com/")) {
                    list.add(new SearchResult(title, href, snippet));
                }
            }
        }

        return list;
    }

    private static List<SearchResult> scrapeDuckDuckGo(String query, int limit) throws IOException {
        String url = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();

        Elements elements = doc.select(".result");
        List<SearchResult> list = new ArrayList<>();

        for (Element el : elements) {
            if (list.size() >= limit) {
                break;
            }

            Element titleLinkEl = el.selectFirst("a.result__a");
            Element snippetEl = el.selectFirst(".result__snippet");

            if (titleLinkEl != null) {
                String title = titleLinkEl.text();
                String href = titleLinkEl.attr("abs:href");
                String snippet = snippetEl != null ? snippetEl.text() : "";

                // Clean DuckDuckGo redirect link
                href = cleanDdgUrl(href);

                if (href.startsWith("http")) {
                    list.add(new SearchResult(title, href, snippet));
                }
            }
        }

        return list;
    }

    private static String cleanDdgUrl(String url) {
        if (url != null && url.contains("uddg=")) {
            try {
                int idx = url.indexOf("uddg=");
                String uddg = url.substring(idx + 5);
                int ampIdx = uddg.indexOf('&');
                if (ampIdx != -1) {
                    uddg = uddg.substring(0, ampIdx);
                }
                return URLDecoder.decode(uddg, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("No se pudo decodificar el enlace redireccionado de DuckDuckGo: {}", url, e);
            }
        }
        return url;
    }
}
