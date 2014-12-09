The project is open source now, and welcome to contribute your sugessions, comments, issues report.

How to build

A. precondition:
1. Download and Install Android SDK
2. Download and Install Andoird NDK
3. Downlaod and Install Eclipse or Android Studio

B. build native code under command line with NDK
# cd 3DHomeEngine/
# ndk-build

C. Import prject into Eclipse or Android Studio
1. Application project:
   3DHome
   
2. Library project:
   3DHomeEngine
   BaiduStatic
   BorqsWeather
   DigitalFrameService
   Market
   
3. jar 
   ./3DHome/libs/GoogleAdMobAdsSdk-6.4.1.jar
   ./BaiduStatic/libs/android-support-v4.jar
   ./BaiduStatic/libs/android_api_3_4.jar
   ./BorqsWeather/libs/locSDK_3.1.jar
   ./Market/libs/google-play-services.jar
   
D. Dependency
1. 3DHomeEngine => none
2. BaiduStatic => android-support-v4.jar, android_api_3_4.jar
3. BorqsWeather => android-support-v4.jar, locSDK_3.1.jar
4. DigitalFrameService => android-support-v4.jar
5. Market => BaiduStatic, android-support-v4.jar, google-play-services.jar
6. 3DHome => 3DHomeEngine, BaiduStatic, BorqsWeather, DigitalFrameService, Market, GoogleAdMobAdsSdk-6.4.1.jar, android-support-v4.jar
