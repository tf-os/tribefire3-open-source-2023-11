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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;

/**
 * A convenience class for OS-related stuff.
 * 
 * @author romank
 *
 */
public class CommandExecutionImpl implements CommandExecution {

	private static Logger logger = Logger.getLogger(CommandExecutionImpl.class);

	public final int NO_TIMEOUT = 0;

	protected ProcessTerminator processTerminator = null;

	@Override
	public RunCommandContext runCommand(RunCommandRequest request) throws Exception {
		long t1 = System.currentTimeMillis();

		String commandDescription = request.getCommandDescription();
		String[] commandParts = request.getCommandParts();
		long timeout = request.getTimeout();
		Map<String, String> environmentVariables = request.getEnvironmentVariables();
		int retries = request.getRetries();
		long retryDelay = request.getRetryDelay();
		boolean silent = request.isSilent();
		String input = request.getInput();

		String signature = String.format("runCommand(commands='%s', timeout=%d, retries=%d, retryDelay=%d, environmentVariables=%s)",
				commandDescription, timeout, retries, retryDelay, environmentVariables);

		if (silent) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("%s: begin", signature));
		} else {
			if (logger.isDebugEnabled())
				logger.debug(String.format("%s: begin", signature));
		}

		RunCommandContext result = runCommand(commandParts, timeout, false, environmentVariables, input);
		int count = 0;
		while ((count < retries) && (result.getErrorCode() != 0)) {
			count++;

			trace(commandDescription, timeout, count, retries);

			result = runCommand(commandParts, timeout, false, environmentVariables, input);

			if (result.getErrorCode() != 0) {
				if ((retryDelay > 0) && (count < retries)) {
					try {
						Thread.sleep(retryDelay);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		long duration = System.currentTimeMillis() - t1;
		trace(commandDescription, result, duration);

		if (silent) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("%s: system call took [Time] %d ms", signature, duration));
		} else {
			if (logger.isDebugEnabled())
				logger.debug(String.format("%s: system call took [Time] %d ms", signature, duration));
		}

		return result;
	}

	protected RunCommandContext runCommand(String[] command, long timeout, boolean silent, Map<String, String> environmentVariables, String input)
			throws Exception {

		long t1 = System.currentTimeMillis();

		String signature = String.format("runCommand(commands='%s', timeout=%d)", java.util.Arrays.toString(command), timeout);
		String shortSignature = command[0];

		if (silent) {
			if (logger.isTraceEnabled())
				logger.trace(String.format("%s: begin", signature));
		} else {
			if (logger.isDebugEnabled())
				logger.debug(String.format("%s: begin", signature));
		}

		File stdoutFile = File.createTempFile(shortSignature + "-stdout", ".txt");
		File stderrFile = File.createTempFile(shortSignature + "-stderr", ".txt");

		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectOutput(Redirect.to(stdoutFile));
			processBuilder.redirectError(Redirect.to(stderrFile));

			if (environmentVariables != null) {
				Map<String, String> environmentOfProcessBuilder = processBuilder.environment();
				environmentOfProcessBuilder.clear();
				environmentOfProcessBuilder.putAll(environmentVariables);
			}

			/* starting the process */
			Process commandProcess = processBuilder.start();

			if (timeout > NO_TIMEOUT) {
				if (this.processTerminator != null) {
					this.processTerminator.addProcess(RunCommandRequest.getArrayAsString(command), commandProcess, timeout);
				}
			}

			if (input != null) {
				try (Writer outputWriter = new OutputStreamWriter(commandProcess.getOutputStream(), StandardCharsets.UTF_8)) {
					outputWriter.write(input + "\n");
				}
			}

			int returnCode = -1;
			boolean wait = true;
			while (wait) {
				try {
					returnCode = commandProcess.waitFor();
					wait = false;
				} catch (InterruptedException ie) {
					logger.debug(
							() -> "Got interrupted while waiting for the command " + signature + " to finish. Killing process " + commandProcess);
					commandProcess.destroyForcibly();
					throw ie;
				}
			}

			String output = readFully(stdoutFile);
			String error = readFully(stderrFile);

			if (silent) {
				if (logger.isTraceEnabled())
					logger.trace(String.format("%s: system call took [Time] %d ms", signature, (System.currentTimeMillis() - t1)));
			} else {
				if (logger.isDebugEnabled())
					logger.debug(String.format("%s: system call took [Time] %d ms", signature, (System.currentTimeMillis() - t1)));
			}

			return new RunCommandContext(returnCode, output, error);

		} finally {
			deleteFileSilently(stdoutFile);
			deleteFileSilently(stderrFile);
		}
	}

	private String readFully(File stdoutFile) {
		if (stdoutFile == null || !stdoutFile.exists() || stdoutFile.length() == 0) {
			return "";
		}
		try (Reader r = new InputStreamReader(new FileInputStream(stdoutFile), "UTF-8")) {

			StringBuilder stringBuilder = new StringBuilder();

			final char[] buff = new char[65536];
			while (true) {
				final int len = r.read(buff);
				if (len < 0) {
					break;
				}
				stringBuilder.append(buff, 0, len);
			}

			return stringBuilder.toString();
		} catch (Exception e) {
			logger.debug(() -> "Could not read temporary file " + stdoutFile.getAbsolutePath(), e);
		}
		return "";
	}

	private void deleteFileSilently(File file) {
		if (file != null && file.exists()) {
			try {
				file.delete();
			} catch (Exception e) {
				logger.debug(() -> "Error while trying to delete temp file: " + file.getAbsolutePath(), e);
			}
		}
	}

	private void trace(String command, long timeout, int count, int retries) {

		if (!logger.isTraceEnabled())
			return;

		if (count == 1)
			logger.trace(String.format("running command %s ... timout is %d ms and %d retries", command, timeout, retries));
		else
			logger.trace(String.format("running command %s ... timout is %d ms and %d / %d retries", command, timeout, count, retries));
	}

	private void trace(String command, RunCommandContext result, long duration) {
		if (logger.isTraceEnabled())
			logger.trace(String.format("running command %s : finished in [Time] %d ms, outcome is\n<<<%s\n>>>", command, duration, result));
	}

	@Configurable
	public void setProcessTerminator(ProcessTerminator processTerminator) {
		this.processTerminator = processTerminator;
	}

}
