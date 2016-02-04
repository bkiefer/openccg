@echo off
rem sets OpenCCG environment variables

set CP="%~dp0\..\target\openccg.jar"
rem variant for use with 'build compile' option, if desired:
rem set CP=%OPENCCG_CLASSES%;%OPENCCG_SRC%;%DIRLIBS%
set JAVA="java"
set JAVA_MEM=-Xmx256m
rem set JAVA_MEM=-Xmx2048m
rem set JAVA_MEM="-Xmx8g"
rem set JAVA_MEM="-Xmx16g"
set JAVA_CMD=%JAVA% %JAVA_MEM% -classpath %CP% -Dfile.encoding=UTF8
