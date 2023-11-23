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
package com.braintribe.logging.io;

import java.io.PrintWriter;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;

public class LoggingPrintWriter extends PrintWriter {

	protected Logger logger = null;
	protected LogLevel logLevel = LogLevel.DEBUG;
	protected StringBuffer text = new StringBuffer();

	public LoggingPrintWriter(Logger _logger, LogLevel _logLevel) {
		super(System.err);
		this.logger = _logger;
		this.logLevel = _logLevel;
	}

	@Override
	public void close() {
		flush();
	}

	@Override
	public void flush() {
		if (!text.toString().equals("")) {
			flushLine();
		}
	}

	@Override
	public void print(final boolean b) {
		text.append(b);
	}

	@Override
	public void print(final char c) {
		text.append(c);
	}

	@Override
	public void print(final char[] s) {
		text.append(s);
	}

	@Override
	public void print(final double d) {
		text.append(d);
	}

	@Override
	public void print(final float f) {
		text.append(f);
	}

	@Override
	public void print(final int i) {
		text.append(i);
	}

	@Override
	public void print(final long l) {
		text.append(l);
	}

	@Override
	public void print(final Object obj) {
		text.append(obj);
	}

	@Override
	public void print(final String s) {
		text.append(s);
	}

	@Override
	public void println() {
		if (!text.toString().equals("")) {
			flushLine();
		}
	}

	@Override
	public void println(final boolean x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final char x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final char[] x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final double x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final float x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final int x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final long x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final Object x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void println(final String x) {
		text.append(x);
		flushLine();
	}

	@Override
	public void write(char buf[], int off, int len) {
		text.append(buf, off, len);
	}

	@Override
	public void write(int c) {
		text.append(c);
	}

	@Override
	public void write(String s, int off, int len) {
		text.append(s, off, len);
	}

	@Override
	public boolean checkError() {
		return false;
	}

	private void flushLine() {
		logger.log(this.logLevel, text.toString());
		text.setLength(0);
	}

}
