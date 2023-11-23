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
package com.braintribe.devrock.zed.commons;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import com.braintribe.devrock.zed.api.context.CommonZedCoreContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * collection of common simple functions for ZED
 * 
 * @author pit
 *
 */
public class Commons {
	/**
	 * @param entityType
	 * @return
	 * @throws ZedException
	 */
	public static <T extends GenericEntity> T create(CommonZedCoreContext context, EntityType<T> entityType) throws ZedException {
		if (context.session() != null) {
			try {
				return (T) context.session().create(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";				
				throw new ZedException(msg, e);
			}
		} 
		else {
			return (T) entityType.create();
		}
	}
	
	public static TypeReferenceEntity ensureTypeReference( CommonZedCoreContext context, ZedEntity z) {
		if (z instanceof TypeReferenceEntity) 
			return (TypeReferenceEntity) z;
		
		TypeReferenceEntity tr = create(context, TypeReferenceEntity.T);
		tr.setReferencedType(z);
		return tr;
	}
	
	/**
	 * returns a 'comparable' URL so the originating jar can be identified 
	 * @param resourceUrl - the explicit URL from the {@link URLClassLoader}
	 * @return - an URL that specifies the jar level only (not the internal file)
	 */
	public static URL extractComparableResource(URL resourceUrl, String resourceName) {
		String asE = resourceUrl.toExternalForm();
		String originatingResource = null;
		int p = asE.indexOf('!');
		if (p > 0) {
			originatingResource = asE.substring(4, p); 
		}
		else {
			String urlSuffix = resourceName.replace('.', '/') + ".class";
			int len = urlSuffix.length();
			originatingResource = asE.substring(0, (asE.length() - len));
		}
				
		try {
			return new URL( originatingResource);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("cannot build trimmed URL from [" + asE + "]", e);
		}	
	}
	
	public static Artifact getOwningArtifact( Map<URL,Artifact> map, URL suspect) {
		Artifact artifact = null;
		String scannedUrl = suspect.toString();
		if (scannedUrl.startsWith( "jar:")) {
			scannedUrl = scannedUrl.substring( 4);
		}		
		for (Map.Entry<URL, Artifact> entry : map.entrySet()) {
			String artifactUrl = entry.getKey().toString();
			if (scannedUrl.startsWith(artifactUrl)) {
				artifact = entry.getValue();
				break;
			}
		}
		return artifact;
	}
}
