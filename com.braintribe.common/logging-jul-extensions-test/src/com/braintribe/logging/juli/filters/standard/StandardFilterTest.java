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
package com.braintribe.logging.juli.filters.standard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;

import com.braintribe.utils.DOMTools;

/**
 * Provides tests for {@link StandardFilter}.
 *
 * @author michael.lafite
 */
public class StandardFilterTest {

	@Test
	public void testParseConditionAndCreateFilter() {
		check("<true/>", null, true);
		check("<false/>", null, false);
		check("<and></and>", null, true);
		check("<or></or>", null, false);
		check("<and><true/></and>", null, true);
		check("<and><true/><false/></and>", null, false);
		check("<or><true/></or>", null, true);
		check("<or><true/><false/></or>", null, true);
		check("<or><false/><false/></or>", null, false);

		check("<matches>abc.*</matches>", newRecord("abcd"), true);
		check("<matches>abc.*</matches>", newRecord("abe"), false);
		check("<contains>abc</contains>", newRecord("abc"), true);
		check("<contains></contains>", newRecord("abc"), true);
		check("<contains> </contains>", newRecord("abc"), false);
		check("<contains> </contains>", newRecord("abc "), true);
		check("<contains> </contains>", newRecord("abc "), true);

		check("<not><matches>.*(Loading index from|Loaded index item).*</matches></not>", newRecord("abc"), true);
		check("<not><matches>.*(Loading index from|Loaded index item).*</matches></not>", newRecord("PREFIX Loading index from SUFFIX"), false);
		check("<not><matches>.*(Loading index from|Loaded index item).*</matches></not>", newRecord(null), true);

		// from BTT-5546
		{
			String conditionString = "<not><or><contains>NOTWANTED</contains><contains>BADMESSAGE</contains><matches>.*hibernate dummy warning.*</matches><matches>.*hibernate error code ORA-23\\d+ not wanted.*</matches></or></not>";
			check(conditionString, newRecord("some valid text"), true);
			check(conditionString, newRecord("abcdNOTWANTEDdbc"), false);
			check(conditionString, newRecord("abcdBADMESSAGEdbc"), false);
			check(conditionString, newRecord("sometexthibernate dummy warningsometext"), false);
		}

		// LoggerFilter
		{
			String loggerFilterConditionString = "<loggerNames>com.braintribe.model.processing.securityservice.usersession.basic.BasicUserSessionService@INFO-</loggerNames>";
			LogRecord loggerFilterLogRecord = new LogRecord(Level.INFO, "Ignore");
			loggerFilterLogRecord.setLoggerName("com.braintribe.model.processing.securityservice.usersession.basic.BasicUserSessionService");
			check(loggerFilterConditionString, loggerFilterLogRecord, true);
			loggerFilterLogRecord.setLoggerName("com.braintribe.model.processing.securityservice.usersession.basic.BasicUserSession2Service");
			check(loggerFilterConditionString, loggerFilterLogRecord, false);
			loggerFilterLogRecord.setLoggerName("com.braintribe.model.processing.securityservice.usersession.basic.BasicUserSessionService");
			loggerFilterLogRecord.setLevel(Level.FINE);
			check(loggerFilterConditionString, loggerFilterLogRecord, false);
			loggerFilterLogRecord.setLevel(Level.WARNING);
			check(loggerFilterConditionString, loggerFilterLogRecord, true);
		}

		// security log
		{
			// @formatter:off
			String securityFilterConditionString =
			"<and>\n"
			+ "	<loggerNames>com.braintribe.model.processing.securityservice.usersession.basic.BasicUserSessionService, com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor, com.braintribe.web.servlet.auth.AuthServlet, com.braintribe.cartridge.common.processing.web.ServiceEndpointFilter@FINE</loggerNames>\n"
			+ "	<or>\n"
			+ "		<!-- Login -->\n"
			+ "		<matches>.*Authentication of user .* succeeded.*</matches>\n"
			+ "		<!-- Wrong password -->\n"
			+ "		<matches>.*Authentication of user .* failed.*</matches>\n"
			+ "		<!-- Session is closed -->\n"
			+ "		<matches>.*Closing user session .* for user .*</matches>\n"
			+ "		<!-- Session has expired -->\n"
			+ "		<matches>.*session .* was considered expired .*</matches>\n"
			+ "		<!-- Session is invalid -->\n"
			+ "		<matches>.*An invalid sessionId.* was provided.*</matches>\n"
			+ "	</or>\n"
			+ "</and>";

			// @formatter:on
			LogRecord loginLogRecord = new LogRecord(Level.FINE, "Authentication of user 'admin' from '127.0.0.1' succeeded");
			loginLogRecord.setLoggerName("com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor");
			check(securityFilterConditionString, loginLogRecord, true);
		}
	}

	private static void check(String conditionString, LogRecord record, boolean expectedResult) {
		Filter filter = StandardFilter.parseConditionAndCreateFilter(DOMTools.parse(conditionString).getDocumentElement());
		assertThat(filter.isLoggable(record)).isEqualTo(expectedResult);
	}

	private static LogRecord newRecord(String message) {
		return new LogRecord(Level.INFO, message);
	}

}
