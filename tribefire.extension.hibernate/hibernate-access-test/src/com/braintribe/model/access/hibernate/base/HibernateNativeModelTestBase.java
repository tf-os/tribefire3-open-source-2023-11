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
package com.braintribe.model.access.hibernate.base;

import com.braintribe.model.access.hibernate.base.model.n8ive.AmbiguousEntity;
import com.braintribe.model.access.hibernate.base.model.n8ive.Player;
import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public abstract class HibernateNativeModelTestBase extends HibernateAccessRecyclingTestBase {

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.n8ive();
	}

	// #################################################
	// ## . . . . . . . . Data creation . . . . . . . ##
	// #################################################

	protected Player player(String name) {
		return create(Player.T, name);
	}

	protected Player player(String name, String lastName) {
		Player result = create(Player.T, name);
		result.setLastName(lastName);
		return result;
	}

	protected Player player(String name, Player teammate) {
		Player result = create(Player.T, name);
		result.setTeammate(teammate);
		return result;
	}

	protected AmbiguousEntity ambigTop(String name) {
		return create(AmbiguousEntity.T, name);
	}

	protected com.braintribe.model.access.hibernate.base.model.n8ive.sub.AmbiguousEntity ambigSub(String name) {
		return create(com.braintribe.model.access.hibernate.base.model.n8ive.sub.AmbiguousEntity.T, name);
	}

}
