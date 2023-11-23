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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.utils.lcd.NullSafe.nonNull;

import java.io.IOException;
import java.io.InputStream;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;

/**
 * @author peter.gazdik
 */
public class AppendedSnippetImpl implements PersistenceAppender.AppendedSnippet {

	public final long sizeInBytes;
	public final InputStreamProvider inputStreamProvider;

	public AppendedSnippetImpl(long size, InputStreamProvider inputStreamProvider) {
		this.sizeInBytes = nonNull(size, "size");
		this.inputStreamProvider = nonNull(inputStreamProvider, "inputStreamProvider");
	}

	@Override
	public long sizeInBytes() {
		return sizeInBytes;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return inputStreamProvider.openInputStream();
	}

}
