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
package com.braintribe.model.processing.resource.enrichment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.resource.specification.ResourceSpecification;

public class DelegatingSpecificationDetector implements ResourceSpecificationDetector<ResourceSpecification> {

	
	private Map<String, ResourceSpecificationDetector<?>> detectorMap;
	
	@Configurable
	@Required
	public void setDetectorMap(Map<String, ResourceSpecificationDetector<?>> detectorMap) {
		this.detectorMap = detectorMap;
	}
	
	@Override
	public ResourceSpecification getSpecification(InputStream in, String mimeType, GmSession session) throws IOException {
		ResourceSpecificationDetector<?> delegate = detectorMap.get(mimeType);
		if (delegate != null) {
			return delegate.getSpecification(in, mimeType, session);
		}
		
		return null;
	}
	
}
