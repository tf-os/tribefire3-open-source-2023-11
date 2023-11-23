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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.user.statistics.UserStatistics;

/**
 * @author peter.gazdik
 */
class HbmConfigurer_UserStatistics extends AbstractHbmConfigurer {

	public static void run(HibernateAccessEdr2ccEnricher enricher) {
		new HbmConfigurer_UserStatistics(enricher).run();
	}

	private HbmConfigurer_UserStatistics(HibernateAccessEdr2ccEnricher enricher) {
		super(enricher, "hbm:edr2cc/user-sessions/");
	}

	private void run() {
		mdEditor.onEntityType(UserStatistics.T) //
				.addPropertyMetaData(screamingCamelCaseConversion()) //
				.addMetaData(entityMapping("TF_SS_USER_STATISTICS", UserStatistics.T)) //
				.addPropertyMetaData(GenericEntity.id, stringTypeSpecification());
	}

}
