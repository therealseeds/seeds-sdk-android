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

package ly.count.android.sdk.inappmessaging;

import java.util.List;

import android.net.Uri;
import android.os.Build;

public class InAppMessageRequest {
	private static final String REQUEST_TYPE_ANDROID = "android_app";
	
	private String userAgent;
	private String userAgent2;
	private String headers;
	private String listAds;
	private String requestURL;
	private String protocolVersion;
	private String appKey;
	private double longitude = 0.0;
	private double latitude = 0.0;
	private boolean adspaceStrict;
	private int adspaceWidth;
	private int adspaceHeight;
	private Gender gender;
	private int userAge;
	private List<String> keywords;

	private String ipAddress;
	private String androidAdId = "";
	private boolean adDoNotTrack = false;
	private String connectionType;
	private long timestamp;

	public String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	public String getConnectionType() {
		return this.connectionType;
	}

	public String getDeviceMode() {
		return Build.MODEL;
	}

	public String getHeaders() {
		if (this.headers == null)
			return "";
		return this.headers;
	}

	public String getIpAddress() {
		if (this.ipAddress == null)
			return "";
		return this.ipAddress;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public String getListAds() {
		if (this.listAds != null)
			return this.listAds;
		else
			return "";
	}

	public double getLongitude() {
		return this.longitude;
	}

	public String getProtocolVersion() {
		if (this.protocolVersion == null)
			return Const.VERSION;
		else
			return this.protocolVersion;
	}

	public String getAppKey() {
		if (this.appKey == null)
			return "";
		return this.appKey;
	}

	public String getRequestType() {
		return InAppMessageRequest.REQUEST_TYPE_ANDROID;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getUserAgent() {
		if (this.userAgent == null)
			return "";
		return this.userAgent;
	}

	public String getUserAgent2() {
		if (this.userAgent2 == null)
			return "";
		return this.userAgent2;
	}

	public void setConnectionType(final String connectionType) {
		this.connectionType = connectionType;
	}



	public void setHeaders(final String headers) {
		this.headers = headers;
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setListAds(final String listAds) {
		this.listAds = listAds;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public void setProtocolVersion(final String protocolVersion) {
		this.protocolVersion = protocolVersion;
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

//	@Override
//	public String toString() {
//
//		return this.toUri().toString();
//	}

	public String countlyUriToString() {
		return this.toCountlyUri().toString();
	}

//	public Uri toUri() {
//		final Uri.Builder b = Uri.parse(this.getRequestURL()).buildUpon();
//		Random r = new Random();
//		int random = r.nextInt(50000);
//
//		b.appendQueryParameter("rt", this.getRequestType());
//		b.appendQueryParameter("v", this.getProtocolVersion());
//		b.appendQueryParameter("i", this.getIpAddress());
//		b.appendQueryParameter("u", this.getUserAgent());
//		b.appendQueryParameter("u2", this.getUserAgent2());
//		b.appendQueryParameter("s", this.getAppKey());
//		b.appendQueryParameter("o_andadvid", androidAdId);
//		b.appendQueryParameter("o_andadvdnt", (adDoNotTrack ? "1" : "0"));
//		b.appendQueryParameter("r_random", Integer.toString(random));
//		b.appendQueryParameter("t", Long.toString(this.getTimestamp()));
//		b.appendQueryParameter("connection_type", this.getConnectionType());
//		b.appendQueryParameter("listads", this.getListAds());
//		b.appendQueryParameter("c_customevents", "1");
///*		b.appendQueryParameter("c_mraid", "1");*/
///*		if(isVideoRequest) {
//			b.appendQueryParameter("r_type", "video");
//			b.appendQueryParameter("r_resp", "vast20");
//			if(videoMaxDuration != 0) {
//				b.appendQueryParameter("v_dur_max", Integer.toString(videoMaxDuration));
//			}
//			if(videoMinDuration != 0) {
//				b.appendQueryParameter("v_dur_min", Integer.toString(videoMinDuration));
//			}
//		} else {*/
//			b.appendQueryParameter("r_type", "banner");
///*		}*/
//
//		if(userAge != 0) {
//			b.appendQueryParameter("demo_age", Integer.toString(userAge));
//		}
//
//		if (gender != null) {
//			b.appendQueryParameter("demo_gender", gender.getServerParam());
//		}
//
//		if(keywords != null && !keywords.isEmpty()) {
//			String parameter = TextUtils.join(", ", keywords);
//			b.appendQueryParameter("demo_keywords", parameter);
//		}
//
//		b.appendQueryParameter("u_wv", this.getUserAgent());
//		b.appendQueryParameter("u_br", this.getUserAgent());
//		if(longitude != 0 && latitude != 0) {
//			b.appendQueryParameter("longitude", Double.toString(longitude));
//			b.appendQueryParameter("latitude", Double.toString(latitude));
//		}
//
//		if(adspaceHeight > 0 && adspaceWidth > 0) {
//			if(adspaceStrict) {
//				b.appendQueryParameter("adspace_strict", "1");
//			} else {
//				b.appendQueryParameter("adspace_strict", "0");
//			}
//			b.appendQueryParameter("adspace_width", Integer.toString(adspaceWidth));
//			b.appendQueryParameter("adspace_height", Integer.toString(adspaceHeight));
//		}
//
//		return b.build();
//	}

	//TODO: replace hardcoded parameters
	public Uri toCountlyUri() {
		String countlyURL = requestURL;
		String path = "/o/messages";
		final Uri.Builder b = Uri.parse((countlyURL + path)).buildUpon();

		b.appendQueryParameter("app_key", appKey);
		b.appendQueryParameter("orientation", "portrait");
		b.appendQueryParameter("deviceCategory", "androidPhone");

		return b.build();
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public boolean isAdspaceStrict() {
		return adspaceStrict;
	}

	public void setAdspaceStrict(boolean adspaceStrict) {
		this.adspaceStrict = adspaceStrict;
	}

	public int getAdspaceWidth() {
		return adspaceWidth;
	}

	public void setAdspaceWidth(int adspaceWidth) {
		this.adspaceWidth = adspaceWidth;
	}

	public int getAdspaceHeight() {
		return adspaceHeight;
	}

	public void setAdspaceHeight(int adspaceHeight) {
		this.adspaceHeight = adspaceHeight;
	}

	public String getAndroidAdId() {
		return androidAdId;
	}

	public void setAndroidAdId(String androidAdId) {
		this.androidAdId = androidAdId;
	}

	public Boolean hasAdDoNotTrack() {
		return adDoNotTrack;
	}

	public void setAdDoNotTrack(boolean adDoNotTrack) {
		this.adDoNotTrack = adDoNotTrack;
	}

/*	public boolean isVideoRequest() {
		return isVideoRequest;
	}

	public void setVideoRequest(boolean isVideoRequest) {
		this.isVideoRequest = isVideoRequest;
	}

	public int getVideoMinDuration() {
		return videoMinDuration;
	}

	public void setVideoMinDuration(int videoMinDuration) {
		this.videoMinDuration = videoMinDuration;
	}

	public int getVideoMaxDuration() {
		return videoMaxDuration;
	}

	public void setVideoMaxDuration(int videoMaxDuration) {
		this.videoMaxDuration = videoMaxDuration;
	}
*/
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

}