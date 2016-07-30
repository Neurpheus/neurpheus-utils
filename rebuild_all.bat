@echo off
rem call mvn -P without-performence-tests clean compile org.jacoco:jacoco-maven-plugin:0.7.5.201505241946:prepare-agent install sonar:sonar
call mvn clean compile org.jacoco:jacoco-maven-plugin:0.7.5.201505241946:prepare-agent install sonar:sonar
pause
