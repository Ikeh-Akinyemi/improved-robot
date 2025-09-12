package com.emailvalidator;

import com.mailgun.api.v4.MailgunEmailVerificationApi;
import com.mailgun.client.MailgunClient;

public class MailingListValidator {
  private final MailgunEmailVerificationApi validationApi;

  public MailingListValidator(String apiKey) {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      System.err.println("❌ Mailgun API key is missing. Please set the MAILGUN_API_KEY environment variable.");
      this.validationApi = null;
    } else {
      // This creates a dedicated client for the v4 Email Verification API
      this.validationApi = MailgunClient.config(apiKey)
          .createApi(MailgunEmailVerificationApi.class);
      System.out.println("✅ Mailgun API client initialized.");
    }
  }

  public MailgunEmailVerificationApi getValidationApi() {
    if (validationApi == null) {
      throw new IllegalStateException("Mailgun API client is not available. Check API key.");
    }
    return validationApi;
  }

  public boolean isClientAvailable() {
    return this.validationApi != null;
  }
}