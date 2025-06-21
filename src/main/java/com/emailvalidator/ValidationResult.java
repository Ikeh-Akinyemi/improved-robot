// src/main/java/com/emailvalidator/ValidationResult.java
package com.emailvalidator;

public class ValidationResult {
    private String result;
    private String risk;
    private String reason;
    
    public ValidationResult(String result, String risk, String reason) {
        this.result = result;
        this.risk = risk;
        this.reason = reason;
    }
    
    public String getResult() { return result; }
    public String getRisk() { return risk; }
    public String getReason() { return reason; }
}