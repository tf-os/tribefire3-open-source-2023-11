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
package com.braintribe.product.rat.imp;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.product.rat.imp.impl.deployable.CollaborativeSmoodAccessImp;

/**
 * An imp manages an instance of a certain type of {@link GenericEntity} on which it can perform typical operations. For
 * example a {@link CollaborativeSmoodAccessImp} manages an instance of {@link CollaborativeSmoodAccess} and can amongst other things deploy or
 * undeploy it. Imps also have a {@link #session() session}.
 * <p>
 * <b>Note:</b> An imp's instance must be attached to its session and persisted in this session's access.
 *
 * @param <T>
 *            type of the instance managed by this imp
 */
public interface Imp<T extends GenericEntity> extends HasSession {

	/**
	 * Returns instance managed by this imp.
	 */
	T get();

	/**
	 * Deletes the instance/entity managed by this imp from the access/session.
	 */
	void delete();

}
