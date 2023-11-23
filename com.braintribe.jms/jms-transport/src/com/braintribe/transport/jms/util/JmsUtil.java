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
package com.braintribe.transport.jms.util;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.transport.jms.message.IMessageContext;

public class JmsUtil {

	public static void logError(Logger logger, Throwable ext, String text) {
		log(logger, LogLevel.ERROR, ext, text);
	}
	public static void log(Logger logger, LogLevel logLevel, Throwable ext, String text) {
		logger.log(logLevel, text, ext);
		if ((ext != null) && (ext instanceof JMSException)) {
			logger.log(logLevel, "Linked Exception:", ((JMSException) ext).getLinkedException());
		}
	}

	public static String getMessageText(IMessageContext messageContext) throws Exception {
		String msgText = null;
		
		Message msg = messageContext.getMessage();
		if (msg instanceof MapMessage) {
			msgText = ((MapMessage) msg).getString("body");
		} else if (msg instanceof TextMessage) {
			msgText = ((TextMessage) msg).getText();
		} else if (msg instanceof BytesMessage) {
			msgText = ((BytesMessage) msg).readUTF();
		} else {
			throw new Exception("Unsupported JMS message type " + msg.getClass());
		}
		
		return msgText;
	}
	

	public static Object getMessageContent(IMessageContext messageContext) throws Exception {
		Message msg = messageContext.getMessage();
		if (msg instanceof MapMessage) {
			Map<String,Object> mapContent = new HashMap<String,Object>();
			MapMessage mm = (MapMessage) msg;
			for (Enumeration<?> e = mm.getMapNames(); e.hasMoreElements(); ) {
				String key = (String) e.nextElement();
				Object value = mm.getObject(key);
				mapContent.put(key, value);
			}
			return mapContent;
		} else if (msg instanceof TextMessage) {
			String msgText = ((TextMessage) msg).getText();
			return msgText;
		} else if (msg instanceof BytesMessage) {
			BytesMessage bm = (BytesMessage) msg;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = bm.readBytes(buffer);
			while (len != -1) {
				baos.write(buffer, 0, len);
				len = bm.readBytes(buffer);
			}
			byte[] msgBytes = baos.toByteArray();
			return msgBytes;
		} else if (msg instanceof ObjectMessage) {
			ObjectMessage om = (ObjectMessage) msg;
			Object content = om.getObject();
			return content;
		} else {
			throw new Exception("Unsupported JMS message type " + msg.getClass());
		}

	}
	
	
	public static String generateCorrelationID() {
		String correlationID = getRandomString() + getRandomString(10);
		return correlationID;
	}
	
	public static String getRandomString() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String id = sdf.format(d);

		Random rnd = new Random();
		long lng = rnd.nextLong();
		lng = (lng < 0) ? (-lng) : (lng);
		id += lng;

		return id;
	}
	
	public static String getRandomString(int length) {
		Random r = new Random();
		String result = "";
		while (result.length() < length) {
			long l = r.nextLong();
			if (l < 0) {
				l = -l;
			}
			result = result + l;
		}
		return result.substring(0, length);
	}
	public static String getBuildVersion() {
		return "$Build_Version$ $Id: JmsUtil.java 92352 2016-03-11 14:34:02Z roman.kurmanowytsch $";
	}
}
