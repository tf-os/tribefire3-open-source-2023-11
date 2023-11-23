@echo off
for /d %%f in (*-model) do (
    echo Triggering Model Build including Typescript Generation #3 > %%~nf\ci-trigger.txt
)