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
package com.braintribe.web.multipart.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.RandomAccessFormDataReader;

public class RandomAccessFormDataReaderImpl implements RandomAccessFormDataReader {

	private final Map<String, List<PartReader>> parts = new HashMap<>();
	private final SequentialFormDataReaderImpl dataStreaming;
	private boolean complete;

	public RandomAccessFormDataReaderImpl(InputStream in, byte[] boundary) {
		dataStreaming = new SequentialFormDataReaderImpl(in, boundary);
	}
	
	public RandomAccessFormDataReaderImpl(InputStream in, byte[] boundary, boolean autoCloseInput) {
		dataStreaming = new SequentialFormDataReaderImpl(in, boundary, autoCloseInput);
	}

	protected void ensureComplete() throws MalformedMultipartDataException, IOException {
		if (!complete) {
			PartReader partStreaming = null;
			PartReader previousPartStreaming = null;

			while ((partStreaming = dataStreaming.next()) != null) {
				if (previousPartStreaming != null) {
					previousPartStreaming.backup();
				}

				registerPart(partStreaming);

				previousPartStreaming = partStreaming;
			}

			complete = true;
		}
	}

	@Override
	public Set<String> getPartNames() throws MalformedMultipartDataException, IOException {
		ensureComplete();
		return parts.keySet();
	}

	@Override
	public Collection<PartReader> getAllParts(String name) throws MalformedMultipartDataException, IOException {
		ensureComplete();
		List<PartReader> partStreamings = parts.get(name);

		if (partStreamings != null) {
			return partStreamings;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Allows to access the part in lazy streaming way. If the order of the random access is like the order of the parts
	 * in the # original stream the processing can happen without temporary storage
	 *
	 */
	@Override
	public PartReader getFirstPart(String name) throws MalformedMultipartDataException, IOException {
		List<PartReader> partStreamings = parts.get(name);

		if (partStreamings != null) {
			return partStreamings.get(0);
		}

		PartReader partStreaming = null;

		while ((partStreaming = dataStreaming.next()) != null) {
			if (partStreaming.getName().equals(name)) {
				registerPart(partStreaming);
				return partStreaming;
			} else {
				partStreaming.backup();
				registerPart(partStreaming);
			}
		}

		complete = true;

		return null;
	}

	protected void registerPart(PartReader partStreaming) {
		String name = partStreaming.getName();
		List<PartReader> partStreamings = parts.get(name);

		if (partStreamings == null) {
			partStreamings = new ArrayList<>();
			parts.put(name, partStreamings);
		}

		partStreamings.add(partStreaming);
	}

	@Override
	public void close() throws Exception {
		dataStreaming.close();
	}

}
