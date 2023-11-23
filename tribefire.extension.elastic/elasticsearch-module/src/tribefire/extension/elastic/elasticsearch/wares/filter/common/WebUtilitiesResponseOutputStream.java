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
/*
 * Copyright 2010-2011 Rajendra Patil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tribefire.extension.elastic.elasticsearch.wares.filter.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 * Common Simple Servlet Response stream using ByteArrayOutputStream
 *
 */
public class WebUtilitiesResponseOutputStream extends ServletOutputStream {

	private ByteArrayOutputStream byteArrayOutputStream;

	public WebUtilitiesResponseOutputStream() {
		byteArrayOutputStream = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) throws IOException {
		byteArrayOutputStream.write(b);
	}

	@Override
	public void close() throws IOException {
		byteArrayOutputStream.close();
	}

	@Override
	public void flush() throws IOException {
		byteArrayOutputStream.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byteArrayOutputStream.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		byteArrayOutputStream.write(b);
	}

	public ByteArrayOutputStream getByteArrayOutputStream() {
		return byteArrayOutputStream;
	}

	void reset() {
		byteArrayOutputStream.reset();
	}
}
