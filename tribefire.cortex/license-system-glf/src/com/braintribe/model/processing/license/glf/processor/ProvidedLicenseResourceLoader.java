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
package com.braintribe.model.processing.license.glf.processor;

import java.io.InputStream;

import com.auxilii.glf.client.exception.SystemException;
import com.auxilii.glf.client.loader.XMLLoader;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.license.glf.LicenseResourceLoader;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Resource;

public class ProvidedLicenseResourceLoader extends XMLLoader {

	protected static Logger logger = Logger.getLogger(LicenseResourceLoader.class);
	
	protected Resource licenseResource = null;
	protected PersistenceGmSession session = null;
	
	public ProvidedLicenseResourceLoader(PersistenceGmSession session, Resource licenseResource) throws SystemException {
		super();
		this.session = session;
		this.licenseResource = licenseResource;
	}

	@Override
	protected InputStream openLicenseStream() throws Exception {
		
		if (this.licenseResource == null)
			throw new Exception("No license resource available.");
		
		if (logger.isTraceEnabled())
			logger.trace("Opening license resource "+this.licenseResource.getId());
		
		ResourceAccess resourceAccess = this.session.resources();
		InputStream is = resourceAccess.retrieve(this.licenseResource).stream();
		
		return is;
	}

	@Override
	protected void saveState() {
		//Nothing to do here
	}
	
	@Override
	protected boolean stateChanged() {
		return false;
	}
}
