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

import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.access.hibernate.base.model.simple.HierarchySubA;
import com.braintribe.model.access.hibernate.base.model.simple.HierarchySubB;
import com.braintribe.model.meta.GmMetaModel;

/**
 * @author peter.gazdik
 */
public abstract class HibernateBaseModelTestBase extends HibernateAccessRecyclingTestBase {

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.basic_NoPartition();
	}

	// #################################################
	// ## . . . . . . . . Data creation . . . . . . . ##
	// #################################################

	protected BasicScalarEntity createBse(String name) {
		return create(BasicScalarEntity.T, name);
	}

	protected BasicEntity createBe(String name) {
		return create(BasicEntity.T, name);
	}

	protected HierarchySubA createSubA(String name) {
		return create(HierarchySubA.T, name);
	}

	protected HierarchySubB createSubB(String name) {
		return create(HierarchySubB.T, name);
	}

}
