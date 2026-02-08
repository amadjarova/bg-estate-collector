package dev.sisi.service;

import dev.sisi.model.PropertyListing;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class CsvExporterTest {

    private final CsvExporter csvExporter = new CsvExporter();

    @Test
    void saveToCsv_validListings_returnsFileNameWithCorrectTimestamp() throws IOException {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 1, 20, 14, 35, 8);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);

            String fileName = csvExporter.saveToCsv(List.of());

            assertEquals("estates_20260120_143508.csv", fileName);
            Files.deleteIfExists(new File(fileName).toPath());
        }
    }

    @Test
    void saveToCsv_withData_writesUtf8BomAndCorrectContent() throws IOException {
        var listing = createSampleListing();
        String fileName = null;

        try {
            fileName = csvExporter.saveToCsv(List.of(listing));
            File file = new File(fileName);
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            assertEquals((byte) 0xEF, fileBytes[0]);
            assertEquals((byte) 0xBB, fileBytes[1]);
            assertEquals((byte) 0xBF, fileBytes[2]);

            String content = new String(fileBytes, 3, fileBytes.length - 3, StandardCharsets.UTF_8);
            String[] lines = content.split("\\R");

            assertTrue(lines.length >= 2, "File should have at least a header and one data row");
            assertTrue(lines[1].contains("Mladost"), "Data row should contain the district");
            assertTrue(lines[1].contains("250000"), "Data row should contain the price");
        } finally {
            if (fileName != null) Files.deleteIfExists(new File(fileName).toPath());
        }
    }

    @Test
    void saveToCsv_emptyList_createsFileWithOnlyHeader() throws IOException {
        String fileName = csvExporter.saveToCsv(List.of());
        File file = new File(fileName);

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(1, lines.size(), "Empty list should result in a file with only the header line");
        Files.deleteIfExists(file.toPath());
    }

    private PropertyListing createSampleListing() {
        return new PropertyListing(
                "Mladost", 3, 250000, 110, 5, 8,
                false, true, true, true,
                "Brick", 2025, true, true,
                "Spacious apartment", "https://imot.bg/listing123"
        );
    }
}
