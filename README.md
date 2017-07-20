# Seeds Android SDK

Increase your app revenue (and so much more) with the power of social good by integrating Seeds into your app!   If you have any questions regarding your specific setup, feel free to contact our team through the [website](http://www.playseeds.com) chat, or at team@playseeds.com.
We’re so happy you’re here!

## The following platforms are also available:
- [Unity SDK](https://github.com/therealseeds/seeds-sdk-unity)
- [iOS](https://github.com/therealseeds/seeds-sdk-ios)
## Pull requests welcome
We’re built on open source and welcome bug fixes and other contributions.
## Pre-integration checklist

Before beginning the integration, please take care of all of the below:

- Create a Seeds account [here](https://www.playseeds.com/) if you haven't already done so
- The [Dashboard](https://developers.playseeds.com/index.html) tab shows a list of your apps and the campaigns they contain. You can start with the default `example-interstitial`, and we’ll add your final campaigns automatically.

- Familiarize yourself with the [Seeds Android Example Integration](https://github.com/therealseeds/seeds-sdk-android/tree/master/app-inappmessaging), which shows a complete Seeds integration in action.  It’s good to have on hand as a reference if needed.

## Download
Gradle:
```groovy
repositories {
  jcenter()
}
dependencies {
  compile 'com.playseeds:android-sdk:0.5.0'
}
```
Or Maven:
```xml
<dependency>
  <groupId>com.playseeds</groupId>
  <artifactId>android-sdk</artifactId>
  <version>0.5.0</version>
  <type>pom</type>
</dependency>
```
Seeds requires Android version 4.1 or higher.
## ProGuard
The Seeds SDK does not currently support ProGuard. If you are using ProGuard please add the following option to your ProGuard Rules:
```
-keep com.playseeds.** { *; }
```
## Usage
The Seeds SDK functionality is divided into two parts: [Interstitials](#interstitials_header) that represent all functionality connected to the content Seeds shows in-app, and [Events](#events_header) that represent logging analytics data.
## Initialization
**Seeds must be initialized on the very beginning of the app launch.**
If you have an Application subclass, then do the following:
```java
public class YourApplication extends Application {
    @Override
    public void onCreate() {
        ...
        Seeds.init(this, SEEDS_APP_KEY);
    }
}
```
If you don’t have the Application subclass, please do the following in your Main Activity:
```java
public class YourMainActivity extends Activity {
    @Override
    public void onCreate() {
        ...
        Seeds.init(this, SEEDS_APP_KEY);
    }
}
```
You can find your **SEEDS_APP_KEY**  in the [Seeds’ Dashboard](https://developers.playseeds.com/index.html).
## <a name="interstitials_header"></a>Interstitials
### General rules:
* InterstitialId is the id of the interstitial (you guessed it!), which can be found in your [Dashboard](https://developers.playseeds.com/index.html) under the **Campaign Name** section.
* Context is the description of the place in your app at which you are showing the Seeds interstitial.  Please use short but understandable descriptions (e.g. “level_1", “pause”, “app_start”).
### 1) Pre-load the intesrtitial:
**Please note that every interstitial must be pre-loaded before you attempt to show it.**
We suggest pre-loading all the interstitials from your Main Activity
```java
public class YourMainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        Seeds.interstitials().fetch(YOUR_INTERSTITIAL_ID);
        Seeds.interstitials().fetch(YOUR_INTERSTITIAL_ID_2);
    }
}
```
### 2) Set the listener:
To receive callbacks about events from the SDK (e.g. notifications about clicks, dismissals, errors, and loading), please use the implementation of the [InterstitialListener](#interstitials_listener), or the InterstitialListenerAdapter (overriding only the methods you need). We suggest setting the listener at the place  that will be active at the time the interstitial is shown. For example:
```java
public class SomeActivity extends Activity implements InterstitialListener {
    
    @Override
    protected void onResume() {
        ...
        Seeds.interstitials().setListener(this);
    }
    @Override
    protected void onPause() {
        ...
        Seeds.interstitials().setListener(null); //Used to clear the listener
    }
    @Override
    public void onLoaded(SeedsInterstitial seedsInterstitial) {
        //Called when the interstitial was loaded
    }
    @Override
    public void onClick(SeedsInterstitial seedsInterstitial) {
        //Called when the interstitial "buy" button was clicked
    }
    @Override
    public void onShown(SeedsInterstitial seedsInterstitial) {
        //Called when the interstitial was successfully shown
    }
    @Override
    public void onDismissed(SeedsInterstitial seedsInterstitial) {
        //Called when the user presses "close" or "back" button
    }
    @Override
    public void onError(String interstitialId, Exception exception) {
        //Called when error occurs
    }
}
```
### 3) Show the Interstitial:
To show the interstitial please do the following:
- First сheck to see if the interstitial has already loaded. **Please note that the onError() method of the listener will also be called if the interstitial was not pre-loaded.**
- If the interstitial is loaded - show it!  Sample usage:
```java
public class SomeActivity extends Activity {
    public void someMethod(){
        ...
        if (Seeds.interstitials().isLoaded(YOUR_INTERSTITIAL_ID)){
            Seeds.interstitials().show(YOUR_INTERSTITIAL_ID, YOUR_INTERSTITIAL_CONTEXT);
        }   
    }
}
```
### <a name="interstitials_listener"></a>InterstitialsListener
The interface contains five methods for addressing different scenarios after the attempted opening of a Seeds interstitial.
```java
void onLoaded(SeedsInterstitial seedsInterstitial); //Called when the interstitial was loaded
void onClick(SeedsInterstitial seedsInterstitial); //Called when the interstitial "buy" button was clicked
void onShown(SeedsInterstitial seedsInterstitial); //Called when the interstitial was successfully shown
void onDismissed(SeedsInterstitial seedsInterstitial); //Called when the user presses "close" or "back" button
void onError(String interstitialId, Exception exception); //Called when error occurs
```
### <a name=“seeds_interstitial”></a>SeedsInterstitial
This is the object-wrapper used by in the listener by the Seeds SDK for sharing info about interstitials. As of the most recent release, it contains:
* InterstitialId;
* Context;
## <a name="events_header"></a>Events
An event is the generalized mechanism for tracking all user actions taken in-app. **Seeds.Events** use two approaches for logging data: direct logging for purchases made, and an object-based approach for tracking all other data. Use `logSeedsIAPPayment()` or `logIAPPayment()` after any successful purchase, and `logUser()` with the provided wrapper to empower Seeds to make the targeted recommendations that will best convert your non-payers into paying customers. There is also an additional way to log your app’s custom data, `logEvent()`, which uses the object-based approach with custom attributes.
### After successful in-app purchase:
**This method should be called after any successful purchase in the app.**  Depending on whether a completed purchases was either a Seeds purchase or a non-Seeds purchase, please notify the SDK in one of the two following ways:
```java
public class SomeActivity extends Activity {
    public void someMethod(){
        ...
        //Successful purchase was made above
        //If there was a Seeds-promoted purchase:
        Seeds.events().logSeedsIAPPayment(PRODUCT_ID, YOUR_PURCHASE_PRICE, YOUR_TRANSACTION_ID);
        //If there was ususal purchase:
        Seeds.events().logIAPPayment(PRODUCT_ID, YOUR_PURCHASE_PRICE, YOUR_TRANSACTION_ID);
    }
}
```
### After generating user data:
- Send the user info so that Seeds has the tools needed to best convert your users into paying customers.
**UserInfo is the predefined object, that provide all supported parameters**:
```java
public class SomeActivity extends Activity {
    public void someMethod(){
        ...
        //User info is available here
        UserInfo userInfo = new UserInfo();
        userInfo.setName("John");
        userInfo.setEmail("example_email@example.com");
        //Any other parameters that are available go here
        ...
        Seeds.events().logUser(userInfo);
    }
}
```
- Track common events using the predefined **Event** class, or create a subclass for using `putAttribute(String key, String value)`:
```java
Seeds.events().logEvent(Event event);
```
## Optional: Add Seeds branding to your in-app store

In addition to boosting your revenue using the Seeds interstitials, you can increase profits by adding the Seeds logo to the appropriate in-app purchase items within your marketplace.  If you’d like to try this, please contact us via the [website](http://www.playseeds.com) chat. We’re more than happy to help!

### Switch to the Production Environment App Key

Before publishing your app update, please switch from using the Test Environment App Key to the Production Environment App Key.  If needed, you can find both keys in your [Dashboard](https://developers.playseeds.com/index.html).

### Add Your Social Good Transfer Information
Visit [this link](https://developers.playseeds.com/) and click on the Social Good Transfer tab on the left menu to input the credit card information for your future social good transfers!

### Finally, Submit Your App Update to the Store!
Now you’re all set to make as much as 30% more money while simultaneously helping folks in need around the world.  You are amazing!  Thank you!


## Solutions to common integration problems

### Why isn’t the interstitial showing up?

1. Check that the campaign name is correct at your [Dashboard](https://developers.playseeds.com/index.html).
2. Confirm in `inAppMessageShown` that the interstitial pre-load was successful.

### Ahhh, I'm experiencing another problem.  Please help!

Please reach out via the [website](http://www.playseeds.com) chat, and we’ll be happy to quickly assist.

## License
MIT License

Copyright (c) 2017 Seeds


Copyright (c) 2012, 2013 Countly

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
