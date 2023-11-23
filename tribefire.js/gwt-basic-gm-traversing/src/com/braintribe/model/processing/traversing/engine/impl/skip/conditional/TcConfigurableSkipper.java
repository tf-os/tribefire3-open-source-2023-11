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
package com.braintribe.model.processing.traversing.engine.impl.skip.conditional;

import com.braintribe.model.generic.path.api.IListItemModelPathElement;
import com.braintribe.model.generic.path.api.IMapKeyModelPathElement;
import com.braintribe.model.generic.path.api.IMapValueModelPathElement;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.IPropertyModelPathElement;
import com.braintribe.model.generic.path.api.IRootModelPathElement;
import com.braintribe.model.generic.path.api.ISetItemModelPathElement;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapEntryCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.pr.criteria.matching.TcIterator;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingSkippingCriteria;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.impl.visitors.ElementTypeOrientedVisitor;

/**
 * A {link ConditionalSkipper} that evaluates the skip based on
 * {@link TraversingCriterion}
 */
public class TcConfigurableSkipper extends ElementTypeOrientedVisitor implements ConditionalSkipper {
	private StandardMatcher matcher = new StandardMatcher();
	private GmTraversingSkippingCriteria skippingCriteria;
	private SkipUseCase skipUseCase;

	@Override
	public GmTraversingSkippingCriteria getSkippingCriteria() {
		return skippingCriteria;
	}

	@Override
	public void setSkippingCriteria(GmTraversingSkippingCriteria skippingCriteria) {
		this.skippingCriteria = skippingCriteria;
	}

	@Override
	public SkipUseCase getSkipUseCase() {
		return skipUseCase;
	}

	@Override
	public void setSkipUseCase(SkipUseCase skipUseCase) {
		this.skipUseCase = skipUseCase;
	}

	public void setMatcherCheckOnlyProperties(boolean flag) {
		matcher.setCheckOnlyProperties(flag);
	}

	public void setTraversionCriterion(TraversingCriterion traversingCriterion) {
		matcher.setCriterion(traversingCriterion);
	}

	public void setCheckOnlyProperties(boolean checkOnlyProperties) {
		matcher.setCheckOnlyProperties(checkOnlyProperties);
	}

	@Override
	protected void onRootEnter(GmTraversingContext context, IRootModelPathElement pathElement) {
		RootCriterion rootCriterion = RootCriterion.T.create();
		rootCriterion.setTypeSignature(pathElement.getType().getTypeSignature());
		associateTc(context, pathElement, rootCriterion, null, null);
	}

	@Override
	protected void onPropertyEnter(GmTraversingContext context, IPropertyModelPathElement pathElement) {
		PropertyCriterion propertyCriterion = PropertyCriterion.T.create();
		Property property = pathElement.getProperty();
		propertyCriterion.setPropertyName(property.getName());
		propertyCriterion.setTypeSignature(property.getType().getTypeSignature());
		associateTc(context, pathElement, propertyCriterion, null, null);
	}

	@Override
	protected void onListItemEnter(GmTraversingContext context, IListItemModelPathElement pathElement) {
		ListElementCriterion listElementCriterion = ListElementCriterion.T.create();
		CollectionType collectionType = pathElement.getPrevious().getType();
		listElementCriterion.setTypeSignature(collectionType.getCollectionElementType().getTypeSignature());
		associateTc(context, pathElement, listElementCriterion, null, null);
	}

	@Override
	protected void onSetItemEnter(GmTraversingContext context, ISetItemModelPathElement pathElement) {
		SetElementCriterion setElementCriterion = SetElementCriterion.T.create();
		CollectionType collectionType = pathElement.getPrevious().getType();
		setElementCriterion.setTypeSignature(collectionType.getCollectionElementType().getTypeSignature());
		associateTc(context, pathElement, setElementCriterion, null, null);
	}

	@Override
	protected void onMapKeyEnter(GmTraversingContext context, IMapKeyModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getPrevious().getType();
		MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
		mapEntryCriterion.setTypeSignature(collectionType.getTypeSignature());

		MapKeyCriterion mapKeyCriterion = MapKeyCriterion.T.create();
		mapKeyCriterion.setTypeSignature(collectionType.getParameterization()[0].getTypeSignature());
		associateTc(context, pathElement, mapKeyCriterion, mapEntryCriterion, pathElement.getMapEntry());
	}

