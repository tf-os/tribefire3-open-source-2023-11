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
package com.braintribe.model.version;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a container that holds several {@link VersionInterval},
 * as Maven allows 
 * 
 * @author pit/dirk
 *
 */
public interface VersionIntervals extends VersionExpression {
	
	EntityType<VersionIntervals> T = EntityTypes.T(VersionIntervals.class);
	static final String elements = "elements";

	/**
	 * @return - the {@link VersionInterval} that make this up
	 */
	List<VersionInterval> getElements();
	void setElements( List<VersionInterval> elements);
	
	@Override
	default boolean matches(Version version) {
		for (VersionInterval interval : getElements()) {
			if (interval.matches(version))
				return true;
		}
		return false;
	}
	
	@Override
	default String asString() {
		return getElements().stream().map( VersionExpression::asString).collect(Collectors.joining( ","));
	}
	
	@Override
	default List<VersionInterval> asVersionIntervalList() {	
		return getElements();
	}
}
