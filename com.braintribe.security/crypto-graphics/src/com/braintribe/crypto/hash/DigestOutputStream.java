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
package com.braintribe.crypto.hash;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;

import com.braintribe.crypto.utils.TextUtils;

public class DigestOutputStream extends OutputStream {
	private MessageDigest messageDigest;
	private OutputStream targetOut;
	
	public DigestOutputStream(MessageDigest digest) {
		super();
		this.messageDigest = digest;
	}
	
	public DigestOutputStream(MessageDigest digest, OutputStream targetOut) {
		super();
		this.messageDigest = digest;
		this.targetOut = targetOut;
	}

	@Override
	public void write(int b) throws IOException {
		messageDigest.update((byte)b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		messageDigest.update(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		messageDigest.update(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		if (targetOut != null) {
			OutputStreamWriter writer = new OutputStreamWriter(targetOut, "UTF-8");
			String hex = TextUtils.convertToHex(messageDigest.digest());
			writer.write(hex);
			writer.close();
		}
	}
	
	public MessageDigest getMessageDigest() {
		return messageDigest;
	}
}
