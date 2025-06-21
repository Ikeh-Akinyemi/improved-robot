package com.emailvalidator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;

public class EmailValidatorApp {

    public static void main(String[] args) {
        try {
            EmailListValidator validator = new EmailListValidator();
            validator.testMailgunConnection();
            validator.testMailgunConnection();

            Scanner scanner = new Scanner(System.in);

            System.out.println("=== BASIC EMAIL LIST LOADER ===");

            // Test basic functionality first
            validator.testBasicFunctionality();

            System.out.print("Enter CSV file path (or 'test' to use test data): ");
            String filePath = scanner.nextLine().trim();

            if (filePath.isEmpty()) {
                System.out.println("No file specified. Exiting.");
                return;
            }

            // If user types 'test', create a simple test file
            if ("test".equals(filePath)) {
                createTestCsvFile();
                filePath = "test-emails.csv";
            }

            // Load emails
            List<EmailRecord> emails = validator.loadEmailsFromCsv(filePath);

            System.out.println("\n=== TESTING EMAIL VALIDATION ===");

            // Test validation on a few different types of emails from our loaded list
            if (!emails.isEmpty()) {
                // // Test the first email (should be a normal address)
                // validator.validateSingleEmail(emails.get(0).getEmail());

                // // Test an obviously invalid email if we have one
                // for (EmailRecord email : emails) {
                //     if (!email.getEmail().contains("@") || email.getEmail().contains("invalid")) {
                //         System.out.println();
                //         validator.validateSingleEmail(email.getEmail());
                //         break;
                //     }
                // }

                // Test different types of validation endpoints to understand access patterns
                validator.testBulkValidationAccess();
                System.out.println(); // Add spacing for readability

                validator.testBulkPreviewAccess();
                System.out.println(); // Add spacing for readability

                // Only test single email validation if bulk services seem to be working
                System.out.println("Testing single email validation...");
                validator.validateSingleEmail(emails.get(0).getEmail());
            }

            // Display results
            System.out.println("\nSuccessfully loaded " + emails.size() + " email addresses:");
            for (int i = 0; i < Math.min(5, emails.size()); i++) {
                EmailRecord email = emails.get(i);
                System.out.println("  " + (i + 1) + ". " + email.getEmail());
                if (!email.getMetadata().isEmpty()) {
                    System.out.println("     Metadata: " + email.getMetadata());
                }
            }

            if (emails.size() > 5) {
                System.out.println("  ... and " + (emails.size() - 5) + " more emails");
            }

        } catch (Exception e) {
            System.err.println("Application failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTestCsvFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("test-emails.csv"))) {
            writer.println("email,name,source");
            writer.println("john.doe@example.com,John Doe,website");
            writer.println("jane.smith@gmail.com,Jane Smith,newsletter");
            writer.println("bob.jones@company.com,Bob Jones,referral");
            writer.println("alice.brown@outlook.com,Alice Brown,social");
        }
        System.out.println("Created test-emails.csv with sample data");
    }
}