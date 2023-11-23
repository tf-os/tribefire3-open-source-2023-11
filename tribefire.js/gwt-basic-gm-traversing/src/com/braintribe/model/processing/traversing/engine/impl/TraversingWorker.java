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
package com.braintribe.model.processing.traversing.engine.impl;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingEnterEvent;
import com.braintribe.model.processing.traversing.api.GmTraversingEvent;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingRootModelPathElement;

public class TraversingWorker implements GmTraversingContext {

	private GmTraversingEventImpl currentEvent;
	private GmTraversingEventImpl nextEvent;
	private GmTraversingEventImpl lastEvent;
	private final GmTraversingVisitor[] visitors;
	private int currentDepth;
	private boolean skipAll;
	private int visitorIndex;
	private final Map<GenericEntity, ReferenceCounter> referenceCountings = new HashMap<GenericEntity, ReferenceCounter>();
	private ReferenceCounter lastCounter = null;
	private SkipUseCase currentSkipUseCase;
	private boolean firstSkipUseCaseSet;

	private void setSkipUseCase(SkipUseCase skipUseCase) {
		if (!isFirstSkipUseCaseSet()) {
			this.currentSkipUseCase = skipUseCase;
			setFirstSkipUseCaseSet(true);
		}
	}

	private void resetEventIteration() {
		this.currentSkipUseCase = null;
		setFirstSkipUseCaseSet(false);
		nextEvent = null;
	}

	public TraversingWorker(GmTraversingVisitor[] visitors, Object value) {
		super();
		this.visitors = visitors;

		GenericModelType valueType = GMF.getTypeReflection().getBaseType().getActualType(value);

		TraversingRootModelPathElement rootElement = new TraversingRootModelPathElement(null, value, valueType);

		GmTraversingEnterEventImpl enterEvent = new GmTraversingEnterEventImpl(rootElement, null);
		GmTraversingLeaveEventImpl leaveEvent = enterEvent.getLeaveEvent();

		currentEvent = enterEvent;
		lastEvent = leaveEvent;
	}

	@Override
	public GmTraversingEnterEvent appendEventPair(GmTraversingEvent predecessor, TraversingModelPathElement pathElement) {
		GmTraversingEventImpl prev = (GmTraversingEventImpl) predecessor;

		if (prev == null) {
			prev = lastEvent;
		}

		GmTraversingEnterEventImpl enterEvent = new GmTraversingEnterEventImpl(pathElement, currentEvent);
		GmTraversingLeaveEventImpl leaveEvent = enterEvent.getLeaveEvent();

		// link in the new element pair
		leaveEvent.next = prev.next;
		prev.next = enterEvent;

		if (prev == lastEvent) {
			lastEvent = leaveEvent;
		}

		return enterEvent;
	}

	@Override
	public void skipAll(SkipUseCase skipUseCase) {
		skipWalkFrame(skipUseCase);
		skipAll = true;
	}

	@Override
	public void skipWalkFrame(SkipUseCase skipUseCase) {
		if (currentEvent.isEnter()) {
			GmTraversingEnterEventImpl enterEvent = (GmTraversingEnterEventImpl) currentEvent;
			nextEvent = enterEvent.getLeaveEvent();
		}
		setSkipUseCase(skipUseCase);
	}

	@Override
	public void skipDescendants(SkipUseCase skipUseCase) {
		currentEvent.setSkipDescendants(true);
		setSkipUseCase(skipUseCase);
	}

	//TODO this method returns the current skipUseCase regardless of the skip type, verify that this is correct
	@Override
	public SkipUseCase getSkipUseCase() {
		return currentSkipUseCase;
	}

	@Override
	public void abort() {
		currentEvent.next = null;
		lastEvent = null;
	}

	@Override
	public int getCurrentDepth() {
		return currentDepth;
	}

	@Override
	public <T> T getVisitorSpecificCustomValue(IModelPathElement pathElement) {
		Object customValues[] = ((TraversingModelPathElement) pathElement).getCustomValue();

		return (T) (customValues != null ? customValues[visitorIndex] : null);
	}

	@Override
	public void setVisitorSpecificCustomValue(IModelPathElement pathElement, Object value) {
		Object customValues[] = ((TraversingModelPathElement) pathElement).getCustomValue();
		if (customValues == null) {
			customValues = new Object[visitors.length];
			((TraversingModelPathElement) pathElement).setCustomValue(customValues);
		}

		customValues[visitorIndex] = value;
	}

	@Override
	public <T> T getSharedCustomValue(IModelPathElement pathElement) {
		return (T) ((TraversingModelPathElement) pathElement).getSharedCustomValue();
	}

	@Override
	public void setSharedCustomValue(IModelPathElement pathElement, Object value) {
		((TraversingModelPathElement) pathElement).setSharedCustomValue(value);
	}

	@Override
	public <T extends GmTraversingEvent> T getEvent() {
		return (T) currentEvent;
	}

	public void run() throws GmTraversingException {
		while (currentEvent != null) {
			resetEventIteration();
			if (currentEvent.isSkippedByParent()) {
				currentEvent.setSkipDescendants(true);
			} else { // if currentEvent is not skipped
				if (currentEvent.isEnter()) {
					if (skipAll) {
						nextEvent = ((GmTraversingEnterEventImpl) currentEvent).getLeaveEvent().next;
					} else {
						for (GmTraversingVisitor visitor : visitors) {
							visitor.onElementEnter(this, currentEvent.getPathElement());
						}
						currentDepth++;
					}
				} else { // if current Event is a Leave event
					for (GmTraversingVisitor visitor : visitors) {
						visitor.onElementLeave(this, currentEvent.getPathElement());
					}
					currentDepth--;
				}
			}

			currentEvent.parent = null;
			// TODO simplify the if condition
			currentEvent = (nextEvent == null) ? (!currentEvent.isSkippedByParent() && currentEvent.isEnter()
					&& skipAll ? nextEvent : currentEvent.next) : nextEvent;
		}
	}

	protected void incrementReferenceCounter(GenericEntity entity) {
		ReferenceCounter counter = lastCounter;

		if (counter == null)
			counter = new ReferenceCounter();

		counter.count = 1;

		lastCounter = referenceCountings.put(entity, counter);
		if (lastCounter != null)
			counter.count += lastCounter.count;
	}

	private boolean isFirstSkipUseCaseSet() {
		return firstSkipUseCaseSet;
	}

	private void setFirstSkipUseCaseSet(boolean firstSkipUseCaseSet) {
		this.firstSkipUseCaseSet = firstSkipUseCaseSet;
	}

	private static class ReferenceCounter {
		public int count;
	}
}
