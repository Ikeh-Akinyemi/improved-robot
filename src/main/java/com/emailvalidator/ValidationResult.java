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
        
        // Rule 3: Exclude disposable emails.
        if (isDisposable) {
            return false;
        }

        // Rule 4: 'deliverable' with 'low' risk is always safe.
        if ("deliverable".equals(result) && "low".equals(risk)) {
            return true;
        }
        
        // Rule 5: By default, consider 'deliverable' and 'risky' addresses as safe,
        // as long as they don't violate the stricter rules above.
        return "deliverable".equals(result) || "risky".equals(result);
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