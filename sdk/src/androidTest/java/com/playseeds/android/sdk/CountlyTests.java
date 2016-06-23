/*
Copyright (c) 2012, 2013, 2014 Countly

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.playseeds.android.sdk;

import android.content.Context;
import android.test.AndroidTestCase;

import java.util.HashMap;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CountlyTests extends AndroidTestCase {
    Seeds mUninitedSeeds;
    Seeds mSeeds;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final CountlyStore countlyStore = new CountlyStore(getContext());
        countlyStore.clear();

        mUninitedSeeds = new Seeds();

        mSeeds = new Seeds();
        mSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "1234");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        assertNotNull(mUninitedSeeds.getConnectionQueue());
        assertNull(mUninitedSeeds.getConnectionQueue().getContext());
        assertNull(mUninitedSeeds.getConnectionQueue().getServerURL());
        assertNull(mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getTimerService());
        assertNull(mUninitedSeeds.getEventQueue());
        assertEquals(0, mUninitedSeeds.getActivityCount());
        assertEquals(0, mUninitedSeeds.getPrevSessionDurationStartTime());
        assertFalse(mUninitedSeeds.getDisableUpdateSessionRequests());
        assertFalse(mUninitedSeeds.isLoggingEnabled());
    }

    public void testSharedInstance() {
        Seeds sharedSeeds = Seeds.sharedInstance();
        assertNotNull(sharedSeeds);
        assertSame(sharedSeeds, Seeds.sharedInstance());
    }

    public void testInitWithNoDeviceID() {
        mUninitedSeeds = spy(mUninitedSeeds);
        mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", null);
        verify(mUninitedSeeds).init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", null);
    }

    public void testInit_nullContext() {
        try {
            mUninitedSeeds.init(null, null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "1234");
            fail("expected null context to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullServerURL() {
        try {
            mUninitedSeeds.init(getContext(), null, null, null, "appkey", "1234");
            fail("expected null server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_emptyServerURL() {
        try {
            mUninitedSeeds.init(getContext(), null, null, "", "appkey", "1234");
            fail("expected empty server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_invalidServerURL() {
        try {
            mUninitedSeeds.init(getContext(), null, null, "not-a-valid-server-url", "appkey", "1234");
            fail("expected invalid server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullAppKey() {
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", null, "1234");
            fail("expected null app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_emptyAppKey() {
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "", "1234");
            fail("expected empty app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullDeviceID() {
        // null device ID is okay because it tells Seeds to use OpenUDID
       mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", null);
    }

    public void testInit_emptyDeviceID() {
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "");
            fail("expected empty device ID to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithSameParams() {
        final String deviceID = "1234";
        final String appKey = "appkey";
        final String serverURL = "http://ly.count.android.sdk.test.count.ly";

        mUninitedSeeds.init(getContext(), null, null, serverURL, appKey, deviceID);
        final EventQueue expectedEventQueue = mUninitedSeeds.getEventQueue();
        final ConnectionQueue expectedConnectionQueue = mUninitedSeeds.getConnectionQueue();
        final CountlyStore expectedCountlyStore = expectedConnectionQueue.getCountlyStore();
        assertNotNull(expectedEventQueue);
        assertNotNull(expectedConnectionQueue);
        assertNotNull(expectedCountlyStore);

        // second call with same params should succeed, no exception thrown
        mUninitedSeeds.init(getContext(), null, null, serverURL, appKey, deviceID);

        assertSame(expectedEventQueue, mUninitedSeeds.getEventQueue());
        assertSame(expectedConnectionQueue, mUninitedSeeds.getConnectionQueue());
        assertSame(expectedCountlyStore, mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertSame(getContext(), mUninitedSeeds.getConnectionQueue().getContext());
        assertEquals(serverURL, mUninitedSeeds.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedSeeds.getConnectionQueue().getAppKey());
        assertSame(mUninitedSeeds.getConnectionQueue().getCountlyStore(), mUninitedSeeds.getEventQueue().getCountlyStore());
    }

    public void testInit_twiceWithDifferentContext() {
        mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "1234");
        // changing context is okay since SharedPrefs are global singletons
        mUninitedSeeds.init(mock(Context.class), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "1234");
    }

    public void testInit_twiceWithDifferentServerURL() {
        mUninitedSeeds.init(getContext(), null, null, "http://test1.count.ly", "appkey", "1234");
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://test2.count.ly", "appkey", "1234");
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithDifferentAppKey() {
        mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey1", "1234");
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey2", "1234");
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithDifferentDeviceID() {
        mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "1234");
        try {
            mUninitedSeeds.init(getContext(), null, null, "http://ly.count.android.sdk.test.count.ly", "appkey", "4321");
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_normal() {
        final String deviceID = "1234";
        final String appKey = "appkey";
        final String serverURL = "http://ly.count.android.sdk.test.count.ly";

        mUninitedSeeds.init(getContext(), null, null, serverURL, appKey, deviceID);

        assertSame(getContext(), mUninitedSeeds.getConnectionQueue().getContext());
        assertEquals(serverURL, mUninitedSeeds.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNotNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getEventQueue());
        assertSame(mUninitedSeeds.getConnectionQueue().getCountlyStore(), mUninitedSeeds.getEventQueue().getCountlyStore());
    }

    public void testHalt_notInitialized() {
        mUninitedSeeds.halt();
        assertNotNull(mUninitedSeeds.getConnectionQueue());
        assertNull(mUninitedSeeds.getConnectionQueue().getContext());
        assertNull(mUninitedSeeds.getConnectionQueue().getServerURL());
        assertNull(mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getTimerService());
        assertNull(mUninitedSeeds.getEventQueue());
        assertEquals(0, mUninitedSeeds.getActivityCount());
        assertEquals(0, mUninitedSeeds.getPrevSessionDurationStartTime());
    }

    public void testHalt() {
        final CountlyStore mockCountlyStore = mock(CountlyStore.class);
        mSeeds.getConnectionQueue().setCountlyStore(mockCountlyStore);
        mSeeds.onStart();
        assertTrue(0 != mSeeds.getPrevSessionDurationStartTime());
        assertTrue(0 != mSeeds.getActivityCount());
        assertNotNull(mSeeds.getEventQueue());
        assertNotNull(mSeeds.getConnectionQueue().getContext());
        assertNotNull(mSeeds.getConnectionQueue().getServerURL());
        assertNotNull(mSeeds.getConnectionQueue().getAppKey());
        assertNotNull(mSeeds.getConnectionQueue().getContext());

        mSeeds.halt();

        verify(mockCountlyStore).clear();
        assertNotNull(mSeeds.getConnectionQueue());
        assertNull(mSeeds.getConnectionQueue().getContext());
        assertNull(mSeeds.getConnectionQueue().getServerURL());
        assertNull(mSeeds.getConnectionQueue().getAppKey());
        assertNull(mSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mSeeds.getTimerService());
        assertNull(mSeeds.getEventQueue());
        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testOnStart_initNotCalled() {
        try {
            mUninitedSeeds.onStart();
            fail("expected calling onStart before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStart_firstCall() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        mSeeds.onStart();

        assertEquals(1, mSeeds.getActivityCount());
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        assertTrue(prevSessionDurationStartTime > 0);
        assertTrue(prevSessionDurationStartTime <= System.nanoTime());
        verify(mockConnectionQueue).beginSession();
    }

    public void testOnStart_subsequentCall() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        mSeeds.onStart(); // first call to onStart
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        mSeeds.onStart(); // second call to onStart

        assertEquals(2, mSeeds.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mSeeds.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).beginSession();
    }

    public void testOnStop_initNotCalled() {
        try {
            mUninitedSeeds.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStop_unbalanced() {
        try {
            mSeeds.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStop_reallyStopping_emptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        mSeeds.onStart();
        mSeeds.onStop();

        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).endSession(0);
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    public void testOnStop_reallyStopping_nonEmptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mSeeds.setEventQueue(mockEventQueue);

        when(mockEventQueue.size()).thenReturn(1);
        final String eventStr = "blahblahblahblah";
        when(mockEventQueue.events()).thenReturn(eventStr);

        mSeeds.onStart();
        mSeeds.onStop();

        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue).endSession(0);
        verify(mockConnectionQueue).recordEvents(eventStr);
    }

    public void testOnStop_notStopping() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        mSeeds.onStart();
        mSeeds.onStart();
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        mSeeds.onStop();

        assertEquals(1, mSeeds.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mSeeds.getPrevSessionDurationStartTime());
        verify(mockConnectionQueue, times(0)).endSession(anyInt());
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    public void testRecordEvent_keyOnly() {
        final String eventKey = "eventKey";
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, 1, 0.0d);
        seeds.recordEvent(eventKey);
        verify(seeds).recordEvent(eventKey, null, 1, 0.0d);
    }

    public void testRecordEvent_keyAndCount() {
        final String eventKey = "eventKey";
        final int count = 42;
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, count, 0.0d);
        seeds.recordEvent(eventKey, count);
        verify(seeds).recordEvent(eventKey, null, count, 0.0d);
    }

    public void testRecordEvent_keyAndCountAndSum() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, count, sum);
        seeds.recordEvent(eventKey, count, sum);
        verify(seeds).recordEvent(eventKey, null, count, sum);
    }

    public void testRecordEvent_keyAndSegmentationAndCount() {
        final String eventKey = "eventKey";
        final int count = 42;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, segmentation, count, 0.0d);
        seeds.recordEvent(eventKey, segmentation, count);
        verify(seeds).recordEvent(eventKey, segmentation, count, 0.0d);
    }

    public void testRecordEvent_initNotCalled() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mUninitedSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalStateException when recordEvent called before init");
        } catch (IllegalStateException ignored) {
            // success
        }
    }

    public void testRecordEvent_nullKey() {
        final String eventKey = null;
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            //noinspection ConstantConditions
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_emptyKey() {
        final String eventKey = "";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_countIsZero() {
        final String eventKey = "";
        final int count = 0;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with count=0");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_countIsNegative() {
        final String eventKey = "";
        final int count = -1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with a negative count");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasNullKey() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put(null, "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasEmptyKey() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasNullValue() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", null);

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasEmptyValue() {
        final String eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<String, String>(1);
        segmentation.put("segkey1", "segvalue1");

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mSeeds.setEventQueue(mockEventQueue);

        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).sendEventsIfNeeded();
        seeds.recordEvent(eventKey, segmentation, count, sum);

        verify(mockEventQueue).recordEvent(eventKey, segmentation, count, sum);
        verify(seeds).sendEventsIfNeeded();
    }

    public void testSendEventsIfNeeded_emptyQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.sendEventsIfNeeded();

        verify(mockEventQueue, times(0)).events();
        verifyZeroInteractions(mockConnectionQueue);
    }

    public void testSendEventsIfNeeded_lessThanThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(9);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.sendEventsIfNeeded();

        verify(mockEventQueue, times(0)).events();
        verifyZeroInteractions(mockConnectionQueue);
    }

    public void testSendEventsIfNeeded_equalToThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(10);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.sendEventsIfNeeded();

        verify(mockEventQueue, times(1)).events();
        verify(mockConnectionQueue, times(1)).recordEvents(eventData);
    }

    public void testSendEventsIfNeeded_moreThanThreshold() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(20);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.sendEventsIfNeeded();

        verify(mockEventQueue, times(1)).events();
        verify(mockConnectionQueue, times(1)).recordEvents(eventData);
    }

    public void testOnTimer_noActiveSession() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.onTimer();

        verifyZeroInteractions(mockConnectionQueue, mockEventQueue);
    }

    public void testOnTimer_activeSession_emptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.onStart();
        mSeeds.onTimer();

        verify(mockConnectionQueue).updateSession(0);
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    public void testOnTimer_activeSession_nonEmptyEventQueue() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(1);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.onStart();
        mSeeds.onTimer();

        verify(mockConnectionQueue).updateSession(0);
        verify(mockConnectionQueue).recordEvents(eventData);
    }

    public void testOnTimer_activeSession_emptyEventQueue_sessionTimeUpdatesDisabled() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setDisableUpdateSessionRequests(true);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(0);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.onStart();
        mSeeds.onTimer();

        verify(mockConnectionQueue, times(0)).updateSession(anyInt());
        verify(mockConnectionQueue, times(0)).recordEvents(anyString());
    }

    public void testOnTimer_activeSession_nonEmptyEventQueue_sessionTimeUpdatesDisabled() {
        final ConnectionQueue mockConnectionQueue = mock(ConnectionQueue.class);
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setDisableUpdateSessionRequests(true);

        final EventQueue mockEventQueue = mock(EventQueue.class);
        when(mockEventQueue.size()).thenReturn(1);
        final String eventData = "blahblahblah";
        when(mockEventQueue.events()).thenReturn(eventData);
        mSeeds.setEventQueue(mockEventQueue);

        mSeeds.onStart();
        mSeeds.onTimer();

        verify(mockConnectionQueue, times(0)).updateSession(anyInt());
        verify(mockConnectionQueue).recordEvents(eventData);
    }

    public void testRoundedSecondsSinceLastSessionDurationUpdate() {
        long prevSessionDurationStartTime = System.nanoTime() - 1000000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 2000000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1600000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1200000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());
    }

    public void testIsValidURL_badURLs() {
        assertFalse(Seeds.isValidURL(null));
        assertFalse(Seeds.isValidURL(""));
        assertFalse(Seeds.isValidURL(" "));
        assertFalse(Seeds.isValidURL("blahblahblah.com"));
    }

    public void testIsValidURL_goodURL() {
        assertTrue(Seeds.isValidURL("http://ly.count.android.sdk.test.count.ly"));
    }

    public void testCurrentTimestamp() {
        final int testTimestamp = (int) (System.currentTimeMillis() / 1000l);
        final int actualTimestamp = Seeds.currentTimestamp();
        assertTrue(((testTimestamp - 1) <= actualTimestamp) && ((testTimestamp + 1) >= actualTimestamp));
    }

    public void testSetDisableUpdateSessionRequests() {
        assertFalse(mSeeds.getDisableUpdateSessionRequests());
        mSeeds.setDisableUpdateSessionRequests(true);
        assertTrue(mSeeds.getDisableUpdateSessionRequests());
        mSeeds.setDisableUpdateSessionRequests(false);
        assertFalse(mSeeds.getDisableUpdateSessionRequests());
    }

    public void testLoggingFlag() {
        assertFalse(mUninitedSeeds.isLoggingEnabled());
        mUninitedSeeds.setLoggingEnabled(true);
        assertTrue(mUninitedSeeds.isLoggingEnabled());
        mUninitedSeeds.setLoggingEnabled(false);
        assertFalse(mUninitedSeeds.isLoggingEnabled());
    }
}
