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
package com.braintribe.utils.mime;

import java.io.File;
import java.util.function.Function;

import org.apache.tika.Tika;

import com.braintribe.logging.Logger;


/**
 * @deprecated
 * Use MimeTypeDetector instead.
 * 
 *
 */
@Deprecated
public class TikaMimeTypeProvider implements Function<File, String> {

	protected static Logger logger = Logger.getLogger(TikaMimeTypeProvider.class);

	protected final Tika tika = new Tika();

	@Override
	public String apply(File file) throws RuntimeException {
		try {
			return this.tika.detect(file);
		} catch (Exception e) {
			throw new RuntimeException("Error while trying to detect MIME type of " + file + " using TIKA.", e);
		}
	}
}
