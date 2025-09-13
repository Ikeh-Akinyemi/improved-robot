package com.emailvalidator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmailValidatorApp {

    public static void main(String[] args) {
        String apiKey = System.getenv("MAILGUN_API_KEY");
        if (args.length == 0) {
            System.err.println("Usage: java -jar <jar-file> <path-to-your-csv-file>");
            return;
        }
        String csvFilePath = args[0];

        MailingListValidator validator = new MailingListValidator(apiKey);
        if (!validator.isClientAvailable()) {
            return;
        }
        
        MailingListLoader loader = new MailingListLoader();
        BulkValidationProcessor processor = new BulkValidationProcessor(validator);

        try {
            List<String> emailsToValidate = loader.loadFromCsv(csvFilePath);
            if (emailsToValidate.isEmpty()) {
                System.out.println("No valid email addresses found in the file to validate.");
                return;
            }

            Map<String, ValidationResult> validationResults = processor.validateEmailList(emailsToValidate);

            // Filter the results
            FilteredMailingList filteredList = new FilteredMailingList();
            validationResults.forEach((email, result) -> filteredList.addAddress(email, result));
            
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