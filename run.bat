@echo off
REM run.bat - Windows helper to run common project targets
REM Usage: run.bat ^<target^>
REM Targets: package, test, run-sample, run-newman, clean

setlocal

if "%~1"=="" goto usage

if /I "%~1"=="package" (
    echo Running: mvn -DskipTests package
    mvn -DskipTests package
    goto :eof
)

if /I "%~1"=="test" (
    echo Running: mvn test
    mvn test
    goto :eof
)

if /I "%~1"=="run-sample" (
    echo Running: java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\sample
    java -jar "target\aaga-projet-0.1.0-SNAPSHOT.jar" -i "data\sample.edgelist" -o "results\sample" -a bsa
    goto :eof
)

if /I "%~1"=="run-newman" (
    echo Running: java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\big
    java -jar "target\aaga-projet-0.1.0-SNAPSHOT.jar" -i "data\sample.edgelist" -o "results\newman"
    goto :eof
)

if /I "%~1"=="clean" (
    echo Running: mvn -q clean
    mvn -q clean
    if exist results rmdir /s /q results
    goto :eof
)

:usage
echo Usage: run.bat ^<target^>
echo Targets: package, test, run-sample, run-newman, clean
endlocal
