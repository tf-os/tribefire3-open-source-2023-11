@echo off
setlocal
set ANT_OPTS=-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y
set GRADLE_DEBUG_OPTS=-Dorg.gradle.debug=true --no-daemon
CALL dr
endlocal