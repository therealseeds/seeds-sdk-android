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

package ly.count.android.sdk.inappmessaging;

import static ly.count.android.sdk.inappmessaging.Const.RESPONSE_ENCODING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


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

	@Override
	InAppMessageResponse parse(final InputStream inputStream, Header[] headers) throws RequestException {

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		final InAppMessageResponse response = new InAppMessageResponse();

		try {
				db = dbf.newDocumentBuilder();
				InputSource src = new InputSource(inputStream);
				if (Log.LOG_AD_RESPONSES) {
					String sResponse = convertStreamToString(inputStream);
					Log.d("InAppMessage RequestPerform HTTP Response: " + sResponse);
					byte[] bytes = sResponse.getBytes(RESPONSE_ENCODING);
					src = new InputSource(new ByteArrayInputStream(bytes));
				}
				src.setEncoding(Const.RESPONSE_ENCODING);
				final Document doc = db.parse(src);

				final Element element = doc.getDocumentElement();

				if (element == null)
					throw new RequestException("Document is not an xml");

				final String errorValue = this.getValue(doc, "error");
				if (errorValue != null)
					throw new RequestException("Error Response received: " + errorValue);

				final String type = element.getAttribute("type");
				element.normalize();
				if ("imageAd".equalsIgnoreCase(type)) {
					response.setType(Const.IMAGE);
					response.setBannerWidth(this.getValueAsInt(doc, "bannerwidth"));
					response.setBannerHeight(this.getValueAsInt(doc, "bannerheight"));
					final ClickType clickType = ClickType.getValue(this.getValue(doc, "clicktype"));
					response.setClickType(clickType);
					response.setClickUrl(this.getValue(doc, "clickurl"));
					response.setImageUrl(this.getValue(doc, "imageurl"));
					response.setRefresh(this.getValueAsInt(doc, "refresh"));
					response.setScale(this.getValueAsBoolean(doc, "scale"));
					response.setSkipPreflight(this.getValueAsBoolean(doc, "skippreflight"));
				} else if ("textAd".equalsIgnoreCase(type)) {
					response.setType(Const.TEXT);
					response.setText(this.getValue(doc, "htmlString"));
					String skipOverlay = this.getAttribute(doc, "htmlString", "skipoverlaybutton");
					if (skipOverlay != null) {
						response.setSkipOverlay(Integer.parseInt(skipOverlay));
					}
					final ClickType clickType = ClickType.getValue(this.getValue(doc, "clicktype"));
					response.setClickType(clickType);
					response.setClickUrl(this.getValue(doc, "clickurl"));
					response.setRefresh(this.getValueAsInt(doc, "refresh"));
					response.setScale(this.getValueAsBoolean(doc, "scale"));
					response.setSkipPreflight(this.getValueAsBoolean(doc, "skippreflight"));
				} else if ("noAd".equalsIgnoreCase(type)) {
					response.setType(Const.NO_AD);
					if (response.getRefresh() <= 0) {
						response.setRefresh(RELOAD_AFTER_NO_AD);
					}
				} else {
					throw new RequestException("Unknown response type " + type);
				}
				//ToDO: remove hard-coded URL
				response.setClickUrl("farm://store");


		} catch (final ParserConfigurationException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (final SAXException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (final IOException e) {
			throw new RequestException("Cannot read Response", e);
		} catch (final Throwable t) {
			throw new RequestException("Cannot read Response", t);
		}

		return response;
	}

	@Override
	InAppMessageResponse parseTestString() throws RequestException {
		return parse(is, null);
	}

	public InAppMessageResponse parseCountlyUri(final InputStream inputStream, Header[] headers) throws RequestException {

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		final InAppMessageResponse response = new InAppMessageResponse();

		try {
			db = dbf.newDocumentBuilder();
			InputSource src = new InputSource(inputStream);

			String sResponse = convertStreamToString(inputStream);
			Log.i("InAppMessage RequestPerform HTTP Response: " + sResponse);

		} catch (final ParserConfigurationException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (final Throwable t) {
			throw new RequestException("Cannot read Response", t);
		}

		return response;
	}

}
