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
package com.braintribe.model.generic.annotation.meta.api;

import java.lang.annotation.Annotation;

import com.braintribe.model.generic.annotation.meta.Aliases;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.mapping.Alias;

/**
 * Extension of {@link MdaHandler} for annotations which are repeatable.
 * <p>
 * For example {@link Alias} is a repeatable annotation, with the {@link Aliases} as it's aggregator.
 */
public interface RepeatableMdaHandler<A extends Annotation, RA extends Annotation, M extends MetaData> extends MdaHandler<A, M> {

	/** Returns the {@link MdaHandler} for the aggregator annotation. */
	RepeatableAggregatorMdaHandler<RA, M> aggregatorHandler();

	/**
	 * Special marker for {@link MdaHandler} for aggregator annotations. For example, for the annotation {@link Alias}, the corresponding aggregator
	 * is {@link Aliases}.
	 */
	public interface RepeatableAggregatorMdaHandler<RA extends Annotation, M extends MetaData> extends MdaHandler<RA, M> {
		// no extension
	}
}
