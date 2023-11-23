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
package com.braintribe.mimetype;

import java.io.File;
import java.io.InputStream;

/**
 * Mime type detector interface.
 *
 *
 */
public interface MimeTypeDetector {

	/**
	 * Reads the mimetype for the given file and filename.
	 *
	 * @param file
	 *            Input file
	 * @param fileName
	 *            Filename
	 * @return Mimetype or application/octet-stream, if not found
	 */
	String getMimeType(File file, String fileName) throws RuntimeException;

	/**
	 * <p>
	 * Reads the mimetype for the given input stream and filename.
	 *
	 * <p>
	 * The given {@link InputStream} is never closed by this method, and might not be fully consumed.
	 *
	 * @param is
	 *            Input stream
	 * @param fileName
	 *            File name
	 * @return Mimetype or application/octet-stream, if not found
	 */
	String getMimeType(InputStream is, String fileName);

}
