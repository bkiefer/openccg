@echo off
rem Usage: ccg-grammardoc [-s|--source sourceDir] [-d|--dest destDir]
call  %~dp0\ccg-env
set JAVA_ARGS=-Xmx128m -classpath %CP%
%JAVA% %JAVA_ARGS% opennlp.ccg.grammardoc.GrammarDoc %*
