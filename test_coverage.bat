@echo off
call mvn clean org.jacoco:jacoco-maven-plugin:0.7.5.201505241946:prepare-agent install

pause
