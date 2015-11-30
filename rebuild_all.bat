@echo off
call mvn -P without-performence-tests -Dcobertura.report.format=xml clean install cobertura:cobertura sonar:sonar
pause
