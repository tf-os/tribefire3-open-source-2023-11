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
package com.braintribe.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.util.Random;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

public class CountingDigestInputStream extends DigestInputStream {
	private static final Logger logger = Logger.getLogger(CountingDigestInputStream.class);
	private long count;

	public CountingDigestInputStream(InputStream stream, MessageDigest digest) {
		super(stream, digest);
	}

	@Override
	public int read() throws IOException {
		int res = super.read();

		if (res != -1) {
			count++;
		}

		return res;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int res = super.read(b, off, len);

		if (res != -1) {
			count += res;
		}

		return res;
	}

	public long getCount() {
		return count;
	}

	public static void main(String[] args) {
		try {
			int count = 1000000;

			final byte[] data = new byte[count];
			Random random = new Random();
			random.nextBytes(data);

			Supplier<InputStream> provider1 = new Supplier<InputStream>() {

				@Override
				public InputStream get() throws RuntimeException {
					try {
						return new CountingDigestInputStream(new ByteArrayInputStream(data), MessageDigest.getInstance("MD5"));
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
				}

			};

			Supplier<InputStream> provider2 = new Supplier<InputStream>() {

				@Override
				public InputStream get() throws ProviderException {
					return new ByteArrayInputStream(data);
				}

			};

			suck(provider1);
			suck(provider2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void suck(Supplier<InputStream> provider) throws Exception {
		long start = System.currentTimeMillis();
		for (int n = 0; n < 10; n++) {
			InputStream in = provider.get();

			try {
				byte buffer[] = new byte[8192];
				while (in.read(buffer) != -1) {
					// nothing to do
				}
			} finally {
				IOTools.closeCloseable(in, logger);
			}
		}
		long end = System.currentTimeMillis();
		long delta = end - start;

		System.out.println(delta + " ms");
	}
}
