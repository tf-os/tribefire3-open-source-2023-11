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
package com.braintribe.model.access.hibernate.base.wire.contract;

import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

/**
 * Offers various models configured with hibernate mappings.
 * 
 * @author peter.gazdik
 */
public interface HibernateModelsContract extends WireSpace {

	/** Basic model with all types, partition is not mapped. */
	GmMetaModel basic_NoPartition();

	/** {@link BasicEntity} has compositeId of integerValue + stringValue */
	GmMetaModel compositeId();

	/** Model for testing native HQL. */
	GmMetaModel n8ive();

	/** Model for testing TCs. */
	GmMetaModel graph();

	/** Model for testing {@link HasAcl ACL}. */
	GmMetaModel acl();

	/** Model with a single type that is not on the classpath. */
	GmMetaModel nonClasspath();

}
