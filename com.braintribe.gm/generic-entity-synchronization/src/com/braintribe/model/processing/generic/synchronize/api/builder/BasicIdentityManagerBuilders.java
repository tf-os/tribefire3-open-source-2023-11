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
package com.braintribe.model.processing.generic.synchronize.api.builder;

import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.ExternalIdIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.GenericIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.GlobalIdIdentityManager;
import com.braintribe.model.processing.generic.synchronize.experts.IdPropertyIdentityManager;

/**
 * Wrapping builder offering options to fluently build basic {@link IdentityManager}'s.
 */
public interface BasicIdentityManagerBuilders<S extends GenericEntitySynchronization> {

	/**
	 * Returns the builder to create a customized {@link ExternalIdIdentityManager} 
	 */
	public ExternalIdIdentityManagerBuilder<S> externalId();
	
	/**
	 * Returns the builder to create a customized {@link GlobalIdIdentityManager} 
	 */
	public GlobalIdIdentityManagerBuilder<S> globalId();
	
	/**
	 * Returns the builder to create a customized {@link IdPropertyIdentityManager}
	 */
	public IdPropertyIdentityManagerBuilder<S> idProperty();

	/**
	 * Returns the builder to create a customized {@link GenericIdentityManager} 
	 */
	public GenericIdentityManagerBuilder<S> generic();
	
}
