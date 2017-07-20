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

import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EventTests extends AndroidTestCase {
    public void testConstructor() {
        final Event event = new Event();
        assertNull(event.eventName);
        assertNull(event.attributes);
        assertEquals(0, event.count);
        assertEquals(0, event.timestamp);
        assertEquals(0.0d, event.sum);
    }

    public void testEqualsAndHashCode() {
        final Event event1 = new Event();
        final Event event2 = new Event();
        assertFalse(event1.equals(null));
        assertFalse(event1.equals(new Object()));
        assertTrue(event1.equals(event2));
        assertEquals(event1.hashCode(), event2.hashCode());

        event1.eventName = "eventKey";
        assertFalse(event1.equals(event2));
        assertFalse(event2.equals(event1));
        assertTrue(event1.hashCode() != event2.hashCode());

        event2.eventName = "eventKey";
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
        assertEquals(event1.hashCode(), event2.hashCode());

        event1.timestamp = 1234;
        assertFalse(event1.equals(event2));
        assertFalse(event2.equals(event1));
        assertTrue(event1.hashCode() != event2.hashCode());

        event2.timestamp = 1234;
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
        assertEquals(event1.hashCode(), event2.hashCode());

        event1.attributes = new HashMap<>();
        assertFalse(event1.equals(event2));
        assertFalse(event2.equals(event1));
        assertTrue(event1.hashCode() != event2.hashCode());

        event2.attributes = new HashMap<>();
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
        assertEquals(event1.hashCode(), event2.hashCode());

        event1.attributes.put("segkey", "segvalue");
        assertFalse(event1.equals(event2));
        assertFalse(event2.equals(event1));
        assertTrue(event1.hashCode() != event2.hashCode());

        event2.attributes.put("segkey", "segvalue");
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
        assertEquals(event1.hashCode(), event2.hashCode());

        event1.sum = 3.2;
        event2.count = 42;
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    public void testToJSON_nullSegmentation() throws JSONException {
        final Event event = new Event();
        event.eventName = "eventKey";
        event.timestamp = 1234;
        event.count = 42;
        event.sum = 3.2;
        final JSONObject jsonObj = event.toJSON();
        assertEquals(4, jsonObj.length());
        assertEquals(event.eventName, jsonObj.getString("eventName"));
        assertEquals(event.timestamp, jsonObj.getInt("timestamp"));
        assertEquals(event.count, jsonObj.getInt("count"));
        assertEquals(event.sum, jsonObj.getDouble("sum"));
    }

    public void testToJSON_emptySegmentation() throws JSONException {
        final Event event = new Event();
        event.eventName = "eventKey";
        event.timestamp = 1234;
        event.count = 42;
        event.sum = 3.2;
        event.attributes = new HashMap<>();
        final JSONObject jsonObj = event.toJSON();
        assertEquals(5, jsonObj.length());
        assertEquals(event.eventName, jsonObj.getString("eventName"));
        assertEquals(event.timestamp, jsonObj.getInt("timestamp"));
        assertEquals(event.count, jsonObj.getInt("count"));
        assertEquals(event.sum, jsonObj.getDouble("sum"));
        assertEquals(0, jsonObj.getJSONObject("attributes").length());
    }

    public void testToJSON_withSegmentation() throws JSONException {
        final Event event = new Event();
        event.eventName = "eventKey";
        event.timestamp = 1234;
        event.count = 42;
        event.sum = 3.2;
        event.attributes = new HashMap<>();
        event.attributes.put("segkey", "segvalue");
        final JSONObject jsonObj = event.toJSON();
        assertEquals(5, jsonObj.length());
        assertEquals(event.eventName, jsonObj.getString("eventName"));
        assertEquals(event.timestamp, jsonObj.getInt("timestamp"));
        assertEquals(event.count, jsonObj.getInt("count"));
        assertEquals(event.sum, jsonObj.getDouble("sum"));
        assertEquals(1, jsonObj.getJSONObject("attributes").length());
        assertEquals(event.attributes.get("segkey"), jsonObj.getJSONObject("attributes").getString("segkey"));
    }

    public void testToJSON_sumNaNCausesJSONException() throws JSONException {
        final Event event = new Event();
        event.eventName = "eventKey";
        event.timestamp = 1234;
        event.count = 42;
        event.sum = Double.NaN;
        event.attributes = new HashMap<>();
        event.attributes.put("segkey", "segvalue");
        final JSONObject jsonObj = event.toJSON();
        assertEquals(4, jsonObj.length());
        assertEquals(event.eventName, jsonObj.getString("eventName"));
        assertEquals(event.timestamp, jsonObj.getInt("timestamp"));
        assertEquals(event.count, jsonObj.getInt("count"));
        assertEquals(1, jsonObj.getJSONObject("attributes").length());
        assertEquals(event.attributes.get("segkey"), jsonObj.getJSONObject("attributes").getString("segkey"));
    }

    public void testFromJSON_nullJSONObj() {
        try {
            Event.fromJSON(null);
            fail("Expected NPE when calling Event.fromJSON with null");
        } catch (NullPointerException ignored) {
            // success
        }
    }

    public void testFromJSON_noKeyCausesJSONException() {
        final JSONObject jsonObj = new JSONObject();
        assertNull(Event.fromJSON(jsonObj));
    }

    public void testFromJSON_nullKey() throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", JSONObject.NULL);
        assertNull(Event.fromJSON(jsonObj));
    }

    public void testFromJSON_emptyKey() throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", "");
        assertNull(Event.fromJSON(jsonObj));
    }

    public void testFromJSON_keyOnly() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_keyOnly_nullOtherValues() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", JSONObject.NULL);
        jsonObj.put("count", JSONObject.NULL);
        jsonObj.put("sum", JSONObject.NULL);
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_noSegmentation() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_nullSegmentation() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        jsonObj.put("attributes", JSONObject.NULL);
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_segmentationNotADictionary() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        jsonObj.put("attributes", 1234);
        assertNull(Event.fromJSON(jsonObj));
    }

    public void testFromJSON_emptySegmentation() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        expected.attributes = new HashMap<>();
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        jsonObj.put("attributes", new JSONObject(expected.attributes));
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_withSegmentation() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        expected.attributes = new HashMap<>();
        expected.attributes.put("segkey", "segvalue");
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        jsonObj.put("attributes", new JSONObject(expected.attributes));
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }

    public void testFromJSON_withSegmentation_nonStringValue() throws JSONException {
        final Event expected = new Event();
        expected.eventName = "eventKey";
        expected.timestamp = 1234;
        expected.count = 42;
        expected.sum = 3.2;
        expected.attributes = new HashMap<>();
        expected.attributes.put("segkey", "1234");
        final Map<Object, Object> badMap = new HashMap<>();
        badMap.put("segkey", 1234); // JSONObject.getString will end up converting this to the string "1234"
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("eventName", expected.eventName);
        jsonObj.put("timestamp", expected.timestamp);
        jsonObj.put("count", expected.count);
        jsonObj.put("sum", expected.sum);
        jsonObj.put("attributes", new JSONObject(badMap));
        final Event actual = Event.fromJSON(jsonObj);
        assertEquals(expected, actual);
        assertEquals(expected.count, actual.count);
        assertEquals(expected.sum, actual.sum);
    }
}
