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
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.security.query.context.PropertyExpertContext;
import com.braintribe.model.processing.security.query.expert.PropertyRelatedAccessExpert;

/**
 * Verifies whether a given property is visible.
 */
public class PropertyVisibilityIlsExpert implements PropertyRelatedAccessExpert {

	private static final Logger log = Logger.getLogger(PropertyVisibilityIlsExpert.class);

	/**
	 * @returns <tt>true</tt> iff we are visiting a visible property
	 */
	@Override
	public boolean isAccessGranted(PropertyExpertContext expertContext) {
		PropertyRelatedModelPathElement path = expertContext.getPropertyRelatedModelPathElement();

		String propertyName = path.getProperty().getName();
		GenericEntity entity = path.getEntity();
		ModelMdResolver mdResolver = expertContext.getMetaData();

		return isPropertyVisible(entity, propertyName, mdResolver);
	}

	private boolean isPropertyVisible(GenericEntity entity, String propName, ModelMdResolver mdResolver) {
		try {
			return mdResolver.entity(entity).property(propName).is(Visible.T);

		} catch (CascadingMetaDataException e) {
			log.warn("Error while resolving PropertyVisibility meta data. ", e);

			return false;
		}
	}

}
