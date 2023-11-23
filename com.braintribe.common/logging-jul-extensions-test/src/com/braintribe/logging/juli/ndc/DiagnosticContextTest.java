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
package com.braintribe.logging.juli.ndc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.logging.StreamHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.logging.juli.formatters.simple.SimpleFormatter;

public class DiagnosticContextTest {

	protected ByteArrayOutputStream logBuffer = null;
	protected StreamHandler streamHandler = null;

	@Before
	public void initialize() throws Exception {

		if (this.streamHandler == null) {
			this.logBuffer = new ByteArrayOutputStream();
			this.streamHandler = new StreamHandler(this.logBuffer, new SimpleFormatter("%4$-7s %7$-33s '%5$s' %6$s [%9$s - %10$s]%n"));

			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DiagnosticContextTest.class.getName());
			logger.addHandler(this.streamHandler);
		}
	}

	@Ignore
	protected String getLastLogLine() throws Exception {
		this.streamHandler.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.logBuffer.toByteArray()), "UTF-8"));
		String line = null;
		String lastLine = null;
		while ((line = br.readLine()) != null) {
			lastLine = line;
		}
		br.close();
		this.logBuffer.reset();
		if (lastLine != null) {
			lastLine = lastLine.trim();
		}
		return lastLine;
	}

	@Test
	public void testNdcSingleElement() throws Exception {

		Logger logger = Logger.getLogger(DiagnosticContextTest.class);
		logger.removeContext();
		logger.clearMdc();

		logger.pushContext("test");
		logger.info("Test Message");

		String lastLine = this.getLastLogLine();

		Assert.assertEquals(true, lastLine.endsWith("[test - ]"));
	}

	@Test
	public void testNdcMultipleElements() throws Exception {

		Logger logger = Logger.getLogger(DiagnosticContextTest.class);
		logger.removeContext();
		logger.clearMdc();

		logger.pushContext("hello");
		logger.pushContext("world");
		logger.info("Test Message");

		String lastLine = this.getLastLogLine();

		Assert.assertEquals(true, lastLine.endsWith("[world,hello - ]"));
	}

	@Test
	public void testMdcSingleElement() throws Exception {

		Logger logger = Logger.getLogger(DiagnosticContextTest.class);
		logger.removeContext();
		logger.clearMdc();

		logger.put("hello", "world");
		logger.info("Test Message");

		String lastLine = this.getLastLogLine();

		Assert.assertEquals(true, lastLine.endsWith("[ - hello=world]"));
	}

	@Test
	public void testMdcMultipleElements() throws Exception {

		Logger logger = Logger.getLogger(DiagnosticContextTest.class);
		logger.removeContext();
		logger.clearMdc();

		logger.put("key1", "value1");
		logger.put("key2", "value2");
		logger.info("Test Message");

		String lastLine = this.getLastLogLine();

		Assert.assertEquals(true, lastLine.contains("key1=value1"));
		Assert.assertEquals(true, lastLine.contains("key2=value2"));
	}
}
