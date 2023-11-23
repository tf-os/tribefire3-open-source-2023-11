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
package com.braintribe.model.processing.traversing.engine.api;

import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.engine.api.customize.ModelWalkerCustomization;

public interface TraversingConfigurer<T extends TraversingConfigurer<T>> {

	/**
	 * Configures a depth-first walker visitor.
	 * 
	 * @see #customWalk(GmTraversingVisitor)
	 */
	T depthFirstWalk();

	/**
	 * Configures a breadth-first walker visitor.
	 * 
	 * @see #customWalk(GmTraversingVisitor)
	 */
	T breadthFirstWalk();

	/**
	 * Configures custom walker visitor.
	 * <p>
	 * NOTE It is also possible to register a walker with {@link #visitor(GmTraversingVisitor)}, but this method also notifies the engine
	 * builder not to use the default one. If no walker is configured, the engine uses a depth-first walker as the very last visitor by
	 * default.
	 */
	T customWalk(GmTraversingVisitor visitor);

	/**
	 * Add {@link GmTraversingVisitor} to the existing visitors.
	 * 
	 */
	T visitor(GmTraversingVisitor visitor);

	/**
	 * Run all the visitors in the order that they were provided on the given target
	 */
	void doFor(Object target) throws GmTraversingException;

	/** Update the default walker with {@linkModelWalkerCustomization} */
	T customizeDefaultWalker(ModelWalkerCustomization customization);

}
