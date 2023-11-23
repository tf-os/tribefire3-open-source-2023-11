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
package com.braintribe.utils.system.exec;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RunCommandRequestTest {

	@Test
	public void testRunCommandRequest() throws Exception{
		
		RunCommandRequest rcr = new RunCommandRequest("command", 0L);
		this.checkCommand(rcr, "command", 0L, -1, -1L, false, null);
		rcr = new RunCommandRequest("command", 10L);
		this.checkCommand(rcr, "command", 10L, -1, -1L, false, null);
		rcr = new RunCommandRequest("hello world", -1L);
		this.checkCommand(rcr, "hello world", -1L, -1, -1L, false, null);

		rcr = new RunCommandRequest(new String[] {"command"}, -1L);
		this.checkCommand(rcr, "command", -1L, -1, -1L, false, null);
		rcr = new RunCommandRequest(new String[] {"hello world"}, 2L);
		this.checkCommand(rcr, "hello world", 2L, -1, -1L, false, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 2L);
		this.checkCommand(rcr, "hello world", 2L, -1, -1L, false, null);

		rcr = new RunCommandRequest("command", 10L, -1);
		this.checkCommand(rcr, "command", 10L, -1, -1L, false, null);
		rcr = new RunCommandRequest("command", 10L, 3);
		this.checkCommand(rcr, "command", 10L, 3, -1L, false, null);

		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 3L, true);
		this.checkCommand(rcr, "hello world", 3L, -1, -1L, true, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 3L, false);
		this.checkCommand(rcr, "hello world", 3L, -1, -1L, false, null);

		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 3L, -1);
		this.checkCommand(rcr, "hello world", 3L, -1, -1L, false, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 3L, 10);
		this.checkCommand(rcr, "hello world", 3L, 10, -1L, false, null);

		Map<String,String> env1 = new HashMap<String, String>();
		env1.put("key1", "value1");
		env1.put("key2", "value2");
		
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, true, null);
		this.checkCommand(rcr, "hello world", -1L, -1, -1L, true, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, 3L, false, env1);
		this.checkCommand(rcr, "hello world", 3L, -1, -1L, false, env1);

		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, 3, null);
		this.checkCommand(rcr, "hello world", -1L, 3, -1L, false, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, 3, env1);
		this.checkCommand(rcr, "hello world", -1L, 3, -1L, false, env1);

		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, 3, 10L, null);
		this.checkCommand(rcr, "hello world", -1L, 3, 10L, false, null);
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, 3, 10L, env1);
		this.checkCommand(rcr, "hello world", -1L, 3, 10L, false, env1);

		rcr = new RunCommandRequest(new String[] {"command"}, -1L, 3, 10L, null);
		Assert.assertEquals("[command]", rcr.getCommandDescription());
		rcr = new RunCommandRequest(new String[] {"hello", "world"}, -1L, 3, 10L, null);
		Assert.assertEquals("[hello],[world]", rcr.getCommandDescription());
	}
	
	@Ignore
	protected void checkCommand(RunCommandRequest rcr, String command, long timeout, int retries, long retryDelay, boolean silent, Map<String,String> expectedEnv) throws Exception {
		Assert.assertEquals(command, rcr.getCommand());
		Assert.assertEquals(timeout, rcr.getTimeout());
		Assert.assertEquals(retries, rcr.getRetries());
		Assert.assertEquals(retryDelay, rcr.getRetryDelay());
		Assert.assertEquals(silent, rcr.isSilent());
		
		Map<String,String> actualMap = rcr.getEnvironmentVariables();
		if (actualMap == null && expectedEnv != null) {
			throw new AssertionError("Actual map is null, expected: "+expectedEnv);
		} else if (actualMap != null && expectedEnv == null) {
			throw new AssertionError("Actual map is "+expectedEnv+", expected: null");
		} else if (actualMap != null && expectedEnv != null) {
			this.compareMap(expectedEnv, actualMap);
			this.compareMap(actualMap, expectedEnv);
		}
	}

	private void compareMap(Map<String, String> expectedEnv, Map<String, String> actualMap) throws AssertionError {
		for (Map.Entry<String,String> actual : actualMap.entrySet()) {
			
			String actualKey = actual.getKey();
			String actualVal = actual.getValue();
			
			String expectedVal = expectedEnv.get(actualKey);
			if (expectedVal == null && actualVal != null) {
				throw new AssertionError("Actual value for key "+actualKey+" is "+actualVal+", expected: null");
			}
			if (expectedVal != null && actualVal == null) {
				throw new AssertionError("Actual value for key "+actualKey+" is null, expected: "+expectedVal);
			}
			if (!expectedVal.equals(actualVal)) {
				throw new AssertionError("Value for key "+actualKey+" differ. Actual: "+actualVal+", expected: "+expectedVal);
			}
		}
	}
	
}
