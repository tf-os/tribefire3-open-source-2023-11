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
package com.braintribe.web.multipart.api;

import java.io.IOException;
import java.io.InputStream;

public interface PartReader extends PartHeader {
	InputStream openStream() throws IOException;
	/**
	 * @deprecated Use {@link #openStream()} instead.
	 */
	@Deprecated
	InputStream openTransferEncodingAwareInputStream() throws IOException;
	String getContentAsString() throws IOException;
	String getContentAsString(String charset) throws IOException;
	void consume() throws IOException;

	PartStreamingStatus getStatus();
	boolean isConsumed();
	boolean isBackedUp();

	void backup() throws IOException;
}
