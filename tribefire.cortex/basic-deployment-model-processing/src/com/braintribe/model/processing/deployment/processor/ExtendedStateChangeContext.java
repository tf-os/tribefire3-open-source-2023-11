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
package com.braintribe.model.processing.deployment.processor;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.api.ManipulationExpositionContext;
import com.braintribe.model.processing.manipulation.basic.BasicMutableManipulationContext;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.sp.api.PostStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;

/**
 * Wrapper for {@link StateChangeContext} providing some extended functionality. This is a convenience class only (i.e.
 * the state change framework does not make any special use of that, but a {@link StateChangeProcessor} might use it to
 * easier extract information from the aforementioned standard context.).
 */
public class ExtendedStateChangeContext<T extends GenericEntity> {
	protected StateChangeContext<T> context;
	private ManipulationExpositionContext manipulationContext;
	private final GenericEntity entity;
	private final Map<EntityReference, PersistentEntityReference> referenceMap;

	public ExtendedStateChangeContext(StateChangeContext<T> ctx) throws StateChangeProcessorException {
		this.context = ctx;
		this.entity = findEntity();
		this.referenceMap = ctx instanceof PostStateChangeContext ? ((PostStateChangeContext<?>) ctx).getReferenceMap() : null;
	}

	private GenericEntity findEntity() throws StateChangeProcessorException {
		try {
			EntityReference reference = context.getEntityProperty().getReference();
			return tryResolveForReference(reference);

		} catch (GmSessionException e) {
			throw new StateChangeProcessorException("Error occured while initializing bidi-property processing.", e);
		}
	}

	protected final ManipulationExpositionContext manipulationContext() {
		if (manipulationContext == null) {
			PropertyManipulation manipulation = context.getManipulation();

			BasicMutableManipulationContext mmc = new BasicMutableManipulationContext();
			mmc.setCurrentManipulationSafe(manipulation);

			manipulationContext = mmc;
		}

		return manipulationContext;
	}

	public StateChangeContext<T> getStateChangeContext() {
		return context;
	}

	public CmdResolver getCmdResolver() {
		return context.getCmdResolver();
	}

	public EntityType<?> getEntityType() {
		return context.getEntityType();
	}

	public String getTargetPropertyName() {
		return context.getEntityProperty().getPropertyName();
	}

	public EntityReference getTargetReference() {
		return manipulationContext().getTargetReference();
	}

	public EntityReference getNormalizedTargetReference() {
		return manipulationContext().getNormalizedTargetReference();
	}

	public Property getTargetProperty() {
		return manipulationContext().getTargetProperty();
	}

	public boolean isTargetPropertyList() {
		GenericModelType pt = getTargetProperty().getType();
		return pt instanceof CollectionType && ((CollectionType) pt).getCollectionKind() == CollectionKind.list;
	}

	public Object getTargetValueIfPossible() {
		return entity() == null ? null : getTargetProperty().get(entity());
	}

	public GenericEntity entity() {
		return entity;
	}

	public ManipulationType manipulationType() {
		return manipulationContext().getCurrentManipulationType();
	}

	public <M extends AtomicManipulation> M manipulation() {
		return manipulationContext().getCurrentManipulation();
	}

	public GenericEntity resolveEntity(GenericEntity ge) throws StateChangeProcessorException {
		if (ge instanceof EntityReference) {
			return resolveEntityForReference((EntityReference) ge);

		} else {
			return ge;
		}
	}

	@SuppressWarnings("unchecked")
	protected final <E extends GenericEntity> E resolveEntityForReference(EntityReference reference) throws StateChangeProcessorException {
		PersistentEntityReference ref = resolveReference(reference);
		try {
			return (E) tryResolveForReference(ref);
		} catch (Exception e) {
			throw new StateChangeProcessorException("Unable to retrieve instance for: " + ref, e);
		}
	}

	private GenericEntity tryResolveForReference(EntityReference reference) throws GmSessionException {
		if (reference instanceof PreliminaryEntityReference) {
			return null;
		}

		/* TODO optimize to findLocalOrBuildShallow once we make sure the manipulation stack can only have preliminary
		 * references for preliminary entities */
		return context.getSystemSession().query().entity(reference).find();
	}

	protected final PersistentEntityReference resolveReference(EntityReference reference) {
		return reference instanceof PersistentEntityReference ? (PersistentEntityReference) reference : referenceMap.get(reference);
	}
}
