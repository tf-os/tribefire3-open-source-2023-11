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
package com.braintribe.model.cortexapi.connection;


import com.braintribe.model.accessdeployment.IncrementalAccess;
//import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface SynchronizeModelWithDbSchema extends DbSchemaRequest {
	
	EntityType<SynchronizeModelWithDbSchema> T = EntityTypes.T(SynchronizeModelWithDbSchema.class);

	// TODO extract this to a hibernate access and cofigure it as an extension on the cortex model.
//	void setAccess(HibernateAccess access);
//	HibernateAccess getAccess();

	// temporary fix
	void setAccess(IncrementalAccess access);
	IncrementalAccess getAccess();
	
	@Initializer("true")
	boolean getResolveRelationships();
	void setResolveRelationships(boolean resolveRelationships);
	
	@Initializer("true")
	boolean getIgnoreUnsupportedTables();
	void setIgnoreUnsupportedTables(boolean ignoreUnsupportedTables);

}
