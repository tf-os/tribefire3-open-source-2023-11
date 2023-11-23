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
package com.braintribe.model.processing.web.rest;

import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.model.rest.Prettiness;

public interface RestMarshalling {
	<T> T decodeAssembly(String encodedAssembly, String mimeType, boolean absenceInfoOnMissingProperties) throws RestRequestException;
	<T> T readAssembly(InputStream in, String mimeType, boolean absenceInfoOnMissingProperties) throws RestRequestException;
	void writeAssembly(OutputStream out, String mimeType, Object assembly, Prettiness prettiness) throws RestRequestException;
}
