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
import java.util.Collection;
import java.util.Set;

public interface RandomAccessFormDataReader extends FormDataReader {
	/**
	 * This method gives overview over all existing parts names which involves to read and cache the complete
	 * outstanding parts in temporary file system storage. This can be supoptimial in cases where temporary storage
	 * should be avoided if possible
	 *
	 * @return
	 */
	public Set<String> getPartNames() throws MalformedMultipartDataException, IOException;

	/**
	 * This method returns all parts for the given name which involves to read and cache the complete outstanding parts
	 * in temporary file system storage. This can be supoptimial in cases where temporary storage should be avoided if
	 * possible
	 *
	 * @return
	 */
	public Collection<PartReader> getAllParts(String name) throws MalformedMultipartDataException, IOException;

	/**
	 * Allows to access the part in lazy streaming way. If the order of the random access is like the order of the parts
	 * in the # original stream the processing can happen without temporary storage
	 *
	 * @param name
	 * @return
	 * @throws MalformedMultipartDataException
	 * @throws IOException
	 */
	PartReader getFirstPart(String name) throws MalformedMultipartDataException, IOException;
}
