// src/main/java/com/emailvalidator/EmailRecord.java
package com.emailvalidator;

import java.util.HashMap;
import java.util.Map;

public class EmailRecord {
    private final String email;
    private final Map<String, String> metadata;
    private ValidationResult validationResult;
    
    public EmailRecord(String email) {
        this.email = email.toLowerCase().trim();
        this.metadata = new HashMap<>();
    }
    
    public EmailRecord(String email, Map<String, String> metadata) {
        this.email = email.toLowerCase().trim();
        this.metadata = new HashMap<>(metadata);
    }
    
    public String getEmail() { return email; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
    public ValidationResult getValidationResult() { return validationResult; }
    public void setValidationResult(ValidationResult result) { this.validationResult = result; }
    
    public boolean shouldKeep() {
        if (validationResult == null) {
            return false;
        }
        return ("deliverable".equals(validationResult.getResult()) || 
                ("catch_all".equals(validationResult.getResult()) && 
                 !"high".equals(validationResult.getRisk())));
    }
    
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
}