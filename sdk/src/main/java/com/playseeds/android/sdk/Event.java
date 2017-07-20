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

import android.util.Log;

import com.playseeds.android.sdk.new_api.events.Events;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the data for a single Count.ly custom event instance.
 * It also knows how to read & write itself to the Count.ly custom event JSON syntax.
 * See the following link for more info:
 * https://count.ly/resources/reference/custom-events
 */
public class Event {
    private static final String KEY_SEGMENTATION = "attributes";
    private static final String KEY_EVENT_NAME = "eventName";
    private static final String KEY_COUNT = "count";
    private static final String KEY_SUM = "sum";
    private static final String KEY_TIMESTAMP = "timestamp";

    public String eventName;
    public Map<String, String> attributes;
    public int count;
    public double sum;
    public int timestamp;

    public Event(String eventName, Map<String, String> attributes, int count) {
        this.eventName = eventName;
        this.attributes = attributes;
        this.count = count;
    }

    public Event(String eventName, Map<String, String> attributes, int count, double sum) {
        this.eventName = eventName;
        this.attributes = attributes;
        this.count = count;
        this.sum = sum;
    }

    public Event(){
        attributes = new ConcurrentHashMap<>();
    }

    /**
     * Creates and returns a JSONObject containing the event data from this object.
     * @return a JSONObject containing the event data from this object
     */
    JSONObject toJSON() {
        final JSONObject json = new JSONObject();

        try {
            json.put(KEY_EVENT_NAME, eventName);
            json.put(KEY_COUNT, count);
            json.put(KEY_TIMESTAMP, timestamp);

            if (attributes != null) {
                json.put(KEY_SEGMENTATION, new JSONObject(attributes));
            }

            // we put in the sum last, the only reason that a JSONException would be thrown
            // would be if sum is NaN or infinite, so in that case, at least we will return
            // a JSON object with the rest of the fields populated
            json.put(KEY_SUM, sum);
        }
        catch (JSONException e) {
            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.w(Seeds.TAG, "Got exception converting an Event to JSON", e);
            }
        }

        return json;
    }

    /**
     * Factory method to create an Event from its JSON representation.
     * @param json JSON object to extract event data from
     * @return Event object built from the data in the JSON or null if the "eventName" value is not
     *         present or the empty string, or if a JSON exception occurs
     * @throws NullPointerException if JSONObject is null
     */
    static Event fromJSON(final JSONObject json) {
        Event event = new Event();

        try {
            if (!json.isNull(KEY_EVENT_NAME)) {
                event.eventName = json.getString(KEY_EVENT_NAME);
            }
            event.count = json.optInt(KEY_COUNT);
            event.sum = json.optDouble(KEY_SUM, 0.0d);
            event.timestamp = json.optInt(KEY_TIMESTAMP);

            if (!json.isNull(KEY_SEGMENTATION)) {
                final JSONObject segm = json.getJSONObject(KEY_SEGMENTATION);
                final HashMap<String, String> segmentation = new HashMap<>(segm.length());
                final Iterator nameItr = segm.keys();
                while (nameItr.hasNext()) {
                    final String key = (String) nameItr.next();
                    if (!segm.isNull(key)) {
                        segmentation.put(key, segm.getString(key));
                    }
                }
                event.attributes = segmentation;
            }
        }
        catch (JSONException e) {
            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.w(Seeds.TAG, "Got exception converting JSON to an Event", e);
            }
            event = null;
        }

        return (event != null && event.eventName != null && event.eventName.length() > 0) ? event : null;
    }

    public void putAttribute(String key, String value){
        attributes.put(key, value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Event)) {
            return false;
        }

        final Event e = (Event) o;

        return (eventName == null ? e.eventName == null : eventName.equals(e.eventName)) &&
               timestamp == e.timestamp &&
               (attributes == null ? e.attributes == null : attributes.equals(e.attributes));
    }

    @Override
    public int hashCode() {
        return (eventName != null ? eventName.hashCode() : 1) ^
               (attributes != null ? attributes.hashCode() : 1) ^
               (timestamp != 0 ? timestamp : 1);
    }
}
