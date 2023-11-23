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
package tribefire.extension.xml.schemed.requestbuilder.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceDeleteBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceRetrieveBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUpdateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.processing.session.impl.session.AbstractGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.HasResourceReadAccess;
import com.braintribe.model.resource.api.ResourceReadAccess;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;

/**
 * a simple {@link GmSession} that can implement the {@link #openStream(Resource)} method for the resources 
 * it has created. 
 * 
 * @author pit
 * @deprecated TODO: Use BasicResourceAccess instead
 */
@Deprecated
public class ResourceProvidingSession extends AbstractGmSession implements ResourceAccess, HasResourceReadAccess {

	@Override
	public void noticeManipulation(Manipulation manipulation) {		
	}

	@Override
	public ResourceUrlBuilder url(Resource resource) {
		return null;
	}

	@Override
	public ResourceUpdateBuilder update(Resource resource) {
		return null;
	}
	
	@Override
	public ResourceCreateBuilder create() {
		return null;
	}

	@Override
	public ResourceRetrieveBuilder retrieve(Resource resource) {
		return null;
	}

	@Override
	public ResourceDeleteBuilder delete(Resource resource) {
		return null;
	}

	@Override
	public InputStream openStream(Resource resource) throws IOException {
		ResourceSource resourceSource = resource.getResourceSource();
		if (resourceSource instanceof FileSystemSource) {
			FileSystemSource filesystemSource = (FileSystemSource) resourceSource;
			File file = new File( filesystemSource.getPath());
			return new FileInputStream(file);			
		}
		throw new IllegalArgumentException("resource source [" + resourceSource.getClass().getName() + "] is not supported");
	}

	
	@Override
	public ResourceReadAccess resources() {
		return this;
	}


	
}
