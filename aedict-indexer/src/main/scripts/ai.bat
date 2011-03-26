@echo off
if "%OS%"=="Windows_NT" @setlocal

SET CLSPATH=.

set JAVA_OPTS=-Xmx1024m

rem Add platform runtime libraries
for %%i in (lib\*.jar) do call :cpappend %%i

rem Launch the runtime
java %JAVA_OPTS% -classpath "%CLSPATH%" sk.baka.aedict.indexer.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

if "%OS%"=="Windows_NT" @endlocal

goto :EOF

:cpappend
	set CLSPATH=%CLSPATH%;%1
	goto :EOF

