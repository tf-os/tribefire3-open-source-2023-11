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
package com.braintribe.swagger.util;

import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;

public class ExchangePackageResourceToModel implements Transformation<Resource, GmMetaModel> {

	@Override
	public GmMetaModel transform(Resource resource, PersistenceGmSession session) {
    	final ExchangePackage exchangePackage = new ExchangePackageResourceToExchangePackage().transform(resource, session);
    	final GmMetaModel gmMetaModel = new ExchangePackageToModel().transform(exchangePackage, session);
    	return gmMetaModel;
    }

}