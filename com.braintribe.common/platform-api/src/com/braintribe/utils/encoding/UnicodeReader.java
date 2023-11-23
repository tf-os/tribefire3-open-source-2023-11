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
package com.braintribe.utils.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
version: 1.1 / 2007-01-25
- changed BOM recognition ordering (longer boms first)

Original pseudocode   : Thomas Weidenfeller
Implementation tweaked: Aki Nieminen

http://www.unicode.org/unicode/faq/utf_bom.html
BOMs:
  00 00 FE FF    = UTF-32, big-endian
  FF FE 00 00    = UTF-32, little-endian
  EF BB BF       = UTF-8,
  FE FF          = UTF-16, big-endian
  FF FE          = UTF-16, little-endian

Win2k Notepad:
  Unicode format = UTF-16LE
 ***/

/**
 * Generic unicode textreader, which will use BOM mark to identify the encoding to be used. If BOM is not found then use a given default or system
 * encoding.
 */
public class UnicodeReader extends Reader {

	UnicodeInputStream unicodeInputStream = null;
	InputStreamReader internalReader = null;
	protected String defaultEncoding = null;

	/**
	 *
	 * @param in
	 *            inputstream to be read
	 * @param defaultEnc
	 *            default encoding if stream does not have BOM marker. Give NULL to use system-level default.
	 */
	public UnicodeReader(InputStream in, String defaultEnc) {
		this.unicodeInputStream = new UnicodeInputStream(in, defaultEnc);
		this.defaultEncoding = defaultEnc;
	}

	public String getDefaultEncoding() {
		return this.defaultEncoding;
	}

	/**
	 * Get stream encoding or NULL if stream is uninitialized. Call init() or read() method to initialize it.
	 */
	public String getEncoding() {
		if (this.internalReader == null) {
			try {
				init();
			} catch (IOException ex) {
				IllegalStateException ise = new IllegalStateException("Init method failed.");
				ise.initCause(ise);
				throw ise;
			}
		}
		return this.unicodeInputStream.getEncoding();
	}

	/**
	 * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only BOM bytes are skipped.
	 */
	protected void init() throws IOException {
		if (internalReader != null) {
			return;
		}

		String encoding = this.unicodeInputStream.getEncoding();
		if (encoding == null) {
			encoding = "UTF-8";
		}
		internalReader = new InputStreamReader(this.unicodeInputStream, encoding);
	}

	@Override
	public void close() throws IOException {
		if (this.internalReader != null) {
			this.internalReader.close();
		} else if (this.unicodeInputStream != null) {
			this.unicodeInputStream.close();
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		init();
		return this.internalReader.read(cbuf, off, len);
	}
}
