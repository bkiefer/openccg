@echo off
rem For usage, do: tccg -h
call %~dp0\ccg-env
%JAVA_CMD% opennlp.ccg.TextCCG %*
