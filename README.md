#Seeds
[Seeds](http://www.playseeds.com) increases paying user conversion for freemium mobile games motivating users to make their first purchase by letting them know that their purchase will help finance microloans in the developing world. The SDK implements this with an interstitial ad and event tracking analytics.

##Integrating the Seeds SDK

Note the Seeds Android SDK is built with production-tested open source components, including [Countly Android SDK](https://github.com/Countly/seeds-sdk-android) for analytics and in-app messaging functionality from the [MobFox Android SDK](https://github.com/mobfox/MobFox-Android-SDK).

##Preparation
If you would like to just test the in-app message functionality to display a test image promo, you may use the special app_key “test”.

After testing, please make a deep link (see [how to make a deep link](https://developer.android.com/training/app-indexing/deep-linking.html)) to the in-app purchase item you’d like to promote and let us know what that deep link is.

If you haven't already, please also let us know your name,  email address and game name so we can get you set up. Please send this info to [sungwon@playseeds.com](sungwon@playseeds.com)

We will then set you up with an app_key so you can get started.


##Installation

###1. Add the SDK to your project

####Android Studio

Add to your build.gradle in Android Studio:

```gradle
repositories {
   maven {
       url  "http://dl.bintray.com/seedsinc/maven"
   }
}

dependencies {
   compile('com.playseeds:android-sdk:0.1.9')
}
```

####Eclipse

Download the following jar and add them to to your libs directory:


[https://bintray.com/artifact/download/seedsinc/android_sdk/seeds-android-sdk-0.1.4.jar](https://bintray.com/artifact/download/seedsinc/android_sdk/seeds-android-sdk-0.1.4.jar)



###2. Set up the SDK

1) Implement the InAppMessageListener in your main activity, eg.:

```java
...implements InAppMessageListener
```

which requires adding the following methods to your main activity:

```java
@Override
public void inAppMessageClicked() {
}

@Override
public void inAppMessageClosed(InAppMessage inAppMessage, boolean b) {

}

@Override
public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {

}

@Override
public void inAppMessageShown(InAppMessage inAppMessage, boolean b) {
}

@Override
public void noInAppMessageFound() {

}
```

2) Add the following to your main activity onCreate method. Please use your app_key as a String in place of YOUR_APP_KEY.

```java
Seeds.sharedInstance()
               .init(this, "http://dash.playseeds.com", “YOUR_APP_KEY”, null, “YOUR_DEVICE_ID”) // note: make sure there is no trailing slash in the url
               .setLoggingEnabled(true);  //optional
```

You can specify device ID by yourself if you have one (it has to be unique per device):

`Seeds.sharedInstance().init(this, "http://dash.playseeds.com", "YOUR_APP_KEY", "YOUR_DEVICE_ID")`

You can rely on Google Advertising ID for device ID generation (recommended) which also requires setting up [Google Play Services](https://developers.google.com/android/guides/setup).

`Seeds.sharedInstance().init(this, "http://dash.playseeds.com", "YOUR_APP_KEY", null, DeviceId.Type.ADVERTISING_ID)`

Or you can use OpenUDID:
`Seeds.sharedInstance().init(this, "http://dash.playseeds.com", "YOUR_APP_KEY", null, DeviceId.Type.OPEN_UDID)`

In the case of OpenUDID you'll need to include the following declaration in your AndroidManifest.xml:

```xml
<service android:name="org.openudid.OpenUDID_service">
    <intent-filter>
        <action android:name="org.openudid.GETUDID" />
    </intent-filter>
</service>
```

3) Next, you can optionally preload the Seeds promo :

```java
Seeds.sharedInstance().requestInAppMessage(); //  recommended to preload the promo
```

4) Then add the following calls to all your activities:
- Call `Seeds.sharedInstance().onStart()` in onStart.
- Call `Seeds.sharedInstance().onStop()` in onStop.

5) Finally, make sure that the INTERNET permission is set if it isn’t already in your manifest file

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

and include the following in your AndroidManifest.xml:

```xml
<!-- for Seeds promo -->
<activity android:name="com.playseeds.android.sdk.inappmessaging.RichMediaActivity"
   android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize"
   android:hardwareAccelerated="false" />
<activity android:name="com.playseeds.android.sdk.inappmessaging.InAppWebView"
   android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize" />
```

###3. Display the Seeds promo interstitial

Use the InAppMessageManager instance to load:

```java
Seeds.sharedInstance().requestInAppMessage();
```

and show:

```java
Seeds.sharedInstance().showInAppMessage();
```

the interstitial promo.

You may wish to add a helper method to your activity to accomplish these functions, e.g.:

```java
public void showSeedsPromo() {
       try {
           runOnUiThread(new Runnable() {
               public void run() {
                   if (Seeds.sharedInstance().isInAppMessageLoaded()) {
                       Seeds.sharedInstance().showInAppMessage();
                   } else {
                       Seeds.sharedInstance().requestInAppMessage();
                   }
               }
           });
       } catch (Exception e) {
           System.out.println("Exception: " + e);
       }
}
```

4. Track the item purchase

In your item store code, please include the following tracking code after a purchase of the Seeds-promoted item:

```java
Seeds.sharedInstance().recordSeedsIAPEvent(ITEM, PRICE);
```

and for regular non-Seeds-promoted items:

```java
Seeds.sharedInstance().recordIAPEvent(ITEM, PRICE);
```

where ITEM is the name or SKU of the item and PRICE is the price of the item.

## Support

Open an issue or email [sungwon@playseeds.com](sungwon@playseeds.com)
