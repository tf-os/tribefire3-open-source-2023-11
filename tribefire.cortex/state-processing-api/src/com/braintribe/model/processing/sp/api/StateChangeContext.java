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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

/**
 * the interface that defines a state change context
 * 
 * contains all information required for a {@link StateChangeProcessor} to execute its payload code:<br/>
 * <br/>
 * if the context is passed in the {@link StateChangeProcessor#onBeforeStateChange} function, the data reflects the
 * state BEFORE the manipulation are applied, if passed in {@link StateChangeProcessor#onAfterStateChange} function, it
 * obviously reflects the state AFTER the application. The data influenced by the application are updated.<br/>
 * <br/>
 * <br/>
 * {@link EntityProperty} - the entity property that is involved <br/>
 * {@link PersistenceGmSession} - the session that allows the processor to commit its eventual changes<br/>
 * {@link GenericEntity} - the entity property that is involved, deriving from it that is<br/>
 * {@link PropertyManipulation} - the PropertyManipulation the is the manipulation that triggers the call. <br/>
 * 
 * @author pit
 * @author dirk
 */

public interface StateChangeContext<T extends GenericEntity> {

	/**
	 * access to the entity reference
	 * 
	 * @return - the EntityReference involved
	 */
	EntityReference getEntityReference();

	/**
	 * access to the entity property involved
	 * 
	 * @return - the EntityProperty involved
	 */
	EntityProperty getEntityProperty();

	/**
	 * access to the persistence session - use it commit any changes that might happen in your processor
	 * 
	 * @return - the PersistenceGmSession
	 */
	PersistenceGmSession getSession();

	PersistenceGmSession getSystemSession();

	/**
	 * returns the {@link CmdResolver} initialized with {@link GmMetaModel} of the access that is behind
	 * the {@link PersistenceGmSession} accessible via {@link #getSession()}.
	 */
	CmdResolver getCmdResolver();

	boolean wasSessionModified();
	
	boolean wasSystemSessionModified();

	/**
	 * access to the process entity we're attached to
	 * 
	 * @return - the process entity (an extension of GenericEntity)
	 */
	T getProcessEntity() throws StateChangeProcessorException;


	T getSystemProcessEntity() throws StateChangeProcessorException;

	/**
	 * access to the actual manipulation that triggered the call if it's a ChangeValueManipulation, it gives access to
	 * the intended/new-value of the property
	 * 
	 * @return - the manipulation involved (an extension of PropertyManipulation)
	 */
	<M extends Manipulation> M getManipulation();

	/**
	 * @return - the entity type involved
	 */
	<T1 extends T> EntityType<T1> getEntityType();

	/**
	 * Returns the context which was acquired by {@link CustomContextProvidingProcessor#createProcessorContext()}, if
	 * the processor which calls this method implements the {@linkplain CustomContextProvidingProcessor} interface.
	 * Otherwise, this method returns <tt>null</tt>.
	 * 
	 * @see CustomContextProvidingProcessor
	 */
	<E> E getProcessorContext();
	
	void overrideCapabilities(StateChangeProcessorCapabilities capabilities);
}
