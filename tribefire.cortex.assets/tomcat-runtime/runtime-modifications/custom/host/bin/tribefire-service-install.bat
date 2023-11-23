@echo off
call tomcat9.exe //DS//tribefire-host >>nul 2>&1
echo Tribefire Host Service removed.
call service.bat install tribefire-host >>nul 2>&1
echo Tribefire Host Service installed.
call tomcat9.exe //US//tribefire-host --DisplayName="Tribefire Host" --Description="Braintribe Tribefire Host (Nucleus)" --Startup=auto >>nul 2>&1
call tomcat9.exe //US//tribefire-host ++JvmOptions=-Dfile.encoding=UTF-8;-Xms64m;-Xmx2048m --JvmMs=64m --JvmMx=2048m  >>nul 2>&1
echo Tribefire Host Service updated.
