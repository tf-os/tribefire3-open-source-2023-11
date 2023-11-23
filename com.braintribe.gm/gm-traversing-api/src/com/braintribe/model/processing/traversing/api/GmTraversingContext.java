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
package com.braintribe.model.processing.traversing.api;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

/**
 * Context which provides access to the traversing-related data for the {@link GmTraversingVisitor} (well, using the right sub-type).
 * 
 * @see EnterContext
 * @see LeaveContext
 */
public interface GmTraversingContext {
	/**
	 * 
	 * @param predecessor
	 *            the explicit predecessor or null if the new event pair should be appended at the end
	 */
	GmTraversingEnterEvent appendEventPair(GmTraversingEvent predecessor, TraversingModelPathElement pathElement);

	/** Skip all following "enter" nodes. */
	void skipAll(SkipUseCase skipUseCase);

	/** Skip to the "leave" corresponding to the current node (if current node is enter). */
	void skipWalkFrame(SkipUseCase skipUseCase);

	/**
	 * Skip all enqueued nodes which are descendants of current node. This "descendant-relationship" is defined by the walker. For standard
	 * depth-first search this would be equivalent to {@link #skipWalkFrame(SkipUseCase)}. For standard breath-first search (that means
	 * first traverse siblings, only then children) this would mean we still traverse all the siblings, and then skip the children of the
	 * current node and go straight to the children of the next sibling.
	 */
	void skipDescendants(SkipUseCase skipUseCase);

	/** Stops immediately, does not even enter "leaves" for nodes that were entered but not "left" yet. */
	void abort();

	/** @return traversing depth of the current object. This depth is computed based on the */
	int getCurrentDepth();

	<T extends GmTraversingEvent> T getEvent();

	<T> T getSharedCustomValue(IModelPathElement pathElement);

	<T> T getVisitorSpecificCustomValue(IModelPathElement pathElement);

	void setSharedCustomValue(IModelPathElement pathElement, Object value);

	void setVisitorSpecificCustomValue(IModelPathElement pathElement, Object value);

	SkipUseCase getSkipUseCase();

}
