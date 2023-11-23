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
package com.braintribe.model.processing.license.glf;

import java.util.List;

import com.braintribe.model.license.License;
import com.braintribe.model.processing.license.exception.LicenseLoadException;
import com.braintribe.model.processing.license.exception.LicenseViolatedException;
import com.braintribe.model.processing.license.exception.NoLicenseConfiguredException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;

public class LicenseLoaderUtil {

	public static License getLicenseResource(PersistenceGmSession session) throws LicenseViolatedException {
		
		EntityQuery agreementQuery = EntityQueryBuilder.from(License.class)
				.where()
					.property(License.active).eq(true)
				.orderBy(License.uploadDate, OrderingDirection.descending)
				.done();
		
		License license = null;
		try {
			license = session.query().entities(agreementQuery).first();
		} catch(Exception e) {
			throw new LicenseLoadException("Could not query for License in the provided session.", e);
		}

		if (license == null)
			throw new NoLicenseConfiguredException("Could not find any license.");

		return license;
		
	}


	public static List<License> getLicenses(PersistenceGmSession session, License excludedLicense) throws Exception {
		
		EntityQuery agreementQuery = EntityQueryBuilder.from(License.class)
				.where()
					.property(License.id).ne(excludedLicense.getId())
				.done();
		List<License> licenseList = session.query().entities(agreementQuery).list();
		return licenseList;
		
	}
}
