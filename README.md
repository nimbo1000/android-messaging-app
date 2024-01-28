#### Whisper - Chatapp project for Mobile Cloud Computing course 2018

### Overview

Whisper is a real-time chat application that supports both one-to-one and group communication. The chat conversations must support both text messages and pictures. Additionally, the pictures shared in the chat messages are displayed in a gallery and sorted based on their features.

### Getting started

Whisper comes with basic chat functionalities in this version. Features and basic functionalities are listed below:

1. User authentication using a mail id
2. Profile settings. 
3. Search for users with username.
4. One-to-One and Group chat
5. Image sharing from gallery and camera
6. Pop up notifications
7. Image resolution optimization
8. Leave chat options


### Project Structure and How to Run

The folder hierarchy is formed of two main components frontend and backend. The frontend includes an Android studio project that represents the client component. The backend includes the firebase project and cloud functions for the backend. To run the whole project, both client and server sides you need to run the deploy.sh script. It accepts two parameters, one mandatory parameter that represents the user's login token to firebase and an optional parameter which is a path to the Android SDK on the machine used to build the project; in case the environment variable ANDROID_HOME is not set. Example: deploy.sh [LOGIN_TOKEN] [SDK_PATH].

To generate a login token, you need to run the command firebase login:ci and login to your account from a web browser. The login token will be shown in the command line window after a successful login and can be used to authenticate a user on firebase in headless mode.

#### Script steps

1. Installing the required node modules using npm.
2. Logging into firebase using the generated token and deploying all cloud functions.
3. Building the Android app and generating the .apk file.


####  Prerequisites to build

The following components need to be installed to be able to run and deploy the project:

1. Google Cloud SDK version
2. Firebase CLI
3. Node.js

### Authors

KJ
KM
ME
NP
SB


Made @Aalto University.