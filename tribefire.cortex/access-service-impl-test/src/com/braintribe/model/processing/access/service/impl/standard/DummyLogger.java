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
package com.braintribe.model.processing.access.service.impl.standard;

import com.braintribe.logging.Logger;

/**
 * The {@link DummyLogger} is enabled for all log levels and swallows log messages.
 * 
 * 
 */
public class DummyLogger extends Logger {

	public DummyLogger(@SuppressWarnings("unused") Class<?> cls) {
		// nothing to do
	}

	private static boolean isTraceEnabled = true;

	@Override
	public boolean isTraceEnabled() {
		return isTraceEnabled;
	}
	
	@Override 
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public void trace(String msg) {
		// swallowing log messages
	}

	@Override
	public void trace(Throwable t) {
		// swallowing log messages
	}

	@Override
	public void trace(String msg, Throwable t) {
		// swallowing log messages
	}

	@Override
	public void debug(String msg) {
		// swallowing log messages
	}

	@Override
	public void debug(Throwable t) {
		// swallowing log messages
	}

	@Override
	public void debug(String msg, Throwable t) {
		// swallowing log messages
	}

	@Override
	public void error(String msg) {
		// swallowing log messages
	}

	@Override
	public void info(String msg) {
		// swallowing log messages
	}

	@Override
	public void info(Throwable t) {
		// swallowing log messages
	}

	@Override
	public void info(String msg, Throwable t) {
		// swallowing log messages
	}

	@Override
	public void warn(String msg) {
		// swallowing log messages
	}

	@Override
	public void warn(Throwable ex) {
		// swallowing log messages
	}

	@Override
	public void warn(String msg, Throwable ex) {
		// swallowing log messages
	}

	@Override
	public void error(String msg, Throwable ex) {
		// swallowing log messages
	}

	public static void setTraceEnabled(boolean isTraceEnabled) {
		DummyLogger.isTraceEnabled = isTraceEnabled;
	}

	@Override
	public void log(final LogLevel logLevel, final String msg) {
		// NO OP
	}

	@Override
	public void log(final LogLevel logLevel, final String msg, final Throwable ex) {
		// NO OP
	}

	@Override
	public void pushContext(String context) {
		// NO OP
	}
	@Override
	public void popContext() {
		// NO OP
	}
	@Override
	public void removeContext() {
		// NO OP
	}

}
