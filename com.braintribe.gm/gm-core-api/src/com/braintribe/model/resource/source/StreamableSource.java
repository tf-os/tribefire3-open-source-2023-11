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
package com.braintribe.model.resource.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;

/**
 * A streamable source that can be kept as a {@link Resource}. Being streamable allows to access the InputStream and OutputStream.
 * 
 *
 */

@GmSystemInterface
public interface StreamableSource {

	InputStreamProvider inputStreamProvider();

	default InputStream openStream() {
		InputStreamProvider inputStreamProvider = inputStreamProvider();

		if (inputStreamProvider != null) {
			try {
				return inputStreamProvider.openInputStream();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new IllegalStateException("TransientSource has no input stream provider:" + this);
		}
	}

	default void writeToStream(OutputStream outputStream) {
		InputStreamProvider inputStreamProvider = inputStreamProvider();

		if (inputStreamProvider != null) {
			try {
				if (inputStreamProvider instanceof OutputStreamer) {
					((OutputStreamer) inputStreamProvider).writeTo(outputStream);
				} else {
					byte buffer[] = new byte[0x10000]; // 16kB buffer
					int bytesRead = 0;
					try (InputStream in = inputStreamProvider.openInputStream()) {
						while ((bytesRead = in.read(buffer)) != -1)
							outputStream.write(buffer, 0, bytesRead);
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new IllegalStateException("TransientSource has no input stream provider:" + this);
		}
	}
}
