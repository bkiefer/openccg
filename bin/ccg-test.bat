@echo off
rem For usage, do: ccg-test -h
call %~dp0\ccg-env
rem set HPROF=-Xrunhprof:cpu=times,file=hmm-prof.txt
%JAVA_CMD% opennlp.ccg.test.Regression %*
