// src/main/java/com/emailvalidator/ValidationDetails.java
package com.emailvalidator;

public class ValidationDetails {
    private String result;
    private String risk;
    private String reason;
    
    public ValidationDetails(String result, String risk, String reason) {
        this.result = result;
        this.risk = risk;
        this.reason = reason;
    }
    
    public String getResult() { return result; }
    public String getRisk() { return risk; }
    public String getReason() { return reason; }
}