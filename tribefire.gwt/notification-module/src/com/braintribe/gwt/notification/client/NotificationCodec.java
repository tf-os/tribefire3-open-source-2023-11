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

import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * This codec decodec a {@link JavaScriptObject} via {@link JSONObject} to
 * a {@link Notification} instance. It uses sub codecs configured by {@link #setTypeCodecs(Map)}
 * to decode the notification data depending on the type. 
 * @author Dirk
 *
 */
public class NotificationCodec extends AbstractNotificationCodec<JavaScriptObject> {
	
	public NotificationCodec() {
		setJsonCodec(new Codec<JSONObject, JavaScriptObject>() {
			@Override
			public JSONObject decode(JavaScriptObject encodedValue) throws CodecException {
				try {
					JSONObject jsonObject = new JSONObject(encodedValue);
					return jsonObject;
				}
				catch (Exception e) {
					throw new CodecException("exception while decoding JavaScriptObject to JSONObject", e);
				}
			}
			
			@Override
			public JavaScriptObject encode(JSONObject value) throws CodecException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Class<JSONObject> getValueClass() {
				return JSONObject.class;
			}
		});
	}
}
