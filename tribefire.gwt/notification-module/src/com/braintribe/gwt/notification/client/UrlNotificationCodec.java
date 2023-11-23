// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gwt.notification.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class UrlNotificationCodec extends AbstractNotificationCodec<String> {
	public UrlNotificationCodec() {
		setJsonCodec(new Codec<JSONObject, String>() {
			@Override
			public JSONObject decode(String encodedValue) throws CodecException {
				JSONValue jsonValue = JSONParser.parseLenient(encodedValue);
				JSONObject jsonObject = jsonValue.isObject();
				if (jsonObject == null)
					throw new CodecException("error casting " + jsonValue + " to JSONObject");
				
				return jsonObject;
			}
			@Override
			public String encode(JSONObject value) throws CodecException {
				throw new UnsupportedOperationException();
			}
			@Override
			public Class<JSONObject> getValueClass() {
				return JSONObject.class;
			}
		});
	}
}
