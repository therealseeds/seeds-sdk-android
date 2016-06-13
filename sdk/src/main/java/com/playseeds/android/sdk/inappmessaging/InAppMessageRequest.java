/*
 *		Copyright 2015 MobFox
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *		Unless required by applicable law or agreed to in writing, software
 *		distributed under the License is distributed on an "AS IS" BASIS,
 *		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *		See the License for the specific language governing permissions and
 *		limitations under the License.
 *
 *		Changes: 	removed video, MRAID and custom ad-specific code
 *					renamed from AdRequest
 */

package com.playseeds.android.sdk.inappmessaging;

import android.net.Uri;

import com.playseeds.android.sdk.DeviceId;

public class InAppMessageRequest {
	private static final int DEVICE_ID_MS_DELAY = 500;  // sleep in ms if device_id not loaded yet
	
	private String userAgent;
	private String userAgent2;
	private String requestURL;
	private String appKey;
	private String deviceId;
	private DeviceId.Type idMode;

	private double longitude = 0.0;
	private double latitude = 0.0;
	private boolean adspaceStrict;

	private String ipAddress;
	private boolean adDoNotTrack = false;
	private String connectionType;
	private long timestamp;

	private String orientation;

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setIdMode(DeviceId.Type idMode) {
		this.idMode = idMode;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setConnectionType(final String connectionType) {
		this.connectionType = connectionType;
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public void setAppKey(final String appKey) {
		this.appKey = appKey;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	public void setUserAgent2(final String userAgent) {
		this.userAgent2 = userAgent;
	}

	public String countlyUriToString() {
		return this.toCountlyUri().toString();
	}


	//TODO: make sure "deviceCategory" gets to server code
	public Uri toCountlyUri() {
		String countlyURL = requestURL;
		String path = "/o/messages";
		final Uri.Builder b = Uri.parse((countlyURL + path)).buildUpon();

		b.appendQueryParameter("app_key", appKey);
		b.appendQueryParameter("orientation", orientation);

		if (deviceId == null || deviceId.isEmpty()) {
			deviceId = Util.getAndroidAdId();
			if (deviceId == null || deviceId.isEmpty()) {
				try {
					Thread.sleep(DEVICE_ID_MS_DELAY);
				} catch (InterruptedException e) {
					Log.e("Sleep interrupted: " + e);
				}
				deviceId = Util.getAndroidAdId();
			}
		}

		if (deviceId == null || deviceId.isEmpty()) {
			Log.e("Device Id could not be set");
		}
		b.appendQueryParameter("device_id", deviceId);
		//b.appendQueryParameter("device_id_type", idMode.toString()); //currently unused

		return b.build();
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public void setAdspaceStrict(boolean adspaceStrict) {
		this.adspaceStrict = adspaceStrict;
	}

	public void setAdDoNotTrack(boolean adDoNotTrack) {
		this.adDoNotTrack = adDoNotTrack;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
}