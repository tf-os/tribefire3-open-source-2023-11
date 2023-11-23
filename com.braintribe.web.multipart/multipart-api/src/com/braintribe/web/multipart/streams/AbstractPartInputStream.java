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
package com.braintribe.web.multipart.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.impl.RemainingBufferAccess;

public abstract class AbstractPartInputStream extends InputStream implements RemainingBufferAccess {
	private Consumer<? super AbstractPartInputStream> closeHandler;

	public void setCloseHandler(Consumer<? super AbstractPartInputStream> closeHandler) {
		this.closeHandler = closeHandler;
	}

	@Override
	public void close() throws IOException {
		byte[] buffer = new byte[IOTools.SIZE_8K];
		while (read(buffer) != -1) {
			// Consume the rest of the part and ignore its content
		}

		if (closeHandler != null) {
			closeHandler.accept(this);
		}

		closeHandler = null;
	}

}
