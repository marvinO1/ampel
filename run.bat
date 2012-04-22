rem echo off
set ampel.hudson.url=http://deadlock.netbeans.org/hudson/api/xml
set ampel.hudson.job=Moxi
set ampel.hudson.color=red
set ampel.pm.exe=c:\pm.exe
set ampel.pm.device=SIS-PM
set ampel.pm.lamp.red=red
set ampel.pm.lamp.green=green

set CLS_PATH="..\lib\ampel.jar;..\lib\dom4j-1.6.1.jar"
java -cp %CLS_PATH% rib.ampel.AmpelMain     
pause

