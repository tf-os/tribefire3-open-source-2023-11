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

import org.apache.commons.lang.NotImplementedException;

import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchangeapi.ReadFromResource;
import com.braintribe.model.exchangeapi.ReadFromResourceResponse;
import com.braintribe.model.processing.exchange.service.FromResourceReader;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;

public class ExchangePackageResourceToExchangePackage implements Transformation<Resource, ExchangePackage> {

	@Override
    public ExchangePackage transform(Resource resource, PersistenceGmSession session) {
		final ReadFromResource rfr = ReadFromResource.T.create();
		rfr.setResource(resource);

		final FromResourceReader frr = new FromResourceReader(rfr);
		final ReadFromResourceResponse rfrr = frr.run();
		final Object assembly = rfrr.getAssembly();

		if (!(assembly instanceof ExchangePackage)) {
			throw new NotImplementedException("Decoded assembly is not an exchange package.");
		}
		
		return (ExchangePackage)assembly;
    }


}