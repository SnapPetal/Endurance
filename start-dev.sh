#!/bin/bash
# Start the application in dev mode with environment variables loaded from .env

# Load environment variables from .env file
set -a
source .env
set +a

# Start the application with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev