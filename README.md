# sunbird-android
Mobile app for sunbird software. Provides the mobile interfaces for all functionality of Sunbird.

Steps to build APK from git repo:

1. Install Android Studio and configure it.
2. Clone GitHub `sunbird-android` repo into it.
3. Replace `$PRODUCER_ID$`, `$CHANNEL_ID$`, `$MOBILE_APP_KEY$` and `$MOBILE_APP_SECRET$` in build.gradle inside app module.
4. You need to generate key and secret for `mobile_app` user using JWT token of `mobile_admin` user.
Please find the steps here
[https://github.com/project-sunbird/sunbird-devops/blob/master/Installation.md#step-6-generate-key-and-secrets-for-mobile-app](https://github.com/project-sunbird/sunbird-devops/blob/master/Installation.md#step-6-generate-key-and-secrets-for-mobile-app)
5. If You want to change the app name go to sunbird-android/app/src/main/res/values/strings.xml and give the required app name.
6. For app logo changing goto sunbird-android/app/src/main/res folder, here in all mipmap folders and drawable folder replace ic_launcher.png image with your logo. Logo name should be `ic_launcher.png`
7. Replace `$YOUR_FABRIC_API_KEY$` in AndroidManifest.xml with your fabric ApiKey. You can create your account in [fabric.io](https://get.fabric.io/) and register your app. After registering your app you will get the ApiKey which you need to add in manifest.
8. If you would like to show contents only for the given channelId than change the value of `FILTER_CONTENT_BY_CHANNEL_ID` to true, by default its false.