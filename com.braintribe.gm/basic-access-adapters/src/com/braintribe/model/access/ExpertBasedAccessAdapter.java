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
package com.braintribe.model.access;

import java.util.Collections;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.crud.CrudExpertAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;

/**
 * 
 * This implementation of {@link IncrementalAccess} extends the functionality of the {@link BasicAccessAdapter} by adding an expert registry. 
 * This registry can be used to register simple {@link Supplier}'s for each of the EntityTypes available in the model. 
 * A Provider must return a {@link Iterable} containing entities of the type the provider is registered for.  
 *
 *
 * @deprecated - use {@link CrudExpertAccess} as a replacement
 */
@Deprecated
public class ExpertBasedAccessAdapter extends BasicAccessAdapter {

	private Logger logger = Logger.getLogger(ExpertBasedAccessAdapter.class);

	private GmExpertRegistry registry;

	
	@Configurable @Required
	public void setRegistry(GmExpertRegistry registry) {
		this.registry = registry;
	}
	
	/**
	 * Overrides {@link #queryPopulation(String)} from {@link BasicAccessAdapter} and tries to find a registered expert for the given
	 * typeSignature. If an expert could be found the result of it's provide method is also the result of this method.
	 * If no expert could be found for the given type an empty collection is returned.  
	 */
	@Override
	protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
		
		Supplier<Iterable<GenericEntity>> expert = registry.findExpert(Supplier.class).forType(typeSignature);
		if (expert == null) {
			logger.debug("No expert found for type: "+typeSignature);
			return Collections.emptySet();
		}
		try {
			return expert.get();	
		} catch (Exception e) {
			throw new ModelAccessException("Could not determine population for type: "+typeSignature,e);
		}
		
	}
	

	
}
