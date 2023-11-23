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
package com.braintribe.model.processing.manipulation.parser.api;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;

public interface MutableGmmlManipulatorParserConfiguration extends GmmlManipulatorParserConfiguration, MutableGmmlParserConfiguration {

	/** @see #errorHandler() */
	void setErrorHandler(GmmlManipulatorErrorHandler errorHandler);

	/** @see #problematicEntitiesRegistry() */
	void setProblematicEntitiesRegistry(ProblematicEntitiesRegistry registry);

	/** The set might be modified internally, so if a copy is needed, do it yourself. */
	void setPreviouslyCreatedEntities(Set<GenericEntity> createdEntities);

	/**
	 * Variables for entities which are created and also deleted in the parsed GMML text. This information has to probably be obtained by doing a
	 * pre-processing of the text, and could improve performance.
	 */
	void setHomeopathicVariables(Set<String> homeopathicVariables);

}
