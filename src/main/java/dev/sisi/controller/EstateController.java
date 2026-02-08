package dev.sisi.controller;

import dev.sisi.service.CsvExporter;
import dev.sisi.service.EstateCrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/estates")
public class EstateController {

    private final CsvExporter csvExporter;
    private final EstateCrawlerService estateCrawlerService;

    public EstateController(CsvExporter csvExporter, EstateCrawlerService estateCrawlerService) {
        this.csvExporter = csvExporter;
        this.estateCrawlerService = estateCrawlerService;
    }

    @PostMapping("/export-csv")
    public ResponseEntity<Map<String, String>> exportEstatesToCsv(
            @RequestParam(name = "pages", defaultValue = "2") int pages)
            throws IOException {
        try {
            var estates = estateCrawlerService.crawl("https://www.imot.bg/obiavi/prodazhbi/grad-sofiya", pages);
            String fileName = csvExporter.saveToCsv(estates);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Data synchronized and exported to " + fileName
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "An error occurred during export."
            ));
        }
    }

}