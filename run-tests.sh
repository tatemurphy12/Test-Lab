#!/bin/bash
set -e

echo "Compiling"
javac -cp "libs/*:." MidsQuest.java MidsQuestTest.java

echo "Running tests"
java -cp "libs/*:." org.junit.platform.console.ConsoleLauncher --scan-classpath --include-tag "$1"

