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
package com.braintribe.persistence.hibernate.adaptor;

import java.io.File;
import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;

public class CompoundHibernateConfigurationAdaptor implements HibernateConfigurationAdaptor {
	
	private List<HibernateConfigurationAdaptor> adaptors;

	protected static Logger log = Logger.getLogger(CompoundHibernateConfigurationAdaptor.class);

	@Required
	public void setAdaptors(List<HibernateConfigurationAdaptor> adaptors) {
		this.adaptors = adaptors;
	}
	
	@Override
	public void adaptEhCacheConfigurationResource(File configurationResourceUrl) throws Exception {
		for (HibernateConfigurationAdaptor adaptor : adaptors) {
			adaptor.adaptEhCacheConfigurationResource(configurationResourceUrl);
		}
	}
	
	@Override
	public void cleanup() {
		for (HibernateConfigurationAdaptor adaptor : adaptors) {
			try {
				adaptor.cleanup();
			} catch (Throwable t) {
				log.error("Failed to cleanup adaptor "+adaptor, t);
			}
		}
	}

}
