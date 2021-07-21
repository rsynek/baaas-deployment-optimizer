#!/bin/bash

readonly quarkusRunner="target/quarkus-app/quarkus-run.jar"

if [[ ! -f "$quarkusRunner" ]]; then
  echo "No target folder detected. Building the project."
  mvn clean package
fi

java -jar "$quarkusRunner" "$@"