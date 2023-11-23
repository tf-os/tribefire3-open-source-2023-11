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
package com.braintribe.model.processing.query.test.model.indexed;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.meta.Indexed;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
@ToStringInformation("${#type_short}[${ambig}]")
public interface IndexedA extends GenericEntity, IsIndexed {

	EntityType<IndexedA> T = EntityTypes.T(IndexedA.class);

	String unique = "unique";
	String ambig = "ambig";
	String metric = "metric";

	/** This is not @Unique on purpose, to test that instances of {@link IndexedASub} can have the same value as instances of IndexAB */
	@Indexed
	String getUnique();
	void setUnique(String unique);

	@Indexed
	String getAmbig();
	void setAmbig(String ambig);

	@Indexed
	String getMetric();
	void setMetric(String metric);
	
	// @formatter:off
	@Override default void putAmbig(String ambig) { setAmbig(ambig); }
	@Override default void putUnique(String unique) { setUnique(unique); }
	@Override default void putMetric(String metric) { setMetric(metric); }
	// @formatter:on

}
