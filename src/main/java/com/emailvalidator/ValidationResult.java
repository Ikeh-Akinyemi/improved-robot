package com.emailvalidator;

public class ValidationResult {
    private final String emailAddress;
    private String result;
    private String risk;
    private String reason;
    private boolean isDisposable;
    private boolean isRole;

    public ValidationResult(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isSafeForSending() {
        // Rule 1: Always exclude 'undeliverable'.
        if ("undeliverable".equals(result)) {
            return false;
        }

        // Rule 2: Always exclude 'high' risk addresses.
        if ("high".equals(risk)) {
            return false;
        }
        
        // Rule 3: Exclude disposable emails to avoid temporary signups.
        if (isDisposable) {
            return false;
        }

        // If none of the removal rules match, consider the address safe.
        // This includes 'deliverable' and 'risky' (with medium/low risk) addresses.
        return true; 
    }

    public String getEmailAddress() { return emailAddress; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRisk() { return risk; }
    public void setRisk(String risk) { this.risk = risk; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public boolean isDisposable() { return isDisposable; }
    public void setDisposable(boolean disposable) { isDisposable = disposable; }
    public boolean isRole() { return isRole; }
    public void setRole(boolean role) { isRole = role; }
}