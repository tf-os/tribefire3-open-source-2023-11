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
package com.braintribe.spring.loader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.braintribe.logging.Logger;


/**
 * byte [] based resource provider 
 * 
 * @author pit
 *
 */
public class ByteResourceProvider extends GenericResourceLoader implements Supplier<byte[]> {

	private static Logger log = Logger.getLogger(ByteResourceProvider.class);
	private String url = null;	
	
	@Required
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	@Override
	public byte[] get() throws RuntimeException {
		try {
			
			Resource urlResource = null;
			try {
				URL urlR = getClass().getClassLoader().getResource(url);
				urlResource = new UrlResource( urlR.toURI().toString());
			} catch (Exception e) {
				String msg = "cannot retrieve resource denoted by URL [" + url + "]";
				log.error( msg, e);
			}
			if (urlResource == null)
				urlResource = getResourceByPath( url);
		
			InputStream in = urlResource.getURL().openStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			byte[] buf = new byte[ 1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
			    out.write(buf, 0, count);
			}					
			return out.toByteArray();
		} catch (Exception e) {
			String msg="cannot provide contents of URL [" + url + "] as " + e; 
			throw new RuntimeException( msg, e);
		} 
			
	}
		
	
}
