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
package com.braintribe.web.velocity.renderer.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;



public class TemplateProvider implements Supplier<String> {
	private String source;
	private String encoding;
	

	public void setSource(String source) {
		this.source = source;
	}	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public String get() throws RuntimeException {
		try {								
			URL url = getClass().getClassLoader().getResource( source);
									
			InputStream in = url.openStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			byte[] buf = new byte[ 1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
			    out.write(buf, 0, count);
			}					
			return out.toString( encoding);
		} catch (Exception e) {
			String msg="cannot provide contents of URL [" + source + "]"; 
			throw new RuntimeException( msg, e);
		} 
			
	}
}
