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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.braintribe.logging.Logger;
import com.braintribe.utils.Base64;

public class ImageEmbeddingGenerator {
	private static Logger log = Logger.getLogger(ImageEmbeddingGenerator.class);

	public String embedImage( URL imageResource) {
		try {
			InputStream in = imageResource.openStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			byte[] buf = new byte[ 1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
			    out.write(buf, 0, count);
			}					
			byte [] bytes = out.toByteArray();
			String base64 = Base64.encodeBytes(bytes, Base64.DONT_BREAK_LINES);			
			return "data:image/gif;base64," + base64;
		} catch (MalformedURLException e) {
			String msg = "Cannot open resource stream as " + e;
			log.error( msg, e);
		} catch (IOException e) {
			String msg = "Cannot read from resource stream as " + e;
			log.error( msg, e);
		}
		return null;
		
	}
}
