package com.emailvalidator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmailValidatorApp {

    public static void main(String[] args) {
        // Step 1: Get API Key and Input File Path
        String apiKey = System.getenv("MAILGUN_API_KEY");
        if (args.length == 0) {
            System.err.println("Usage: java -jar <jar-file> <path-to-your-csv-file>");
            return;
        }
        String csvFilePath = args[0];

        // Step 2: Initialize components
        MailingListValidator validator = new MailingListValidator(apiKey);
        if (!validator.isClientAvailable()) {
            return; // Exit if the client couldn't be initialized
        }
        
        MailingListLoader loader = new MailingListLoader();
        BulkValidationProcessor processor = new BulkValidationProcessor(validator);

        try {
            // Step 3: Load emails from CSV
            List<String> emailsToValidate = loader.loadFromCsv(csvFilePath);
            if (emailsToValidate.isEmpty()) {
                System.out.println("No valid email addresses found in the file to validate.");
                return;
            }

            // Step 4: Run the bulk validation process
            Map<String, ValidationResult> validationResults = processor.validateEmailList(emailsToValidate);

            // Step 5: Filter the results
            FilteredMailingList filteredList = new FilteredMailingList();
            validationResults.forEach((email, result) -> filteredList.addAddress(email, result));
            
            // Step 6: Generate a report and save the cleaned list
            filteredList.generateReport();
            saveCleanedList(filteredList.getSafeAddresses(), "cleaned_mailing_list.csv");

        } catch (Exception e) {
            System.err.println("\n❌ An error occurred during the validation process:");
            e.printStackTrace();
        }
    }

    private static void saveCleanedList(List<String> cleanedAddresses, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("email");
            writer.newLine();
            for (String email : cleanedAddresses) {
                writer.write(email);
                writer.newLine();
            }
        }
        System.out.printf("💾 Successfully saved %d safe email addresses to %s%n", cleanedAddresses.size(), outputPath);
    }
}