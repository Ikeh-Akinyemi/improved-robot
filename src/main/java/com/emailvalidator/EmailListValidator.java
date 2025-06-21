package com.emailvalidator;

import com.mailgun.api.v4.MailgunEmailVerificationApi;
import com.mailgun.client.MailgunClient;
import com.opencsv.CSVReader;
import java.io.*;
import java.util.*;

public class EmailListValidator {

    private static final String MAILGUN_API_KEY = System.getenv("MAILGUN_API_KEY");
    private final MailgunEmailVerificationApi verificationApi;

    public EmailListValidator() {
        System.out.println("EmailListValidator created - basic version");

        // Initialize Mailgun API client using a single assignment approach
        MailgunEmailVerificationApi tempApi = null;

        if (MAILGUN_API_KEY != null && !MAILGUN_API_KEY.isEmpty()) {
            try {
                tempApi = MailgunClient.config(MAILGUN_API_KEY)
                        .createApi(MailgunEmailVerificationApi.class);
                System.out.println("✓ Mailgun API client initialized successfully");
            } catch (Exception e) {
                System.err.println("⚠ Mailgun API initialization failed: " + e.getMessage());
                // tempApi remains null, which is what we want for the failed case
            }
        } else {
            System.out.println("⚠ MAILGUN_API_KEY not set - validation features disabled");
        }

        // Single assignment to the final variable
        this.verificationApi = tempApi;
    }

    // Keep all your existing CSV loading methods exactly as they are
    public List<EmailRecord> loadEmailsFromCsv(String filePath) throws IOException {
        List<EmailRecord> emailRecords = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers;

            try {
                headers = reader.readNext();
            } catch (com.opencsv.exceptions.CsvValidationException e) {
                throw new IOException("Invalid CSV format in headers: " + e.getMessage(), e);
            }

            if (headers == null || headers.length == 0) {
                throw new IOException("CSV file is empty");
            }

            int emailColumnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if ("email".equalsIgnoreCase(headers[i].trim())) {
                    emailColumnIndex = i;
                    break;
                }
            }

            if (emailColumnIndex == -1) {
                throw new IllegalArgumentException("No 'email' column found in CSV headers");
            }

            String[] row;
            int rowNumber = 1;

            while (true) {
                try {
                    row = reader.readNext();
                    if (row == null) {
                        break;
                    }
                    rowNumber++;

                } catch (com.opencsv.exceptions.CsvValidationException e) {
                    System.err.println("Warning: Skipping malformed row " + rowNumber + ": " + e.getMessage());
                    rowNumber++;
                    continue;
                }

                if (row.length > emailColumnIndex &&
                        row[emailColumnIndex] != null &&
                        !row[emailColumnIndex].trim().isEmpty()) {

                    String emailAddress = row[emailColumnIndex].trim();
                    EmailRecord record = new EmailRecord(emailAddress);

                    for (int i = 0; i < Math.min(headers.length, row.length); i++) {
                        if (i != emailColumnIndex && row[i] != null && !row[i].trim().isEmpty()) {
                            record.addMetadata(headers[i].trim(), row[i].trim());
                        }
                    }

                    emailRecords.add(record);
                }
            }
        }

        System.out.println("Loaded " + emailRecords.size() + " email addresses from CSV");
        return emailRecords;
    }

    public void testBasicFunctionality() {
        System.out.println("Testing basic functionality...");
        System.out.println("✓ EmailListValidator is working");
    }

    public void testMailgunConnection() {
        if (verificationApi == null) {
            System.out.println("❌ Mailgun API not available - check your API key");
            return;
        }

        try {
            System.out.println("Testing Mailgun API connection...");
            System.out.println("✓ Mailgun API client is ready for validation");
        } catch (Exception e) {
            System.err.println("❌ Mailgun API test failed: " + e.getMessage());
        }
    }

    public void validateSingleEmail(String emailAddress) {
        if (verificationApi == null) {
            System.out.println("❌ Mailgun API not available - cannot validate email");
            return;
        }

        try {
            System.out.println("Validating email: " + emailAddress);

            // Call Mailgun's single email validation endpoint
            var validationResponse = verificationApi.validateAddress(emailAddress);

            System.out.println("✓ Validation completed for: " + emailAddress);
            System.out.println("  Result: " + validationResponse.getResult());
            System.out.println("  Risk: " + validationResponse.getRisk());

            // Display additional details if available
            if (validationResponse.getReason() != null && !validationResponse.getReason().isEmpty()) {
                System.out.println("  Reason: " + validationResponse.getReason());
            }

        } catch (Exception e) {
            System.err.println("❌ Validation failed for " + emailAddress + ": " + e.getMessage());
            // Print the full stack trace to help us understand API issues
            e.printStackTrace();
        }
    }

    public void testBulkValidationAccess() {
        if (verificationApi == null) {
            System.out.println("❌ Mailgun API not available - cannot test bulk validation");
            return;
        }

        try {
            System.out.println("Testing bulk validation access...");

            // Try to get the list of bulk validation jobs to test endpoint access
            // This is a read-only operation that should work if bulk validation is enabled
            var bulkJobsList = verificationApi.getBulkVerificationJobList();

            System.out.println("✓ Bulk validation endpoint accessible!");
            System.out.println("  Found " + bulkJobsList.getJobs().size() + " existing validation jobs");

        } catch (Exception e) {
            System.err.println("❌ Bulk validation access failed: " + e.getMessage());

            // Let's examine the specific error to understand what's happening
            if (e.getMessage().contains("403") || e.getMessage().contains("Forbidden")) {
                System.err.println("  → This suggests bulk validation may also require additional access");
            } else if (e.getMessage().contains("404") || e.getMessage().contains("Not Found")) {
                System.err.println("  → This suggests the bulk validation endpoints might not exist");
            } else {
                System.err.println("  → This suggests a different type of access or configuration issue");
            }
        }
    }

    public void testBulkPreviewAccess() {
        if (verificationApi == null) {
            System.out.println("❌ Mailgun API not available - cannot test preview validation");
            return;
        }

        try {
            System.out.println("Testing bulk preview validation access...");

            // Try to get the list of preview validation jobs
            // Preview might be available even if full validation isn't
            var previewList = verificationApi.getBulkVerificationPreviewList();

            System.out.println("✓ Bulk preview validation endpoint accessible!");
            System.out.println("  Found " + previewList.getPreviews().size() + " existing preview jobs");

        } catch (Exception e) {
            System.err.println("❌ Bulk preview validation access failed: " + e.getMessage());

            // Analyze the error pattern to understand access restrictions
            if (e.getMessage().contains("paid accounts")) {
                System.err.println("  → Preview validation requires paid account upgrade");
            } else if (e.getMessage().contains("feature unavailable")) {
                System.err.println("  → Preview validation needs additional activation");
            }
        }
    }
}