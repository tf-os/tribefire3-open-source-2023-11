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
import java.io.PushbackInputStream;

/**
version: 1.1 / 2007-01-25
- changed BOM recognition ordering (longer boms first)

Original pseudocode   : Thomas Weidenfeller
Implementation tweaked: Aki Nieminen

http://www.unicode.org/unicode/faq/utf_bom.html
BOMs in byte length ordering:
  00 00 FE FF    = UTF-32, big-endian
  FF FE 00 00    = UTF-32, little-endian
  EF BB BF       = UTF-8,
  FE FF          = UTF-16, big-endian
  FF FE          = UTF-16, little-endian

Win2k Notepad:
  Unicode format = UTF-16LE
 ***/

/**
 * This inputstream will recognize unicode BOM marks and will skip bytes if getEncoding() method is called before any of the read(...) methods.
 *
 * Usage pattern: String enc = "ISO-8859-1"; // or NULL to use systemdefault FileInputStream fis = new FileInputStream(file); UnicodeInputStream uin =
 * new UnicodeInputStream(fis, enc); enc = uin.getEncoding(); // check and skip possible BOM bytes InputStreamReader in; if (enc == null) in = new
 * InputStreamReader(uin); else in = new InputStreamReader(uin, enc);
 */
public class UnicodeInputStream extends InputStream {
	PushbackInputStream internalIn;

	boolean isInited = false;
	String defaultEnc;
	String encoding;

	private static final int BOM_SIZE = 4;

	public UnicodeInputStream(InputStream in, String defaultEnc) {
		internalIn = new PushbackInputStream(in, BOM_SIZE);
		this.defaultEnc = defaultEnc;
	}

	public String getDefaultEncoding() {
		return defaultEnc;
	}

	public String getEncoding() {
		if (!isInited) {
			try {
				init();
			} catch (IOException ex) {
				IllegalStateException ise = new IllegalStateException("Init method failed.");
				ise.initCause(ise);
				throw ise;
			}
		}
		return encoding;
	}

	/**
	 * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only BOM bytes are skipped.
	 */
	protected void init() throws IOException {
		if (isInited) {
			return;
		}

		byte bom[] = new byte[BOM_SIZE];
		int n, unread;
		n = internalIn.read(bom, 0, bom.length);

		if ((n >= 4) && (bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
			encoding = "UTF-32BE";
			unread = n - 4;
		} else if ((n >= 4) && (bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
			encoding = "UTF-32LE";
			unread = n - 4;
		} else if ((n >= 3) && (bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
			encoding = "UTF-8";
			unread = n - 3;
		} else if ((n >= 2) && (bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
			encoding = "UTF-16BE";
			unread = n - 2;
		} else if ((n >= 2) && (bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
			encoding = "UTF-16LE";
			unread = n - 2;
		} else {
			// Unicode BOM mark not found, unread all bytes
			encoding = defaultEnc;
			unread = n;
		}
		// System.out.println("read=" + n + ", unread=" + unread);

		if (unread > 0) {
			internalIn.unread(bom, (n - unread), unread);
		}

		isInited = true;
	}

	@Override
	public void close() throws IOException {
		this.isInited = true;
		internalIn.close();
	}

	@Override
	public int read() throws IOException {
		this.init();
		return internalIn.read();
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		this.init();
		return this.internalIn.read(b, off, len);
	}

	@Override
	public int available() throws IOException {
		return internalIn.available();
	}

	@Override
	public long skip(long n) throws IOException {
		return internalIn.skip(n);
	}

	@Override
	public boolean markSupported() {
		return internalIn.markSupported();
	}

	// not synchronization, because delegate stream is synchronized
	@Override
	public void mark(int readlimit) {
		internalIn.mark(readlimit);
	}

	// not synchronization, because delegate stream is synchronized
	@Override
	public void reset() throws IOException {
		internalIn.reset();
	}

}
