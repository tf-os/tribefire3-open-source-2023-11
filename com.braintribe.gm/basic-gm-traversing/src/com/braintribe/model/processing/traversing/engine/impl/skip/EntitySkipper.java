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
package com.braintribe.model.processing.traversing.engine.impl.skip;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.skip.Skipper;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.impl.visitors.GmTraversingVisitorAdapter;

public abstract class EntitySkipper extends GmTraversingVisitorAdapter implements Skipper {

	private SkipUseCase skipUseCase = DefaultSkipUseCase.INSTANCE;

	@Override
	public SkipUseCase getSkipUseCase() {
		return skipUseCase;
	}

	@Override
	public void setSkipUseCase(SkipUseCase skipUseCase) {
		this.skipUseCase = skipUseCase;
	}

	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		Object value = pathElement.getValue();
		if (value == null)
			return;

		if (pathElement.getType().getTypeCode() != TypeCode.entityType) {
			return;
		}

		if (shouldSkipEntity(context, (GenericEntity) value, pathElement)) {
			// update a skipUseCase in context
			context.skipDescendants(getSkipUseCase());
		}
	}

	protected abstract boolean shouldSkipEntity(GmTraversingContext context, GenericEntity entity, TraversingModelPathElement pathElement);

}
