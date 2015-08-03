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
 *		Changes: 	removed video, custom event and MRAID-related code
 *					renamed from RequestGeneralAd
 */

package com.playseeds.android.sdk.inappmessaging;

import com.playseeds.android.sdk.Seeds;

import org.apache.http.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;


public class RequestGeneralInAppMessage extends RequestInAppMessage<InAppMessageResponse> {

	private final static int RELOAD_AFTER_NO_AD = 20;

	public RequestGeneralInAppMessage() {
	}

	public RequestGeneralInAppMessage(InputStream xmlArg) {
		is = xmlArg;
		Log.d("Parse is null" + (is == null));
	}

	private int getInteger(final String text) {
		if (text == null)
			return 0;
		try {
			return Integer.parseInt(text);
		} catch (final NumberFormatException ex) {
			// do nothing, 0 is returned
		}
		return 0;
	}

	private String getAttribute(final Document document, final String elementName, final String attributeName) {

		NodeList nodeList = document.getElementsByTagName(elementName);
		final Element element = (Element) nodeList.item(0);
		if (element != null) {
			String attribute = element.getAttribute(attributeName);
			if (attribute.length() != 0) {
				return attribute;
			}
		}
		return null;
	}

	private String getValue(final Document document, final String name) {

		NodeList nodeList = document.getElementsByTagName(name);
		final Element element = (Element) nodeList.item(0);
		if (element != null) {
			nodeList = element.getChildNodes();
			if (nodeList.getLength() > 0)
				// if (Log.isLoggable(TAG, Log.DEBUG)) {
				// Log.d(TAG, "node value for " + name + ": " +
				// nodeList.item(0).getNodeValue());
				// }
				return nodeList.item(0).getNodeValue();
		}
		return null;
	}


	private boolean getValueAsBoolean(final Document document, final String name) {
		return "yes".equalsIgnoreCase(this.getValue(document, name));
	}

	private int getValueAsInt(final Document document, final String name) {
		return this.getInteger(this.getValue(document, name));
	}

	private String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}



	public InAppMessageResponse parseCountlyJSON(final InputStream inputStream, Header[] headers) throws RequestException {

		Log.i("Starting parseCountlyJSON");

		final InAppMessageResponse response = new InAppMessageResponse();

		response.setType(Const.TEXT);

//		String skipOverlay = this.getAttribute(doc, "htmlString", "skipoverlaybutton");
//		if (skipOverlay != null) {
//			response.setSkipOverlay(Integer.parseInt(skipOverlay));
//		}
		final ClickType clickType = ClickType.getValue("inapp");
		response.setClickType(clickType);
		response.setRefresh(60);
		response.setScale(false);
		response.setSkipPreflight(true);
		try {

			//String sResponse = convertStreamToString(inputStream);
			//Log.i("InAppMessage RequestPerform HTTP Response: " + sResponse);
			JsonReader jsonReader = Json.createReader(inputStream);
			JsonObject jsonObject = jsonReader.readObject();

			response.setText(jsonObject.getString("htmlString"));
			response.setClickUrl(jsonObject.getString("clickurl"));

			String messageVariant = jsonObject.getString("messageVariant");

			if (messageVariant != null && !messageVariant.equals("false")) {
				Seeds.sharedInstance().setA_bTestingOn(true);
				Seeds.sharedInstance().setMessageVariantName(messageVariant);
			}

			String payingUser = jsonObject.getString()

			jsonReader.close();

		} catch (final Throwable t) {
			Log.e(t.toString());
			throw new RequestException("Cannot read Response", t);
		}

		Log.i("InAppMessageResponse: " + response);
		return response;
	}

}
