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
package com.braintribe.logging.juli.filters.logger;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.logging.juli.formatters.simple.SimpleFormatter;

public class LoggerFilterTest {

	protected ByteArrayOutputStream logBuffer = null;
	protected StreamHandler streamHandler = null;

	public void addStreamHandler(String loggerName) throws Exception {
		if (this.logBuffer == null) {
			this.logBuffer = new ByteArrayOutputStream();
			this.streamHandler = new StreamHandler(this.logBuffer, new SimpleFormatter("%4$-7s %7$-33s '%5$s' %6$s [%9$s - %10$s]%n"));
			this.streamHandler.setLevel(Level.ALL);
		}

		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
		logger.addHandler(this.streamHandler);
	}

	@Test
	public void testParser() throws Exception {
		LoggerFilter lf = new LoggerFilter(false);
		lf.parseEnabledLogLevels("a.b.c, a.b.d@INFO, a.b.e@INFO-, a.b.f@-WARNING, a.b.g@INFO-WARNING, a.b.h");
		Map<String, EnabledLogLevels> enabledLogLevels = lf.getEnabledLogLevels();

		EnabledLogLevels ell = enabledLogLevels.get("a.b.c");
		assertThat(ell.enabled(createLogRecord("a.b.c", Level.FINEST))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.c", Level.SEVERE))).isEqualTo(true);

		ell = enabledLogLevels.get("a.b.d");
		assertThat(ell.enabled(createLogRecord("a.b.d", Level.FINEST))).isEqualTo(false);
		assertThat(ell.enabled(createLogRecord("a.b.d", Level.INFO))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.d", Level.SEVERE))).isEqualTo(false);

		ell = enabledLogLevels.get("a.b.e");
		assertThat(ell.enabled(createLogRecord("a.b.e", Level.FINEST))).isEqualTo(false);
		assertThat(ell.enabled(createLogRecord("a.b.e", Level.INFO))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.e", Level.SEVERE))).isEqualTo(true);

		ell = enabledLogLevels.get("a.b.f");
		assertThat(ell.enabled(createLogRecord("a.b.f", Level.FINEST))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.f", Level.INFO))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.f", Level.WARNING))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.f", Level.SEVERE))).isEqualTo(false);

		ell = enabledLogLevels.get("a.b.g");
		assertThat(ell.enabled(createLogRecord("a.b.g", Level.FINEST))).isEqualTo(false);
		assertThat(ell.enabled(createLogRecord("a.b.g", Level.INFO))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.g", Level.WARNING))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.g", Level.SEVERE))).isEqualTo(false);

		ell = enabledLogLevels.get("a.b.h");
		assertThat(ell.enabled(createLogRecord("a.b.h", Level.FINEST))).isEqualTo(true);
		assertThat(ell.enabled(createLogRecord("a.b.h", Level.SEVERE))).isEqualTo(true);

		assertThat(lf.isLoggable(createLogRecord("a.b.c", Level.FINEST))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.c", Level.SEVERE))).isEqualTo(true);

		assertThat(lf.isLoggable(createLogRecord("a.b.d", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.d", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.d", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.e", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.e", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.e", Level.SEVERE))).isEqualTo(true);

		assertThat(lf.isLoggable(createLogRecord("a.b.f", Level.FINEST))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f", Level.WARNING))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.g", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.g", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.g", Level.WARNING))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.g", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.c.y", Level.FINEST))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.c.y", Level.SEVERE))).isEqualTo(true);

		assertThat(lf.isLoggable(createLogRecord("a.b.d.y", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.d.y", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.d.y", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.e.y", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.e.y", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.e.y", Level.SEVERE))).isEqualTo(true);

		assertThat(lf.isLoggable(createLogRecord("a.b.f.y", Level.FINEST))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f.y", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f.y", Level.WARNING))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.f.y", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.g.y", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.g.y", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.g.y", Level.WARNING))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.g.y", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.FINER))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.FINE))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.INFO))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.WARNING))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.SEVERE))).isEqualTo(false);
	}

	@Test
	public void testParserWithRoot() throws Exception {
		LoggerFilter lf = new LoggerFilter(false);
		lf.parseEnabledLogLevels(".@INFO, 1@FINE-WARNING, 2@SEVERE");

		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.INFO))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("a.b.z", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("1", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("1", Level.FINE))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("1", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("1.2.3.4.5.6", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("1.2.3.4.5.6", Level.FINE))).isEqualTo(true);
		assertThat(lf.isLoggable(createLogRecord("1.2.3.4.5.6", Level.SEVERE))).isEqualTo(false);

		assertThat(lf.isLoggable(createLogRecord("2", Level.FINEST))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("2", Level.FINE))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("2", Level.INFO))).isEqualTo(false);
		assertThat(lf.isLoggable(createLogRecord("2", Level.SEVERE))).isEqualTo(true);
	}

	@Ignore
	protected LogRecord createLogRecord(String loggerName, Level level) {
		LogRecord lr = new LogRecord(level, "some unimportant message");
		lr.setLoggerName(loggerName);
		return lr;
	}

	@Ignore
	protected List<String> getLogLines() throws Exception {
		this.streamHandler.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.logBuffer.toByteArray()), "UTF-8"));
		List<String> lines = new ArrayList<>();
		String line = null;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		this.logBuffer.reset();
		return lines;
	}

	@Ignore
	protected String getLastLogLine() throws Exception {
		List<String> lines = this.getLogLines();
		if (!lines.isEmpty()) {
			return lines.get(lines.size() - 1);
		}
		return null;
	}

	@Test
	public void testLoggerFilter() throws Exception {
		this.initializeLogging("a.b.c, a.b.d@INFO, a.b.e@INFO-, a.b.f@-WARNING, a.b.g@INFO-WARNING, a.b.h", "a.b.c", "a.b.d", "a.b.e", "a.b.f",
				"a.b.g", "a.b.h", "a.b.z");

		Logger loggerAbc = Logger.getLogger("a.b.c");
		loggerAbc.severe("Severe message");
		assertThat(getLastLogLine()).contains("Severe message");
		loggerAbc.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbc.fine("Fine message");
		assertThat(getLastLogLine()).contains("Fine message");
		loggerAbc.finest("Finest message");
		assertThat(getLastLogLine()).contains("Finest message");

		Logger loggerAbc1 = Logger.getLogger("a.b.c.1");
		loggerAbc1.severe("Severe message");
		assertThat(getLastLogLine()).contains("Severe message");
		loggerAbc1.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbc1.fine("Fine message");
		assertThat(getLastLogLine()).contains("Fine message");
		loggerAbc1.finest("Finest message");
		assertThat(getLastLogLine()).contains("Finest message");

		Logger loggerAbd = Logger.getLogger("a.b.d");
		loggerAbd.severe("Severe message");
		assertThat(getLastLogLine()).isNull();
		loggerAbd.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbd.fine("Fine message");
		assertThat(getLastLogLine()).isNull();
		loggerAbd.finest("Finest message");
		assertThat(getLastLogLine()).isNull();

		Logger loggerAbe = Logger.getLogger("a.b.e");
		loggerAbe.severe("Severe message");
		assertThat(getLastLogLine()).contains("Severe message");
		loggerAbe.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbe.fine("Fine message");
		assertThat(getLastLogLine()).isNull();
		loggerAbe.finest("Finest message");
		assertThat(getLastLogLine()).isNull();

		Logger loggerAbf = Logger.getLogger("a.b.f");
		loggerAbf.severe("Severe message");
		assertThat(getLastLogLine()).isNull();
		loggerAbf.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbf.fine("Fine message");
		assertThat(getLastLogLine()).contains("Fine message");
		loggerAbf.finest("Finest message");
		assertThat(getLastLogLine()).contains("Finest message");

		Logger loggerAbg = Logger.getLogger("a.b.g");
		loggerAbg.severe("Severe message");
		assertThat(getLastLogLine()).isNull();
		loggerAbg.warning("Warn message");
		assertThat(getLastLogLine()).contains("Warn message");
		loggerAbg.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbg.fine("Fine message");
		assertThat(getLastLogLine()).isNull();
		loggerAbg.finest("Finest message");
		assertThat(getLastLogLine()).isNull();

		Logger loggerAbh = Logger.getLogger("a.b.h");
		loggerAbh.severe("Severe message");
		assertThat(getLastLogLine()).contains("Severe message");
		loggerAbh.info("Info message");
		assertThat(getLastLogLine()).contains("Info message");
		loggerAbh.fine("Fine message");
		assertThat(getLastLogLine()).contains("Fine message");
		loggerAbh.finest("Finest message");
		assertThat(getLastLogLine()).contains("Finest message");
	}

	protected void initializeLogging(String loggerConfig, String... loggerNames) throws Exception {
		// @formatter:off
		String loggingConfiguration =
				"### Levels ###\n"+
				".level = FINEST\n"+
				"com.braintribe.level = FINEST\n"+
				"\n### Formatters ###\n"+
				"com.braintribe.logging.juli.formatters.simple.SimpleFormatter1.format=%4$-7s %7$-33s '%5$s' %6$s %n\n"+
				"\n### Handlers ###\n"+
				"handlers = com.braintribe.logging.juli.handlers.ConsoleHandler\n"+
				"\n### Console Handler ###\n"+
				"com.braintribe.logging.juli.handlers.ConsoleHandler.level = FINEST\n"+
				"com.braintribe.logging.juli.handlers.ConsoleHandler.formatter = com.braintribe.logging.juli.formatters.simple.SimpleFormatter1\n"+
				"com.braintribe.logging.juli.handlers.ConsoleHandler.filter = com.braintribe.logging.juli.filters.logger.LoggerFilter1\n"+
				"\n### Filters ###\n"+
				"com.braintribe.logging.juli.filters.logger.LoggerFilter1.loggerNames = " + loggerNames + "\n";
		// @formatter:on

		System.out.println("Using configuration:\n" + loggingConfiguration);
		LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(loggingConfiguration.getBytes("UTF-8")));
		LoggerFilter lf = new LoggerFilter(false);
		lf.parseEnabledLogLevels(loggerConfig);

		for (String loggerName : loggerNames) {
			addStreamHandler(loggerName);
		}
		this.streamHandler.setFilter(lf);
	}
}
