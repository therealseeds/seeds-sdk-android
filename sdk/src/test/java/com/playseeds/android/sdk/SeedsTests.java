package com.playseeds.android.sdk;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(SeedsTestsRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SeedsTests {
    public static final String SERVER = "http://devdash.playseeds.com";
    public static final String NO_ADS_APP_KEY = "ef2444ec9f590d24db5054fad8385991138a394b";
    public static final String UNLIMITED_ADS_APP_KEY = "c30f02a55541cbe362449d29d83d777c125c8dd6";

    @Before
    public void setup() {
    }

    @Test
    public void testSeedInAppMessageShown() {
        InAppMessageListener listener = new InAppMessageListener() {
            @Override
            public void inAppMessageClicked() {

            }

            @Override
            public void inAppMessageClosed(InAppMessage inAppMessage, boolean completed) {

            }

            @Override
            public void inAppMessageLoadSucceeded(InAppMessage inAppMessage) {

            }

            @Override
            public void inAppMessageShown(InAppMessage inAppMessage, boolean succeeded) {

            }

            @Override
            public void noInAppMessageFound() {

            }
        };

        Seeds.sharedInstance()
                .init(ShadowApplication.getInstance().getApplicationContext(), listener,
                        SERVER, UNLIMITED_ADS_APP_KEY, null, DeviceId.Type.OPEN_UDID);


//        WelcomeActivity activity = Robolectric.setupActivity(WelcomeActivity.class);
//        activity.findViewById(R.id.login).performClick();
//
//        Intent expectedIntent = new Intent(activity, WelcomeActivity.class);
//        assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
    }

//
//    - (void)setUp {
//
//        [super setUp];
//
//        [Seeds sharedInstance].inAppMessageDelegate = self;
//
//        _testVC = [[TestViewController alloc] init];
//
//    }
//
//    - (void)tearDown {
//
//        [super tearDown];
//
//    }
//
//    - (void)testSeedInAppMessageShown {
//
//        [Seeds.sharedInstance start:YOUR_APP_KEY withHost:YOUR_SERVER];
//
//        NSDate *fiveSeconds = [NSDate dateWithTimeIntervalSinceNow:5.0];
//
//        if ([[Seeds sharedInstance] isInAppMessageLoaded]) {
//            [[Seeds sharedInstance] showInAppMessageIn:_testVC];
//        } else {
//            [[Seeds sharedInstance] requestInAppMessage];
//        }
//
//        [[NSRunLoop currentRunLoop] runUntilDate:fiveSeconds];
//
//        XCTAssertTrue(_seedsInAppMessageShown, @"in app message not shown");
//        XCTAssertTrue(_seedsInAppMessageLoaded, @"not loaded");
//        XCTAssertFalse(_seedsNotFound, @"not found");
//
//    }
//
//    - (void)testSeedInAppMessageShownNeverAds {
//
//        [[Seeds sharedInstance] start:YOUR_APP_KEY_NEVER withHost:YOUR_SERVER];
//
//        NSDate *fiveSeconds = [NSDate dateWithTimeIntervalSinceNow:5.0];
//
//        if ([[Seeds sharedInstance] isInAppMessageLoaded]) {
//            [[Seeds sharedInstance] showInAppMessageIn:_testVC];
//        } else {
//            [[Seeds sharedInstance] requestInAppMessage];
//        }
//
//        [[NSRunLoop currentRunLoop] runUntilDate:fiveSeconds];
//
//        XCTAssertFalse(_seedsInAppMessageLoadedNever, @"Message loaded");
//
//    }
//
//    - (void)testSeedInAppMessageShownAlwaysAds {
//
//        [[Seeds sharedInstance] start:YOUR_APP_KEY_ALWAYS withHost:YOUR_SERVER];
//
//        NSDate *fiveSeconds = [NSDate dateWithTimeIntervalSinceNow:5.0];
//
//        if ([[Seeds sharedInstance] isInAppMessageLoaded]) {
//            [[Seeds sharedInstance] showInAppMessageIn:_testVC];
//        } else {
//            [[Seeds sharedInstance] requestInAppMessage];
//        }
//
//        [[NSRunLoop currentRunLoop] runUntilDate:fiveSeconds];
//
//        XCTAssertTrue(_seedsInAppMessageLoadedAlways, @"Message not loaded");
//
//    }
//
//    #pragma mark - SeedsInAppMessageDelegate
//
//    - (void)seedsInAppMessageShown:(SeedsInAppMessage*)inAppMessage withSuccess:(BOOL)success {
//
//        if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY]) {
//            _seedsInAppMessageShown = success;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_NEVER]) {
//            _seedsInAppMessageShownNever = success;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_ALWAYS]) {
//            _seedsInAppMessageShownAlways = success;
//        }
//    }
//
//    - (void)seedsInAppMessageLoadSucceeded:(SeedsInAppMessage*)inAppMessage {
//
//        if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY]) {
//            _seedsInAppMessageLoaded = YES;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_NEVER]) {
//            _seedsInAppMessageLoadedNever = YES;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_ALWAYS]) {
//            _seedsInAppMessageLoadedAlways = YES;
//        }
//
//        [[Seeds sharedInstance] showInAppMessageIn:_testVC];
//
//    }
//
//
//    - (void)seedsNoInAppMessageFound {
//
//        if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY]) {
//            _seedsNotFound = YES;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_NEVER]) {
//            _seedsNotFoundNever = YES;
//        } else if ([[[Seeds sharedInstance] getAppKey] isEqualToString:YOUR_APP_KEY_ALWAYS]) {
//            _seedsNotFoundAlways = YES;
//        }
//    }
//

}
