#!/bin/bash

echo change directory to backend/functions
cd backend/functions
echo installing npm modules
npm install
cd ..
echo change directory to backend, deploying cloud functions
firebase deploy --only functions --token $1
echo changing directory to frontend
cd ../frontend
echo setting sdk path
if [ -z "$2" ] 
then
    echo no sdk parameter passed, using default
else
    echo setting Android SDK path
    export ANDROID_HOME=$2
fi

echo building apk
./gradlew assembleDebug