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
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

/**
 * the basic interface for all state change processor implementation
 * 
 * @param <T>
 *            - is an extension of a {@link GenericEntity} (i.e. in most cases a Process)
 * 
 * @author pit
 * @author dirk
 */
public interface StateChangeProcessor<T extends GenericEntity, C extends GenericEntity> {

	/**
	 * called <b>synchronously</b> before the actual changes are applied
	 * <p>
	 * as it gives access to the state of the relevant property prior to any changes, use this point cut to store the
	 * last value (needed for edge processing)
	 * <p>
	 * Caution : not all values may be present - if the process entity is a preliminary entity, it will not exist at
	 * this point and trying to retrieve it will result in a null return value
	 * <p>
	 * Caution: It is possible to manipulate contents of the related session and commit it or let it be autocommited but
	 * nevertheless one should really think about the consequences (feedback loops, deadlocks, vetos).
	 * 
	 * @param context
	 *            - the {@link BeforeStateChangeContext} that contains all relevant data
	 * @throws StateChangeProcessorException
	 *             - only thrown if something prevents the change to take place (veto)
	 */
	default C onBeforeStateChange(BeforeStateChangeContext<T> context) throws StateChangeProcessorException { return null; }

	/**
	 * called <b>synchronously</b> after the actual changes were applied -
	 * 
	 * @param context
	 *            - the {@link AfterStateChangeContext} that contains all relevant data
	 * @param customContext
	 *            - the custom context deriving from {@link GenericEntity}
	 * @throws StateChangeProcessorException
	 *             - has no influence on the system whatsoever, so throw it whenever you have a problem
	 */
	default void onAfterStateChange(AfterStateChangeContext<T> context, C customContext) throws StateChangeProcessorException {}

	/**
	 * actual processing method called <b>asynchronously</b>
	 * 
	 * @param context
	 *            - the {@link ProcessStateChangeContext} that contains all relevant data
	 * @param customContext
	 *            - the custom context deriving from {@link GenericEntity}
	 * @throws StateChangeProcessorException-
	 *             has no influence on the system whatsoever, so throw it whenever you have a problem
	 */
	default void processStateChange(ProcessStateChangeContext<T> context, C customContext) throws StateChangeProcessorException {}

	default StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.defaultCapabilities();
	}
	
	/**
	 * Factory method for the context which will be presented by {@link StateChangeContext#getProcessorContext()} 
	 * to each call of this processor during one commit transaction. That context can be used to manage some form of coalescing 
	 * in case this processor is being triggered multiple times.
	 */
	default Object createProcessorContext() { return null; }
	
//	default void onPreCommit(ProcessorTransactionContext context) {}
//	default void onPostCommit(ProcessorTransactionContext context) {};
}
