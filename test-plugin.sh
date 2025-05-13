#!/bin/bash

# Build the plugin
./gradlew shadowJar

# Build a custom Docker image with the plugin
docker build -t kestra-with-sifflet .

# Run Kestra with the plugin
docker run --rm -p 8080:8080 kestra-with-sifflet server local
