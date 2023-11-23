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
package com.braintribe.logging.juli.formatters.json;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.StreamHandler;

import org.junit.Test;

import com.braintribe.logging.Logger;

/**
 * Provides tests for {@link JsonFormatter}
 */
public class JsonFormatterTest {

	@Test
	public void testSimple() throws Exception {
		ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
		StreamHandler streamHandler = new StreamHandler(logBuffer,
				new JsonFormatter("date,level,message,throwable", JsonFormatter.DATEFORMAT_PROPERTY_DEFAULT));

		java.util.logging.Logger javaUtilLogger = java.util.logging.Logger.getLogger(JsonFormatterTest.class.getName());
		javaUtilLogger.addHandler(streamHandler);
		logBuffer.reset();

		Logger logger = Logger.getLogger(JsonFormatterTest.class);
		String firstRecordMessage = "Hello, world!";
		String secondRecordMessage = "some error";
		String secondRecordThrowableMessage = "some exception";

		logger.info(firstRecordMessage);
		logger.error(secondRecordMessage, new RuntimeException(secondRecordThrowableMessage));

		List<String> loggedLines = getLoggedLines(logBuffer, streamHandler);
		assertThat(loggedLines).hasSize(2);

		System.out.println(loggedLines.get(0));

		// we expect something like
		// "{"date":"2018-09-14 14:58:14.805","level":"INFO","message":"Hello, world!"}"
		assertThat(loggedLines.get(0)).startsWith("{\"date\":\"2");
		assertThat(loggedLines.get(0)).contains("\"level\":\"INFO\",\"message\":\"Hello, world!\"");
		assertThat(loggedLines.get(1)).contains(secondRecordMessage).contains(secondRecordThrowableMessage);
	}

	@Test
	public void testDefaultFields() throws Exception {
		ByteArrayOutputStream logBuffer = new ByteArrayOutputStream();
		StreamHandler streamHandler = new StreamHandler(logBuffer,
				new JsonFormatter(JsonFormatter.FIELDS_PROPERTY_DEFAULT, JsonFormatter.DATEFORMAT_PROPERTY_DEFAULT));

		java.util.logging.Logger javaUtilLogger = java.util.logging.Logger.getLogger(JsonFormatterTest.class.getName());
		javaUtilLogger.addHandler(streamHandler);
		logBuffer.reset();

		Logger logger = Logger.getLogger(JsonFormatterTest.class);
		String recordMessage = "default fields test";
		String recordThrowableMessage = "default fields exception";

		logger.error(recordMessage, new RuntimeException(recordThrowableMessage));

		List<String> loggedLines = getLoggedLines(logBuffer, streamHandler);

		System.out.println(loggedLines.get(0));

		assertThat(loggedLines).hasSize(1);
		String logLine = loggedLines.get(0);

		Arrays.asList(JsonFormatter.FIELDS_PROPERTY_DEFAULT.split(",")).forEach(fieldName -> assertThat(logLine).contains(fieldName));
	}

	private List<String> getLoggedLines(ByteArrayOutputStream logBuffer, StreamHandler streamHandler) throws Exception {
		streamHandler.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(logBuffer.toByteArray()), "UTF-8"));
		List<String> bufferList = new ArrayList<>();
		String line = null;
		while ((line = br.readLine()) != null) {
			bufferList.add(line);
		}
		br.close();
		return bufferList;
	}

}
