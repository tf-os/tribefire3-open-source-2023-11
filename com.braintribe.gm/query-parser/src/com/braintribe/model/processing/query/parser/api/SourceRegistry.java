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

import java.util.Map;

import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Source;

/**
 * An interface that represents all the needed operation for linking aliases and
 * sources ({@link Source})
 */
public interface SourceRegistry {

	/**
	 * Registers an alias and a source together. If a temporary source
	 * (SourceLink) already exists, it is adjusted to reflect the actual source.
	 * 
	 * Validates that a source can only be registered once with the same alias.
	 * 
	 * @param alias
	 *            Alias for source
	 * @param source
	 *            {@link Source}
	 * 
	 */
	void registerSource(String alias, Source source);

	/**
	 * If a {@link Source} has been registered for the alias, it is returned.
	 * Otherwise a temporary {@link Source} (SourceLink) is returned as a place
	 * holder.
	 * 
	 * @param alias
	 *            Alias for Source
	 * @return {@link Source} that is represented by the alias.
	 */
	Source acquireSource(String alias);

	/**
	 * If a {@link Join} has been registered for the alias, it is returned.
	 * Otherwise a temporary {@link Source} (SourceLink) is returned as a place
	 * holder.
	 * 
	 * @param alias
	 *            Alias for Join
	 * @return {@link Join} that is represented by the alias.
	 */
	Join acquireJoin(String alias);

	/**
	 * Checks if a source has been registered with a certain alias.
	 * 
	 * @param alias
	 *            Alias for the source
	 * @return true if source is registered, false otherwise
	 */
	boolean validateIfSourceExists(String alias);

	/**
	 * Validates that all {@link Source} have been provided and that there is no
	 * alias without a proper {@link Source}.
	 * 
	 * Validates null sources for {@link EntityQuery} and {@link PropertyQuery}
	 * as well.
	 * 
	 * In case of strictValidation is true, the validation will throw an
	 * exception if any source has not been defined properly. Otherwise, in case
	 * of strictValidation being false, the offending source will be deleted
	 * from the registry instead.
	 * 
	 * @param strictValidation
	 *            A flag that indicates if an exception will be thrown in case
	 *            of malformed sources, or if the source will be simply deleted
	 *            from the registry
	 */
	void validateRegistry(boolean strictValidation);

	/**
	 * @return a {@link Map} where the keys are aliases and the values are the
	 *         corresponding {@link Source}
	 */
	Map<String, Source> getSourcesRegistry();

}
