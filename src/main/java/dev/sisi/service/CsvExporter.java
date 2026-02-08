package dev.sisi.service;

import dev.sisi.model.PropertyListing;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExporter {

    public String saveToCsv(List<PropertyListing> listings) throws IOException {
        String fileName = "estates_raw_data.csv";

        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(PropertyListing.headersFromRecord())
                    .build();

            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (var p : listings) {
                    printer.printRecord(
                            p.district(),
                            p.rooms(),
                            p.price(),
                            p.area(),
                            p.floorNumber(),
                            p.totalFloors(),
                            p.isFirstFloor() ? 1 : 0,
                            p.isLastFloor() ? 1 : 0,
                            p.hasGas() ? 1 : 0,
                            p.hasTec() ? 1 : 0,
                            p.constructionType(),
                            p.constructionYear(),
                            p.hasGarage() ? 1 : 0,
                            p.isClosedComplex() ? 1 : 0,
                            p.description(),
                            p.url()
                    );
                }
            }
        }

        return fileName;
    }
}