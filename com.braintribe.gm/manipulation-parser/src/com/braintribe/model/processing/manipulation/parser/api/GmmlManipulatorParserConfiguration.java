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
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.StrictErrorHandler;

public interface GmmlManipulatorParserConfiguration extends GmmlParserConfiguration {

	/**
	 * Registry for {@link ProblematicEntitiesRegistry#isProblematic(String) problematic entities}.
	 * <p>
	 * If the manipulator-parser encounters a problematic entity (i.e. a lookup of given globalId happens), then it raises the
	 * {@link GmmlManipulatorErrorHandler#problematicEntityReferenced(String)}.
	 * <p>
	 * The basic use-case for using this is lenient parsing of successive files (e.g. CSA stages), where errors in one file are ignored, but the
	 * corresponding entities are collected so we can report a possible follow-up problems in the files after.
	 * <p>
	 * After the parser is done, and there were new problematic entities discovered (and the errors were handled leniently), the parser calls
	 * {@link ProblematicEntitiesRegistry#recognizeProblematicEntities(java.util.Set)} with
	 */
	ProblematicEntitiesRegistry problematicEntitiesRegistry();

	/** If null is returned, the parser uses {@link StrictErrorHandler} per default. */
	GmmlManipulatorErrorHandler errorHandler();

	Set<GenericEntity> previouslyCreatedEntities();

	Set<String> homeopathicVariables();

}
