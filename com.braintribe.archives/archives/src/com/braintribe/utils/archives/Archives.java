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
package com.braintribe.utils.archives;

import java.util.zip.ZipFile;

import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;
import com.braintribe.utils.archives.zip.impl.ZipContextEntryImpl;
import com.braintribe.utils.archives.zip.impl.ZipContextImpl;

/**
 * the starting point of the voyage into the land of archives <br/>
 * current implementation supports ZIP file via {@link ZipFile}
 * @author pit
 *
 */
public class Archives {
	
	/**
	 * the starting point: creates a new {@link ZipContext} and returns it
	 * @return - the created {@link ZipContext}
	 */
	public static ZipContext zip() {
		return new ZipContextImpl();
	}
	
	/**
	 * create an empty {@link ZipContextEntry} and return it
	 * @return - the created {@link ZipContextEntry}
	 */
	public static ZipContextEntry entry() {
		return new ZipContextEntryImpl();
	}
	
}
