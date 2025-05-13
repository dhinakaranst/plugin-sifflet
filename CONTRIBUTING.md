# Contributing to Kestra Sifflet Plugin

Thank you for your interest in contributing to the Kestra Sifflet Plugin! This document provides guidelines and instructions for contributing to this project.

## Prerequisites

- Java 21
- Docker (for testing)
- A Sifflet account with API access

## Setting Up the Development Environment

1. Clone the repository:
   ```bash
   git clone https://github.com/kestra-io/plugin-sifflet.git
   cd plugin-sifflet
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

## Running Tests

To run the tests, you'll need to set the following environment variables:

- `SIFFLET_API_URL`: The base URL of the Sifflet API (e.g., https://tenant.siffletdata.com/api/v1)
- `SIFFLET_ACCESS_TOKEN`: A valid access token for the Sifflet API
- `SIFFLET_RULE_ID`: The ID of a rule to test with

Then run:

```bash
./gradlew test
```

## Building the Plugin

To build the plugin JAR file:

```bash
./gradlew shadowJar
```

The JAR file will be created in the `build/libs` directory.

## Testing with Kestra

1. Build the plugin:
   ```bash
   ./gradlew shadowJar
   ```

2. Copy the JAR file to your Kestra plugins directory:
   ```bash
   cp build/libs/plugin-sifflet-*.jar /path/to/kestra/plugins/
   ```

3. Restart Kestra to load the new plugin.

Alternatively, you can use Docker:

```bash
docker build -t kestra-with-sifflet .
docker run --rm -p 8080:8080 kestra-with-sifflet server local
```

## Creating a Pull Request

1. Fork the repository
2. Create a new branch for your feature or bug fix
3. Make your changes
4. Run tests to ensure everything works
5. Submit a pull request

Please ensure your code follows the existing style and includes appropriate tests.

## Code Style

This project follows the standard Java code style. Please ensure your code is properly formatted before submitting a pull request.

## License

By contributing to this project, you agree that your contributions will be licensed under the project's Apache 2.0 license.
