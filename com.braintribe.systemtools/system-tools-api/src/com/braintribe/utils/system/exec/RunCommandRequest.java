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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class RunCommandRequest {

	protected String command = null;
	protected String[] commandParts = null;
	protected String commandDescription;
	protected long timeout = -1L;
	protected int retries = -1;
	protected long retryDelay = -1L;
	protected Map<String, String> environmentVariables = null;
	protected boolean silent = false;
	protected String input;

	public static RunCommandRequestBuilder builder() {
		return new RunCommandRequestBuilder();
	}

	public static class RunCommandRequestBuilder {
		RunCommandRequest request = new RunCommandRequest();

		public RunCommandRequestBuilder command(String... command) {
			request.commandParts = command;
			return this;
		}
		public RunCommandRequestBuilder input(String input) {
			request.input = input;
			return this;
		}
		public RunCommandRequestBuilder timeout(long timeout) {
			request.timeout = timeout;
			return this;
		}
		public RunCommandRequestBuilder retries(int retries) {
			request.retries = retries;
			return this;
		}
		public RunCommandRequestBuilder env(Map<String, String> environmentVariables) {
			request.environmentVariables = environmentVariables;
			return this;
		}
		public RunCommandRequestBuilder retryDelay(long retryDelay) {
			request.retryDelay = retryDelay;
			return this;
		}
		public RunCommandRequestBuilder silent(boolean silent) {
			request.silent = silent;
			return this;
		}
		public RunCommandRequest build() {
			return request;
		}
	}

	private RunCommandRequest() {
		// For builder only
	}

	public RunCommandRequest(String command, long timeout, int retries) {
		this.command = command;
		this.timeout = timeout;
		this.retries = retries;
	}
	public RunCommandRequest(String[] commandParts, long timeout, int retries) {
		this.commandParts = commandParts;
		this.timeout = timeout;
		this.retries = retries;
	}
	public RunCommandRequest(String[] commandParts, long timeout, int retries, Map<String, String> environmentVariables) {
		this.commandParts = commandParts;
		this.timeout = timeout;
		this.retries = retries;
		this.environmentVariables = environmentVariables;
	}
	public RunCommandRequest(String[] commandParts, long timeout, int retries, long retryDelay, Map<String, String> environmentVariables) {
		this.commandParts = commandParts;
		this.timeout = timeout;
		this.retries = retries;
		this.retryDelay = retryDelay;
		this.environmentVariables = environmentVariables;
	}
	public RunCommandRequest(String command, long timeout) {
		this.command = command;
		this.timeout = timeout;
	}
	public RunCommandRequest(String[] commandParts, long timeout) {
		this.commandParts = commandParts;
		this.timeout = timeout;
	}
	public RunCommandRequest(String[] commandParts, long timeout, boolean silent) {
		this.commandParts = commandParts;
		this.timeout = timeout;
		this.silent = silent;
	}
	public RunCommandRequest(String[] commandParts, long timeout, boolean silent, Map<String, String> environmentVariables) {
		this.commandParts = commandParts;
		this.timeout = timeout;
		this.silent = silent;
		this.environmentVariables = environmentVariables;
	}

	public static String getArrayAsString(String[] array) {
		if (array == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append("[" + array[i] + "]");
		}
		return sb.toString();
	}
	public String getCommandDescription() {
		if (this.commandDescription == null) {
			if (this.command != null) {
				this.commandDescription = this.command;
			} else if (this.commandParts != null) {
				this.commandDescription = getArrayAsString(this.commandParts);
			} else {
				this.commandDescription = "(No command available.)";
			}
		}
		return this.commandDescription;
	}
	public String getCommand() {
		if (this.command == null && this.commandParts != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.commandParts.length; ++i) {
				if (i > 0) {
					sb.append(' ');
				}
				sb.append(this.commandParts[i]);
			}
			this.command = sb.toString();
		}
		return this.command;
	}
	public String[] getCommandParts() {
		if (this.commandParts == null && this.command != null) {
			StringTokenizer st = new StringTokenizer(this.command);
			List<String> tokens = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				tokens.add(st.nextToken().trim());
			}
			this.commandParts = tokens.toArray(new String[tokens.size()]);
		}
		return commandParts;
	}
	public long getTimeout() {
		return timeout;
	}
	public int getRetries() {
		return retries;
	}
	public long getRetryDelay() {
		return retryDelay;
	}
	public Map<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}
	public boolean isSilent() {
		return silent;
	}
	public String getInput() {
		return input;
	}

	@Override
	public String toString() {
		return getCommandDescription();
	}
}
