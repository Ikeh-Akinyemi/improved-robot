package com.emailvalidator;

import com.mailgun.api.v4.MailgunEmailVerificationApi;
import com.mailgun.model.verification.BulkVerificationCreatingResponse;
import com.mailgun.model.verification.BulkVerificationJobStatusResponse;
import com.mailgun.model.verification.BulkVerificationStatusRequest;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

public class BulkValidationProcessor {

    private final MailgunEmailVerificationApi validationApi;
    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final int POLL_INTERVAL_SECONDS = 10;

    public BulkValidationProcessor(MailingListValidator validator) {
        this.validationApi = validator.getValidationApi();
    }

    public Map<String, ValidationResult> validateEmailList(List<String> emails) throws Exception {
        File emailListFile = createTempEmailFile(emails);
        String listId = "validation-job-" + System.currentTimeMillis();
        System.out.println("⚙️  Creating bulk validation job with ID: " + listId);

        BulkVerificationStatusRequest request = BulkVerificationStatusRequest.builder()
            .file(emailListFile)
            .build();
        
        BulkVerificationCreatingResponse creationResponse = validationApi.createBulkVerificationJob(listId, request);
        System.out.println("📤 " + creationResponse.getMessage());

        BulkVerificationJobStatusResponse completedJob = pollForCompletion(listId);
        
        String csvDownloadUrl = completedJob.getDownloadUrl().getCsv();
        System.out.println("📥 Downloading validation results...");
        Map<String, ValidationResult> resultMap = downloadAndParseResults(csvDownloadUrl);
        
        // The local temporary file is no longer needed.
        emailListFile.delete();
        System.out.println("🗑️  Cleaned up local temp file.");

        return resultMap;
    }

    private BulkVerificationJobStatusResponse pollForCompletion(String listId) throws InterruptedException {
        System.out.println("⏳ Waiting for validation job to complete...");
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            if (attempt > 0) {
                TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
            }
            
            BulkVerificationJobStatusResponse status = validationApi.getBulkVerificationJobStatus(listId);

            System.out.printf("[Poll #%d] Status: '%s', Quantity: %d, Processed: %d%n", 
                attempt + 1, 
                status.getStatus(), 
                status.getQuantity(), 
                status.getRecordsProcessed());

            if ("uploaded".equalsIgnoreCase(status.getStatus())) {
                System.out.println("\n✅ Job completed status detected!");
                return status;
            } else if ("failed".equalsIgnoreCase(status.getStatus())) {
                throw new RuntimeException("Mailgun validation job failed with status: " + status.getStatus());
            }
        }
        throw new RuntimeException("Validation job timed out after " + (MAX_POLL_ATTEMPTS * POLL_INTERVAL_SECONDS) + " seconds.");
    }

    private Map<String, ValidationResult> downloadAndParseResults(String downloadUrl) throws IOException, CsvValidationException {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream connectionStream = connection.getInputStream();
             ZipInputStream zipStream = new ZipInputStream(connectionStream)) {
            
            zipStream.getNextEntry();
            
            try (InputStreamReader reader = new InputStreamReader(zipStream);
                 CSVReader csvReader = new CSVReader(reader)) {
                return parseResultsFromCsv(csvReader);
            }
        } finally {
            connection.disconnect();
        }
    }

    private Map<String, ValidationResult> parseResultsFromCsv(CSVReader csvReader) throws IOException, CsvValidationException {
        Map<String, ValidationResult> resultMap = new HashMap<>();
        
        String[] headers = csvReader.readNext();
        if (headers == null) {
            System.err.println("Downloaded results CSV is empty or has no header.");
            return resultMap;
        }

        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].trim().toLowerCase(), i);
        }

        if (!headerMap.containsKey("address")) {
            throw new IOException("Downloaded results CSV does not contain the required 'address' column.");
        }

        String[] line;
        while ((line = csvReader.readNext()) != null) {
            String email = line[headerMap.get("address")];
            ValidationResult result = new ValidationResult(email);

            if (headerMap.containsKey("result")) result.setResult(line[headerMap.get("result")]);
            if (headerMap.containsKey("risk")) result.setRisk(line[headerMap.get("risk")]);
            if (headerMap.containsKey("reason")) result.setReason(line[headerMap.get("reason")]);
            if (headerMap.containsKey("is_disposable_address")) result.setDisposable(Boolean.parseBoolean(line[headerMap.get("is_disposable_address")]));
            if (headerMap.containsKey("is_role_address")) result.setRole(Boolean.parseBoolean(line[headerMap.get("is_role_address")]));
            
            resultMap.put(email.toLowerCase(), result);
        }
        System.out.printf("📊 Parsed %d validation results.%n", resultMap.size());
        return resultMap;
    }

    private File createTempEmailFile(List<String> emails) throws IOException {
        File tempFile = File.createTempFile("mailgun-emails-", ".csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            writer.println("email");
            emails.forEach(writer::println);
        }
        return tempFile;
    }
}