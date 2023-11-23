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
package com.braintribe.model.processing.sp.api;

import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * the context that is passed to the {@link StateChangeProcessorSelector} contains all relevant data :<br/>
 * 
 * {@link Manipulation} - the property manipulation involved<br/>
 * {@link EntityProperty} - the owning entity property if present<br/>
 * {@link EntityType} - the entity type of the entity involved <br/>
 * {@link Property} - the property involved if present<br/>
 * 
 * @author pit
 * @author dirk
 */
public interface StateChangeProcessorSelectorContext {

	/** @return - the property manipulation */
	<M extends Manipulation> M getManipulation();

	/** @return - the entity reference which the manipulation targets */
	EntityReference getEntityReference();

	/** @return - the entity property involved if a property related manipulation is given */
	EntityProperty getEntityProperty();

	/** @return - the entity type involved */
	EntityType<?> getEntityType();

	/** @return - the property involved */
	Property getProperty();

	CmdResolver getCmdResolver();

	PersistenceGmSession getSession();
	PersistenceGmSession getSystemSession();

	<T> T getCustomContext();
	void setCustomContext(Object customContext);

	boolean isForLifecycle();
	boolean isForInstantiation();
	boolean isForDeletion();
	boolean isForProperty();
}
