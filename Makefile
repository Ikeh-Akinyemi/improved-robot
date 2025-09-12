# Makefile for the Mailgun Email Validator Java Project

# Variables
MVN := mvn
MAIN_CLASS := com.emailvalidator.EmailValidatorApp

# Define a variable for the CSV file path.
# It can be overridden from the command line.
# Example: make run FILE=path/to/another.csv
FILE ?= data/input/test-emails.csv

# Default target
.PHONY: all
all: build

# Build the project
.PHONY: build
build:
	@echo "Building the project with Maven..."
	@$(MVN) clean package

# Run the application with the specified CSV file
.PHONY: run
run: build
	@echo "Running the application..."
	@echo "Make sure the MAILGUN_API_KEY environment variable is set."
	@echo "Using input file: $(FILE)"
	@$(MVN) exec:java -Dexec.mainClass="$(MAIN_CLASS)" -Dexec.args="$(FILE)"

# Clean the project
.PHONY: clean
clean:
	@echo "Cleaning the project (removing the target/ directory)..."
	@$(MVN) clean

# Run tests
.PHONY: test
test:
	@echo "Running tests..."
	@$(MVN) test

# Display help message
.PHONY: help
help:
	@echo "Usage: make [target] [VARIABLE=value]"
	@echo ""
	@echo "Targets:"
	@echo "  all        Builds the project (default target)."
	@echo "  build      Cleans the project and creates a new JAR package."
	@echo "  run        Builds and runs the main application."
	@echo "  clean      Removes all generated build files."
	@echo "  test       Runs the unit tests for the project."
	@echo "  help       Shows this help message."
	@echo ""
	@echo "Variables:"
	@echo "  FILE       Path to the input CSV file. Default: $(FILE)"
	@echo "             Example: make run FILE=my_emails.csv"