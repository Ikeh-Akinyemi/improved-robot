# Mailgun Bulk Email List Validator

This is a command-line Java application that uses the Mailgun v4 API to perform bulk validation on a list of email addresses provided in a CSV file. The application handles the entire asynchronous workflow, including uploading the list, polling for job completion, downloading and parsing the results, and saving a final, cleaned list of deliverable email addresses.

This project serves as the working prototype for the accompanying Draft.dev article on the same topic.

## Features

-   Loads a list of email addresses from a specified CSV file.
-   Uses the official Mailgun Java SDK (v2.1.0).
-   Implements the full asynchronous v4 Bulk Validation API workflow:
    -   Creates a validation job by uploading the email list.
    -   Polls the API until the validation job is complete.
    -   Downloads and decompresses the zipped CSV results file.
-   Parses the results CSV defensively, handling potential variations in column order.
-   Filters the list based on deliverability `result` and `risk` score.
-   Generates a summary report in the console.
-   Saves a new CSV file (`cleaned_mailing_list.csv`) containing only the email addresses deemed safe to send.

## Requirements

To build and run this project, you will need:

-   **Java Development Kit (JDK)**: Version 11 or higher.
-   **Apache Maven**: To compile the project and manage dependencies.
-   **A Mailgun Account**:
    -   You must have a paid plan, as email validation is a premium feature.
    -   Your account must have the **Email Validation services activated**. If your account is new, it may be in an evaluation period, and you will need to contact Mailgun support to enable the feature.
    -   Your private **Mailgun API Key**.

## Setup

1.  **Clone the Repository**
    ```bash
    git clone git@github.com:Ikeh-Akinyemi/improved-robot.git
    cd improved-robot
    ```

2.  **Set Your Mailgun API Key**
    The application reads your API key from an environment variable named `MAILGUN_API_KEY`. You must set this variable in your terminal session before running the application.

    ```bash
    # Replace 'YOUR_PRIVATE_API_KEY' with your actual key from the Mailgun dashboard
    export MAILGUN_API_KEY='YOUR_PRIVATE_API_KEY'
    ```

3.  **Build the Project**
    Use the provided `Makefile` or run the Maven command directly to compile the source code and package it into an executable JAR file.

    *Using Make:*
    ```bash
    make build
    ```

    *Using Maven directly:*
    ```bash
    mvn clean package
    ```
    This will create the JAR file in the `target/` directory.

## Usage

You can run the application using either the `Makefile` shortcut or the standard `java -jar` command. You must provide the path to your input CSV file as a command-line argument.

**Input CSV Format:** The input CSV file must contain a header row with a column named `email`.

*Using the Makefile:*
The `Makefile` is configured to use the file at `data/input/test-emails.csv` by default.
```bash
make run
````

To specify a different file, you can override the `INPUT_FILE` variable:

```bash
make run INPUT_FILE=path/to/my/other-list.csv
```

*Using the `java -jar` command:*

```bash
java -jar target/mailgun-list-validator-1.0.0.jar path/to/your/list.csv
```

For example, to run it with the sample data provided:

```bash
java -jar target/mailgun-list-validator-1.0.0.jar data/input/test-emails.csv
```

## Example Output

A successful run will produce output similar to the following:

```
✅ Mailgun API client initialized.
📧 Loaded 7 email addresses from data/input/test-emails.csv
⚙️  Creating bulk validation job with ID: validation-job-1757651646221
📤 The validation job was submitted.
⏳ Waiting for validation job to complete...
[Poll #1] Status: 'processing'
[Poll #2] Status: 'processing'
[Poll #3] Status: 'uploaded'

✅ Job completed status detected!
📥 Downloading validation results...
📊 Parsed 7 validation results.
🗑️  Cleaned up local temp file.

--- Validation Report ---
Total Addresses Processed: 7
✅ Safe to Send: 1 (14.3%)
❌ Removed: 6 (85.7%)
-------------------------
💾 Successfully saved 1 safe email addresses to cleaned_mailing_list.csv
```

## Project Structure

  - `EmailValidatorApp.java`: The main entry point of the application. Orchestrates the workflow.
  - `MailingListValidator.java`: Handles the setup and configuration of the Mailgun API client.
  - `MailingListLoader.java`: Responsible for reading and parsing the input CSV file.
  - `BulkValidationProcessor.java`: Contains the core logic for creating, polling, and processing the bulk validation job.
  - `ValidationResult.java`: A data model class that holds the parsed validation results for a single email. Contains the `isSafeForSending()` filtering logic.
  - `FilteredMailingList.java`: A container class that categorizes the results and generates the final report.
