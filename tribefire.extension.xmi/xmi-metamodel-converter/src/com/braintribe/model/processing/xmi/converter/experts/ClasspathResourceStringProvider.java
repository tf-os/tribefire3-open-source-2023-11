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
package com.braintribe.model.processing.xmi.converter.experts;

import java.io.InputStream;
import java.util.function.Supplier;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.IOTools;



/**
 * lill' helper that returns the content of a template residing in the classpath 
 * @author pit
 *
 */
public class ClasspathResourceStringProvider implements Supplier<String> {
	private String key;
	private String content;
	
	/**
	 * @param key - the name of the template as it is required to find it in the CP
	 */
	public ClasspathResourceStringProvider(String key) {
		this.key = key;
	}

	@Override
	public String get() {
		// check cache 
		if (content != null) {
			return content;
		}
		// get it from the CP
		try ( InputStream in = getClass().getClassLoader().getResourceAsStream(key)) {
			content = IOTools.slurp(in, "ISO-8859-1");
			return content;
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot supply content for template [" + key + "]", IllegalStateException::new);
		}	
	}

}
