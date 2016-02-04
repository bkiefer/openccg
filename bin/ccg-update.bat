@echo off
rem For usage, do: ccg-update -h
call %~dp0\ccg-env
%JAVA_CMD% opennlp.ccg.test.UpdateTestbed %*
