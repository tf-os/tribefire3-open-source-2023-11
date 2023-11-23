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
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.skip.Skipper;
import com.braintribe.model.processing.traversing.engine.api.usecase.DefaultSkipUseCase;
import com.braintribe.model.processing.traversing.impl.visitors.GmTraversingVisitorAdapter;

/**
 * A {@link Skipper} that is triggered if the path element is of type {@link TraversingPropertyModelPathElement}
 */
public abstract class TransferPropertyRelatedValueSkipper extends GmTraversingVisitorAdapter implements Skipper {

	private SkipUseCase skipUseCase = DefaultSkipUseCase.INSTANCE;

	private final PropertySkippingContextImpl propertySkippingContext = new PropertySkippingContextImpl();
	
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
		if (pathElement.getElementType() == ModelPathElementType.Property) {

			propertySkippingContext.context = context;
			propertySkippingContext.propertyPathElement = (TraversingPropertyModelPathElement) pathElement;
			
			if (shouldSkipProperty(propertySkippingContext)) {
				// update a skipUseCase in context
				context.skipDescendants(getSkipUseCase());
			}
		}
	}

	protected abstract boolean shouldSkipProperty(PropertySkippingContext context);

	static class PropertySkippingContextImpl implements PropertySkippingContext {

		public GmTraversingContext context;
		public TraversingPropertyModelPathElement propertyPathElement;

		
		@Override
		public GenericEntity getInstanceToBeCloned() {
			return (GenericEntity) propertyPathElement.getPrevious().getValue();
		}

		@Override
		public GenericEntity getClonedInstance() {
			return (GenericEntity) context.getSharedCustomValue(propertyPathElement.getPrevious());
		}

		@Override
		public Property getProperty() {
			return propertyPathElement.getProperty();
		}

		@Override
		public GmTraversingContext getTraversingContext() {
			return context;
		}

		@Override
		public TraversingPropertyModelPathElement getPropertyPathElement() {
			return propertyPathElement;
		}
		
		
	}
	
}
