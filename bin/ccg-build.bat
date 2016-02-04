@echo off
set scriptdir=%~dp0
call %scriptdir%\ccg-env

cd %scriptdir%\..
call mvn clean install assembly:single -DskipTests=true
call ant -f build-stripped.xml
