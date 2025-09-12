package com.emailvalidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilteredMailingList {

    private final List<String> safeAddresses = new ArrayList<>();
    private final List<String> removedAddresses = new ArrayList<>();
    private final Map<String, ValidationResult> allResults = new HashMap<>();

    public void addAddress(String email, ValidationResult result) {
        allResults.put(email, result);
        if (result.isSafeForSending()) {
            safeAddresses.add(email);
        } else {
            removedAddresses.add(email);
        }
    }
    
    public void generateReport() {
        int total = safeAddresses.size() + removedAddresses.size();
        if (total == 0) {
            System.out.println("No addresses processed.");
            return;
        }
        
        double safePercentage = (double) safeAddresses.size() / total * 100;

        System.out.println("\n--- Validation Report ---");
        System.out.printf("Total Addresses Processed: %d%n", total);
        System.out.printf("✅ Safe to Send: %d (%.1f%%)%n", safeAddresses.size(), safePercentage);
        System.out.printf("❌ Removed: %d (%.1f%%)%n", removedAddresses.size(), 100 - safePercentage);
        System.out.println("-------------------------");
    }

    public List<String> getSafeAddresses() {
        return safeAddresses;
    }

    public List<String> getRemovedAddresses() {
        return removedAddresses;
    }
}