	@Override
	protected void onMapValueEnter(GmTraversingContext context, IMapValueModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getPrevious().getType();
		MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
		mapEntryCriterion.setTypeSignature(collectionType.getTypeSignature());
		MapValueCriterion mapValueCriterion = MapValueCriterion.T.create();
		mapValueCriterion.setTypeSignature(collectionType.getParameterization()[1].getTypeSignature());
		associateTc(context, pathElement, mapValueCriterion, mapEntryCriterion, pathElement.getMapEntry());
	}

	private void skip(GmTraversingContext context) {
		switch (getSkippingCriteria()) {
			case skipAll:
				context.skipAll(getSkipUseCase());
				break;
			case skipDescendants:
				context.skipDescendants(getSkipUseCase());
				break;
			case skipWalkFrame:
				context.skipWalkFrame(getSkipUseCase());
				break;
		}

	}

	@SuppressWarnings({ "incomplete-switch", })
	protected void associateTc(GmTraversingContext context, IModelPathElement pathElement, BasicCriterion tc, BasicCriterion optionalPredecessor, Object optionalPredecessorValue) {
		IModelPathElement pathElementPredecessor = pathElement.getPrevious();
		TcLinkedListNode predecessor = pathElementPredecessor != null ? (TcLinkedListNode) context.getVisitorSpecificCustomValue(pathElementPredecessor) : null;

		if (optionalPredecessor != null) {
			TcLinkedListNode optionalNode = new TcLinkedListNode();
			optionalNode.value = optionalPredecessorValue;
			optionalNode.predecessor = predecessor;
			optionalNode.criterion = optionalPredecessor;
			predecessor = optionalNode;

			if (matcher.matches(new TcLinkedListIterator(optionalNode))) {
				skip(context);
				return;
			}
		}

		Object value = pathElement.getValue();

		TcLinkedListNode node = new TcLinkedListNode();
		node.value = value;
		node.criterion = tc;
		node.predecessor = predecessor;

		if (matcher.matches(new TcLinkedListIterator(node))) {
			skip(context);
		}

		switch (pathElement.getType().getTypeCode()) {
			case entityType:
				EntityCriterion entityCriterion = EntityCriterion.T.create();
				String typeSignature = pathElement.getType().getTypeSignature();
				entityCriterion.setTypeSignature(typeSignature);

				TcLinkedListNode entityNode = new TcLinkedListNode();
				entityNode.predecessor = node;
				entityNode.value = value;
				entityNode.criterion = entityCriterion;

				node = entityNode;

				if (matcher.matches(new TcLinkedListIterator(entityNode))) {
					skip(context);
					return;
				}

				break;
			case mapType:
				CollectionType mapType = pathElement.getType();
				MapCriterion mapCriterion = MapCriterion.T.create();
				mapCriterion.setTypeSignature(mapType.getTypeSignature());

				TcLinkedListNode mapNode = new TcLinkedListNode();
				mapNode.predecessor = node;
				mapNode.value = value;
				mapNode.criterion = mapCriterion;

				node = mapNode;

				if (matcher.matches(new TcLinkedListIterator(mapNode))) {
					skip(context);
					return;
				}

				break;
		}

		context.setVisitorSpecificCustomValue(pathElement, node);
	}

	private static class TcLinkedListNode {
		public Object value;
		public BasicCriterion criterion;
		public TcLinkedListNode predecessor;
	}

	private static class TcLinkedListIterator implements TcIterator {

		private TcLinkedListNode node;

		public TcLinkedListIterator(TcLinkedListNode node) {
			super();
			TcLinkedListNode anchor = new TcLinkedListNode();
			anchor.predecessor = node;
			this.node = anchor;
		}

		@Override
		public boolean hasPrevious() {
			return node.predecessor != null;
		}

		@Override
		public void previous() {
			node = node.predecessor;
		}

		@Override
		public BasicCriterion getCriterion() {
			return node.criterion;
		}

		@Override
		public Object getValue() {
			return node.value;
		}

		@Override
		public Object mark() {
			return node;
		}

		@Override
		public void reset(Object index) {
			this.node = (TcLinkedListNode) index;

		}

	}

}
