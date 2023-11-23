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
package com.braintribe.spring.support.converter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.convert.converter.Converter;

/**
 * 
 * 
 * @deprecated please use {@link StringToUrlConverter} since this one cannot handle relative urls
 */
@Deprecated
public class UrlConverter implements Converter<String, URL> {
	
	public URL convert(String source) {
		if (source == null || source.trim().length() == 0) {
			return null;
		} else {
			try {				
				return new URL( source);
			} catch (MalformedURLException e) {
				File file = new File( source);
				if (file.exists()) {
					try {
						return file.toURI().toURL();
					} catch (MalformedURLException e1) {
						throw new IllegalArgumentException("[" + source + "] is not a valid format for an URL", e);
					}
				} else {
					throw new IllegalArgumentException("can't find file [" + file.getAbsolutePath() + "] ", e);
				}

			}
		}
		
	}

}
