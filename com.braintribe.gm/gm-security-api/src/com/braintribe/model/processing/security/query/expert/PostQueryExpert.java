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
package com.braintribe.model.processing.security.query.expert;

import com.braintribe.model.processing.security.query.context.PostQueryExpertContext;
import com.braintribe.model.query.QueryResult;

/**
 * An expert that is used during the processing of the {@link QueryResult}. This expert should have one method, which is
 * able tell if something is accessible or not, according to provided {@link PostQueryExpertContext}. For technical
 * reasons, the definition of this method is left for sub-interfaces, since the parameter types differ slightly for
 * different expert types.
 * <p>
 * Example, implementation specific (SecurityAspect): Basically all the ILS experts belong to this category, as well as
 * experts for ETILS for situations, where ETILS cannot be applied in query (since only Source are adjusted for ETILS
 * conditions, but if e.g. some entity has property whose value should not be visible based on ETILS, this value is
 * returned in query result, and must be removed later).
 * 
 * @see EntityAccessExpert
 * @see PropertyRelatedAccessExpert
 */
public interface PostQueryExpert extends AccessSecurityExpert {
	// blank
}
