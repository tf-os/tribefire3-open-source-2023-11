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
package com.braintribe.model.processing.query.parser.api;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A representation for semantic errors encountered during parsing. One should
 * only encounter this type of error, only if syntactically everything was
 * sound. Examples for such error:
 * <ul>
 * <li>"Join provided with no defined sourceAlias, propertyName [xyz]"</li>
 * <li>"Source expected and not registered, provided alias: [xyz]"</li>
 * </ul>
 * 
 * In addition to the semantic problems, this error is used seldom to represent
 * errors encountered in some evaluations that use external components, eg. Date
 * evaluation.
 */

public interface GmqlSemanticParsingError extends GmqlParsingError {

	EntityType<GmqlSemanticParsingError> T = EntityTypes.T(GmqlSemanticParsingError.class);

}
