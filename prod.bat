echo off
echo ************************************
echo Deploying app to PRODUCTION!
echo ************************************
pause
echo Building frontend...
call gulp
echo Deploying to GAE...
call mvn appengine:update