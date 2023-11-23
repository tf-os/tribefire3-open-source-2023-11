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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EnhancableCustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Bidirectional;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

/*
 * Please keep this immutable!
 */
/*
 * Right now this processor checks every single time whether given types are supported for the "bidirectional property"
 * relation. This is some place for optimization.
 */
public class BidiPropertyStateChangeProcessor implements StateChangeProcessor<GenericEntity, BidiPropertyStateChangeProcessor.CustomContext>,
		StateChangeProcessorRule, StateChangeProcessorMatch {

	// ###################################
	// ## . . . . . . Rule . . . . . . .##
	// ###################################

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (context.isForProperty()) {
			CmdResolver cmdResolver = context.getCmdResolver();

			// just for sure if something was misconfigured
			if (cmdResolver != null && getBidiProperty(cmdResolver, context.getEntityProperty()) != null)
				return Collections.<StateChangeProcessorMatch> singletonList(this);
		}

		return Collections.emptyList();
	}

	@Override
	public String getRuleId() {
		return getProcessorId();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor(String processorId) {
		return this;
	}

	// ###################################
	// ## . . . . . . Match . . . . . . ##
	// ###################################

	@Override
	public String getProcessorId() {
		return getClass().getName();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor() {
		return this;
	}

	// ###################################
	// ## . . . . . Processor . . . . . ##
	// ###################################

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.capabilities(true, true, false);
	}

	// **************************************************************************************
	// . . . . . . . . . . . . . . . . BEFORE . . . . . . . . . . . . . . . . . . . . . . . .
	// **************************************************************************************

	@Override
	public CustomContext onBeforeStateChange(BeforeStateChangeContext<GenericEntity> beforeContext) throws StateChangeProcessorException {
		BidiContext context = new BidiContext(beforeContext);

		if (context.getLinkedGmProperty() == null)
			return null;

		return addBeforeInfo(context);
	}

	private CustomContext addBeforeInfo(BidiContext beforeContext) {
		CustomContext context = CustomContext.T.create();

		context.setValuesToAdd(findValuesToAdd(beforeContext));
		context.setValuesToRemove(findValuesToRemove(beforeContext));

		ensureAddsRemovesAreDisjoint(context, beforeContext);

		return context;
	}

	private void ensureAddsRemovesAreDisjoint(CustomContext context, BidiContext beforeContext) {
		// They can have something in common only iff manipulation is CVM
		if (beforeContext.manipulationType() == ManipulationType.CHANGE_VALUE)
			removeFromRemovesIfAlsoInAdds(context.getValuesToAdd(), context.getValuesToRemove());
	}

	/**
	 * In case we are assigning the same value to an entity, we do not want to also do the unlinking, thus we make sure
	 * to ignore any removes which we are already adding.
	 */
	private void removeFromRemovesIfAlsoInAdds(Set<EntityReference> adds, Set<EntityReference> removes) {
		Set<EntityReference> _adds = CodingSet.create(EntRefHashingComparator.INSTANCE);
		_adds.addAll(adds);

		Iterator<EntityReference> it = removes.iterator();
		while (it.hasNext())
			if (_adds.contains(it.next()))
				it.remove();
	}

	private Set<EntityReference> findValuesToAdd(BidiContext context) {
		switch (context.manipulationType()) {
			case ADD:
				AddManipulation am = context.manipulation();
				return entityValueAsSet(am.getItemsToAdd().values(), false);
			case REMOVE:
				return Collections.emptySet();
			case CHANGE_VALUE:
				ChangeValueManipulation cvm = context.manipulation();
				return entityValueAsSet(cvm.getNewValue(), false);
			default:
				throw new RuntimeException("Unexpected manipulation: " + context.manipulation());
		}
	}

	private Set<EntityReference> findValuesToRemove(BidiContext context) {
		switch (context.manipulationType()) {
			case ADD:
				return Collections.emptySet();
			case REMOVE:
				RemoveManipulation rm = context.manipulation();
				return valueForBulkRemove(context, rm.getItemsToRemove().keySet());
			case CHANGE_VALUE:
				Object value = context.getTargetValueIfPossible();
				return entityValueAsSet(value, true);
			default:
				throw new RuntimeException("Unexpected manipulation: " + context.manipulation());
		}
	}

	private Set<EntityReference> valueForBulkRemove(BidiContext context, Set<?> valuesOrIndices) {
		Set<EntityReference> result = newSet();

		if (context.isTargetPropertyList()) {
			Object entityValue = context.getTargetValueIfPossible();

			if (entityValue != null) {
				List<?> list = (List<?>) entityValue;

				for (Integer i : (Set<Integer>) valuesOrIndices) 
					result.add(getEntity(list.get(i), true));
			}

		} else {
			result.addAll((Set<EntityReference>) valuesOrIndices);
		}

		return result;
	}

	private <T extends GenericEntity> Set<T> entityValueAsSet(Object value, boolean asReference) {
		if (value == null)
			return Collections.emptySet();

		if (value instanceof Collection) {
			Set<T> result = newSet();
			for (Object o : (Collection<?>) value)
				result.add(getEntity(o, asReference));

			return result;
		}

		if (value instanceof GenericEntity)
			return asSet(getEntity(value, asReference));

		throw new RuntimeException("Unsupported type of value: " + value);
	}

	private <T extends GenericEntity> T getEntity(Object value, boolean asReference) {
		GenericEntity ge = (GenericEntity) value;

		if (!asReference) {
			return (T) ge;
		}

		return ge.reference();
	}

	// *************************************************************************************
	// . . . . . . . . . . . . . . . . AFTER . . . . . . . . . . . . . . . . . . . . . . . .
	// *************************************************************************************

	public static interface CustomContext extends GenericEntity {

		EntityType<CustomContext> T = EntityTypes.T(CustomContext.class);

		Set<EntityReference> getValuesToRemove();
		void setValuesToRemove(Set<EntityReference> valuesToRemove);

		Set<EntityReference> getValuesToAdd();
		void setValuesToAdd(Set<EntityReference> valuesToAdd);

	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> afterContext, CustomContext cc) throws StateChangeProcessorException {
		if (cc == null)
			return;

		BidiContext context = new BidiContext(afterContext);
		context.valuesToAdd = cc.getValuesToAdd();
		context.valuesToRemove = cc.getValuesToRemove();

		if (context.getLinkedGmProperty() == null)
			return;

		handleLinking(context);

		commit(afterContext);
	}

	private void commit(AfterStateChangeContext<GenericEntity> afterContext) throws StateChangeProcessorException {
		try {
			if (afterContext.wasSystemSessionModified())
				afterContext.getSystemSession().commit();

		} catch (GmSessionException e) {
			throw new StateChangeProcessorException("Session commit failed", e);
		}
	}

	private void handleLinking(BidiContext context) throws StateChangeProcessorException {
		for (GenericEntity other : context.valuesToRemove)
			link(context, context.resolveEntity(other), false);

		for (GenericEntity other : context.valuesToAdd)
			link(context, context.resolveEntity(other), true);
	}

	private void link(BidiContext context, GenericEntity other, boolean shouldLink) {
		GenericEntity entity = context.entity();
		Property otherProperty = context.getLinkedProperty();
		link(entity, other, context.getTargetProperty(), otherProperty, shouldLink);
	}

	private void link(GenericEntity entity, GenericEntity other, Property property, Property otherProperty, boolean shouldLink) {
		/* TODO optimize - we don't need the value in case we are removing from Set or adding to set, which is also the
		 * most dangerous situation */
		Object linkedValue = otherProperty.get(other);

		Set<GenericEntity> linkedForOthers = entityValueAsSet(linkedValue, false);

		if (linkedForOthers.contains(entity) == shouldLink)
			return;

		// add it there if we are linking or remove if we are unlinking
		if (shouldLink) {
			if (otherProperty.getType() instanceof CollectionType) {
				((Collection<Object>) linkedValue).add(entity);

			} else {
				// being here means otherProperty has EntityType -> linkedValue can be cast to GenericEntity
				if (linkedValue != entity) {
					/* NOTE the code below (commented out) would be important if the commit we do at the end did not
					 * again trigger the BidiPropertyStateChangeProcessor. If it however triggers it, it can be
					 * commented out. I am leaving it here just in case something changes (again). */

					// if (linkedValue != null) {
					// if value to be overwritten is set to something else (currentValueOfOther), we unlink that too
					// link(other, (GenericEntity) linkedValue, otherProperty, property, false);
					// }
					otherProperty.set(other, entity);
				}
			}

		} else {
			if (property.getType().getTypeCode() == TypeCode.listType) {
				/* In case we are removing from a list, we want to check whether we really removed the last instance of
				 * "other" from the list, before we really unlink it. The "other" might still be in, if it was more than
				 * once in the list, or if it was removed but later added, but at a different position. */

				/* NOTE, this can be easily optimized memory-wise, no need to load entire list, we might just run a
				 * query to check if given entity is within the list. */
				Object editedValue = property.get(entity);
				Set<GenericEntity> allEntitiesInEditedValue = entityValueAsSet(editedValue, false);

				if (allEntitiesInEditedValue.contains(other))
					return;
			}

			if (linkedValue instanceof Collection<?>) {
				if (linkedValue instanceof List<?>) {
					Iterator<?> it = ((List<?>) linkedValue).iterator();

					while (it.hasNext())
						if (it.next() == entity)
							it.remove();

				} else {
					((Set<?>) linkedValue).remove(entity);
				}
			} else {
				otherProperty.set(other, null);
			}
		}
	}

	private static class BidiContext extends ExtendedStateChangeContext<GenericEntity> {
		private GmProperty linkedProperty;
		public Set<EntityReference> valuesToRemove;
		public Set<EntityReference> valuesToAdd;

		public BidiContext(StateChangeContext<GenericEntity> ctx) throws StateChangeProcessorException {
			super(ctx);
		}

		public GmProperty getLinkedGmProperty() {
			if (linkedProperty == null)
				linkedProperty = BidiPropertyStateChangeProcessor.getLinkedProperty(context, getTargetProperty().getType());

			return linkedProperty;
		}

		public Property getLinkedProperty() {
			EntityType<GenericEntity> et = GMF.getTypeReflection().getEntityType(getLinkedGmProperty().getDeclaringType().getTypeSignature());
			return et.findProperty(getLinkedGmProperty().getName());
		}
	}

	static GmProperty getLinkedProperty(StateChangeContext<GenericEntity> context, GenericModelType targetPropertyType) {
		CmdResolver cmdResolver = context.getCmdResolver();
		EntityProperty entityProperty = context.getEntityProperty();

		Bidirectional bp = getBidiProperty(cmdResolver, entityProperty);

		if (bp == null)
			return null;

		GmType linkedPropertyType = bp.getLinkedProperty().getType();

		if (isUnsupportedType(linkedPropertyType))
			throw new RuntimeException("Unsupported linkedProperty type '" + linkedPropertyType.getTypeSignature() + "' for: "
					+ entityProperty.getReference().getTypeSignature() + "#" + entityProperty.getPropertyName());

		if (isUnsupportedTargetPropertyType(targetPropertyType))
			throw new RuntimeException("Unsupported type to have linkedProperty '" + targetPropertyType.getTypeSignature() + "'. Property: "
					+ entityProperty.getReference().getTypeSignature() + "#" + entityProperty.getPropertyName());

		return bp.getLinkedProperty();
	}

	static boolean isUnsupportedType(GmType type) {
		if (type instanceof GmSimpleType || type instanceof GmMapType || type instanceof GmEnumType)
			return true;

		if (type instanceof GmLinearCollectionType) {
			GmType paramType = ((GmLinearCollectionType) type).getElementType();
			if (!(paramType instanceof GmEntityType))
				return true;
		}

		return false;
	}

	private static boolean isUnsupportedTargetPropertyType(GenericModelType type) {
		if (!(type instanceof EnhancableCustomType))
			return true;

		if (type instanceof CollectionType) {
			CollectionType ct = (CollectionType) type;
			return (ct.getCollectionKind() == CollectionKind.map) || !(ct.getCollectionElementType() instanceof EntityType);
		}

		return false;
	}

	static Bidirectional getBidiProperty(CmdResolver cmdResolver, EntityProperty entityProperty) {
		String ts = entityProperty.getReference().getTypeSignature();
		String propName = entityProperty.getPropertyName();

		return cmdResolver.getMetaData().entityTypeSignature(ts).property(propName).meta(Bidirectional.T).exclusive();
	}

}
