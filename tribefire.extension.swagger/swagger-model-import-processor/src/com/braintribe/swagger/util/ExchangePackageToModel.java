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

import java.util.List;
import java.util.Optional;

import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchangeapi.Import;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ExchangePackageToModel implements Transformation<ExchangePackage, GmMetaModel> {

	@Override
    public GmMetaModel transform(ExchangePackage exchangePackage, PersistenceGmSession session ) {
		final Import importRequest = Import.T.create();
		importRequest.setExchangePackage(exchangePackage);
		importRequest.setCreateShallowInstanceForMissingReferences(true);
		
		List<GmMetaModel> models = 
				new ExchangePackageImporter(
						importRequest, 
						session,
						null)
				.run();

		Optional<GmMetaModel> findFirst = models.stream().findFirst();
		if (findFirst.isPresent()) {
			return findFirst.get();
		}
        return null;
    }

}