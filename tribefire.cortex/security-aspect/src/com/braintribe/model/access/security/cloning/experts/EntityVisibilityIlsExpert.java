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
package com.braintribe.model.access.security.cloning.experts;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.security.query.context.EntityExpertContext;
import com.braintribe.model.processing.security.query.expert.EntityAccessExpert;

/**
 * Verifies whether a given entity is visible.
 */
public class EntityVisibilityIlsExpert implements EntityAccessExpert {

	private static final Logger log = Logger.getLogger(EntityVisibilityIlsExpert.class);

	/**
	 * @return <tt>true</tt> iff we are visiting an entity that is visible
	 */
	@Override
	public boolean isAccessGranted(EntityExpertContext expertContext) {
		GenericEntity entity = expertContext.getEntity();

		if (entity == null) {
			return true;
		}

		ModelMdResolver mdResolver = expertContext.getMetaData();

		return isEntityVisible(entity, mdResolver);
	}

	private boolean isEntityVisible(GenericEntity entity, ModelMdResolver mdResolver) {
		try {
			return mdResolver.entity(entity).is(Visible.T);

		} catch (CascadingMetaDataException e) {
			log.debug("Error while resolving EntityVisibility meta data. " + e);

			return false;
		}
	}

}
