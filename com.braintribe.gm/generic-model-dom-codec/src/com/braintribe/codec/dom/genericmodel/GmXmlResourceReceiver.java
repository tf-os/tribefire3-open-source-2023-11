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
package com.braintribe.codec.dom.genericmodel;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

import org.w3c.dom.Document;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.utils.xml.XmlTools;

/**
 * a counter part for the GmXmlProvider.. just the other way round 
 * 
 * @author pit
 */
public class GmXmlResourceReceiver<T> implements Consumer<T>{
	
	private URL resource;
	protected GenericModelRootDomCodec<T> codec;
	
	public GmXmlResourceReceiver() {
		this.codec = new GenericModelRootDomCodec<>();
		GenericModelType type = GMF.getTypeReflection().getBaseType();
		this.codec.setType(type);	
	}
	
	public URL getResource() {
		return resource;
	}

	public void setResource(URL resource) {
		this.resource = resource;
	}

	
	@Override
	public void accept(T object) throws RuntimeException {
		try {
			Document document = codec.encode( object);
			URL resource = getResource();
			if (resource == null) {
				throw new RuntimeException( "resource may not be null at this point");
			}
			XmlTools.writeXml(document, new File( resource.toURI()), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException( "Error while encoding DOM!");
		}		
	}
	
	

}
