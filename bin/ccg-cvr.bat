@echo off
rem For usage, do: ccg-cvr -h
call %~dp0\ccg-env
%JAVA_CMD% opennlp.ccg.test.CrossValidateRealizer %*
