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
package tribefire.extension.hibernate.edr2cc.denotrans;

import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
class HbmConfigurer_UserSessions extends AbstractHbmConfigurer {

	public static void run(HibernateAccessEdr2ccEnricher enricher) {
		new HbmConfigurer_UserSessions(enricher).run();
	}

	private HbmConfigurer_UserSessions(HibernateAccessEdr2ccEnricher enricher) {
		super(enricher, "hbm:edr2cc/user-sessions/");
	}

	private void run() {
		mdEditor.onEntityType(PersistenceUserSession.T) //
				.addPropertyMetaData(screamingCamelCaseConversion()) //
				.addMetaData(entityMapping("TF_US_PERSISTENCE_USER_SESSION", PersistenceUserSession.T)) //
				.addPropertyMetaData(GenericEntity.id, stringTypeSpecification()) //
				.addPropertyMetaData(PersistenceUserSession.expiryDate,
						index("IDX_EXPIRY_DATE", propMapping("EXPIRY_DATE", "PersistenceUserSession/expiryDate")));
	}

}
