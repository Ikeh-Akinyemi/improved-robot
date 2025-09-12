package com.emailvalidator;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MailingListLoader {

    public List<String> loadFromCsv(String filePath) {
        List<String> emails = new ArrayList<>();
        int emailColumnIndex = 0; // Assuming the first column is 'email'

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Read header to find the email column (case-insensitive)
            String[] headers = reader.readNext();
            if (headers != null) {
                for (int i = 0; i < headers.length; i++) {
                    if ("email".equalsIgnoreCase(headers[i].trim())) {
                        emailColumnIndex = i;
                        break;
                    }
                }
            } else {
                 throw new IOException("CSV file is empty or has no header.");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length > emailColumnIndex) {
                    String email = line[emailColumnIndex].trim().toLowerCase();
                    if (!email.isEmpty() && email.contains("@")) {
                        emails.add(email);
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to load mailing list from CSV: " + filePath, e);
        }

        System.out.printf("📧 Loaded %d email addresses from %s%n", emails.size(), filePath);
        return emails;
    }
}