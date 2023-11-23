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
package com.braintribe.model.access.security.manipulation.experts;

import static com.braintribe.model.generic.manipulation.DeleteMode.failIfReferenced;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.AclEntry;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpositionContext;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.security.acl.AclTools;

/**
 * {@link ManipulationSecurityExpert} for ACL.
 * 
 * @see HasAcl
 * @see Acl
 */
public class AclManipulationSecurityExpert implements ManipulationSecurityExpert {

	@Override
	public Object createExpertContext(ManipulationSecurityContext context) {
		return null;
	}

	@Override
	public void expose(ManipulationSecurityExpositionContext context) {
		if (context.getTargetReference() == null)
			return;

		new ValidationImpl(context).validate();
	}

	private class ValidationImpl {

		private static final String ENTITY_ACCESS_DENIED = "Entity access denied!";
		private static final String PROPERTY_ACCESS_DENIED = "Property access denied";

		private final ManipulationSecurityExpositionContext context;
		private final SessionAuthorization sa;

		private GenericEntity owner;
		private String source;
		private boolean relatedToProperty;

		public ValidationImpl(ManipulationSecurityExpositionContext context) {
			this.context = context;
			this.sa = context.getSession().getSessionAuthorization();
		}

		public void validate() {
			checkManipulationOwner();

			if (isPropertyManipulation())
				checkPropertyValues();
		}

		private boolean isPropertyManipulation() {
			return context.getCurrentManipulation() instanceof PropertyManipulation;
		}

		private void checkManipulationOwner() {
			markCheckingManipulationOwner();

			checkModeIfDelete();

			if (owner instanceof Acl)
				checkAcl((Acl) owner, ENTITY_ACCESS_DENIED);

			if (owner instanceof HasAcl)
				checkHasAcl((HasAcl) owner, ENTITY_ACCESS_DENIED, false);
		}

		private void checkModeIfDelete() {
			if (context.getCurrentManipulationType() != ManipulationType.DELETE)
				return;

			if (owner instanceof Acl || owner instanceof AclEntry) {
				DeleteManipulation dm = context.getCurrentManipulation();
				DeleteMode mode = dm.getDeleteMode();

				if (mode != failIfReferenced)
					addViolationEntry(
							owner.entityType().getShortName() + " can only be deleted using '" + failIfReferenced + "' mode, not '" + mode + "'.");
			}
		}

		private void checkAcl(Acl acl, String description) {
			if (AclTools.isAclAdministrable(context.getSession()))
				return;

			if (acl != null && !acl.isOperationGranted(AclOperation.MODIFY_ACL, sa.getUserRoles()))
				addViolationEntry(description);
		}

		private void checkPropertyValues() {
			markCheckingProperty();

			if (isHasAcl()) {
				if (isAclProperty())
					validateAclProperty();
				else if (isOwnerProperty())
					validateOwnerProperty();
			}

			validateMaybeHasAclProperty();
		}

		private void markCheckingManipulationOwner() {
			owner = context.getTargetInstance();
			relatedToProperty = false;
			source = context.getTargetSignature();
		}

		private void markCheckingProperty() {
			relatedToProperty = true;
			source = context.getTargetSignature() + "#" + context.getTargetPropertyName();
		}

		private boolean isHasAcl() {
			return context.getTargetInstance() instanceof HasAcl;
		}

		private boolean isAclProperty() {
			return HasAcl.acl.equals(context.getTargetPropertyName());
		}

		private boolean isOwnerProperty() {
			return HasAcl.owner.equals(context.getTargetPropertyName());
		}

		private void validateAclProperty() {
			if (AclTools.isHasAclAdministrable(context.getSession()))
				return;

			// The case "owner not matching, acl = null" doesn't have to be handled, already covered in checkOwner

			HasAcl hasAcl = context.getTargetInstance();

			if (isCurrentUserOwnerOf(hasAcl))
				return;

			Acl acl = hasAcl.getAcl();
			if (acl != null && !canReplace(acl))
				addViolationEntry(PROPERTY_ACCESS_DENIED);
		}

		private boolean canReplace(Acl acl) {
			return acl.isOperationGranted(AclOperation.REPLACE_ACL, sa.getUserRoles()) //
					|| acl.isOperationGranted(AclOperation.MODIFY_ACL, sa.getUserRoles());
		}

		private void validateOwnerProperty() {
			if (AclTools.isHasAclAdministrable(context.getSession()))
				return;

			HasAcl hasAcl = context.getTargetInstance();
			if (isCurrentUserOwnerOf(hasAcl))
				return;

			addViolationEntry(PROPERTY_ACCESS_DENIED);
		}

		private boolean isCurrentUserOwnerOf(HasAcl hasAcl) {
			String owner = hasAcl.getOwner();
			return owner != null && owner.equals(sa.getUserName());
		}

		private void validateMaybeHasAclProperty() {
			ManipulatedValues manipulatedValues = new ManipulatedValues(context);

			checkForProperty(manipulatedValues.adds, "Cannot add reference to an entity (access denied).");
			checkForProperty(manipulatedValues.directRemoves, "Cannot remove reference to an entity (access denied).");
			checkForProperty(manipulatedValues.mapValueRemoves, "Cannot remove map entry, access to the mapped value is denied.");
		}

		private void checkForProperty(Acls acls, String description) {
			for (HasAcl entity : acls.entities)
				checkHasAcl(entity, description, true);
		}

		private void checkHasAcl(HasAcl entity, String description, boolean read) {
			if (AclTools.isHasAclAdministrable(context.getSession()))
				return;

			if (!passesAclConditions(entity, read))
				addViolationEntry(description);
		}

		private boolean passesAclConditions(HasAcl entity, boolean read) {
			return entity.isOperationGranted(relevantAclOperation(read), sa.getUserName(), sa.getUserRoles());
		}

		private AclOperation relevantAclOperation(boolean read) {
			if (read)
				return AclOperation.READ;

			if (context.getCurrentManipulation().manipulationType() == ManipulationType.DELETE)
				return AclOperation.DELETE;
			else
				return AclOperation.WRITE;
		}

		private void addViolationEntry(String description) {
			SecurityViolationEntry validationEntry = SecurityViolationEntry.T.create();
			validationEntry.setEntityReference(context.getTargetReference());
			validationEntry.setCausingManipulation(context.getCurrentManipulation());
			validationEntry.setDescription("[ACL]" + source + ": " + description);
			if (relatedToProperty)
				validationEntry.setPropertyName(context.getTargetPropertyName());

			context.addViolationEntry(validationEntry);
		}

	}

	private static class ManipulatedValues {
		private ManipulationSecurityExpositionContext context;
		private PropertyManipulation manipulation;
		private GenericModelType propertyType;

		Acls adds = new Acls();
		Acls directRemoves = new Acls();
		Acls mapValueRemoves = new Acls();

		ManipulatedValues(ManipulationSecurityExpositionContext context) {
			this.context = context;
			this.manipulation = context.getCurrentManipulation();
			this.propertyType = resolvePropertyType();

			AtomicManipulation m = context.getCurrentManipulation();

			switch (m.manipulationType()) {
				case ADD:
					Map<Object, Object> itemsToAdd = ((AddManipulation) m).getItemsToAdd();
					addToCollection(itemsToAdd.values());
					if (isMap())
						addToCollection(itemsToAdd.keySet());
					return;

				case REMOVE:
					Map<Object, Object> itemsToRemove = ((RemoveManipulation) m).getItemsToRemove();
					if (isMap()) {
						removeFromCollection(itemsToRemove.keySet());
						removeValuesForKeys(itemsToRemove.keySet());
					} else {
						removeFromCollection(itemsToRemove.values());
					}
					return;

				case CHANGE_VALUE:
					newValue(((ChangeValueManipulation) m).getNewValue());
					return;

				default:
					return;
			}
		}

		private GenericModelType resolvePropertyType() {
			EntityProperty ep = (EntityProperty) manipulation.getOwner();
			Property p = ep.property();
			if (p == null)
				throw new IllegalArgumentException(
						"Cannot resolve property for manipulation owner: " + ep.getReference().getTypeSignature() + "#" + ep.getPropertyName());

			return p.getType();
		}

		private void newValue(Object newValue) {
			filterEntities(adds, propertyType, newValue);
		}

		private void addToCollection(Collection<?> valuesToAdd) {
			filterEntitiesFromCollection(adds, elementType(), valuesToAdd);
		}

		private void removeFromCollection(Collection<?> valuesToRemove) {
			// for Map the "keyType()" really is they key type, for List/Set the keyType() == valueType()
			filterEntitiesFromCollection(directRemoves, keyType(), valuesToRemove);
		}

		private void removeValuesForKeys(Set<?> keySet) {
			if (elementType() instanceof EntityType || elementType() instanceof BaseType) {
				Set<GenericEntity> resolvedKeys = newSet();
				for (Object key : keySet)
					resolvedKeys.add(resolveEntity(context, key));

				Map<?, ?> map = getActualPropertyValue();
				map = newMap(map);
				map.keySet().retainAll(resolvedKeys);

				/* We use the "resolved" parameter, because the value was retrieved from the instance and not the
				 * manipulation (so it is not a bunch of EntityReferences, but it's normal values) */
				filterEntitiesFromCollection(mapValueRemoves, elementType(), map.values(), true);
			}
		}

		private <T> T getActualPropertyValue() {
			return context.getTargetPropertyValue();
		}

		private void filterEntities(Acls acls, GenericModelType type, Object o) {
			filterEntities(acls, type, o, false);
		}

		private void filterEntities(Acls acls, GenericModelType type, Object o, boolean resolved) {
			if (o instanceof Collection) {
				Collection<?> c = (Collection<?>) o;

				if (type.isCollection())
					filterEntitiesFromCollection(acls, ((CollectionType) type).getCollectionElementType(), c, resolved);
				else
					filterEntitiesFromCollection(acls, BaseType.INSTANCE, c, resolved);

			} else if (o instanceof Map) {
				Map<?, ?> m = (Map<?, ?>) o;

				if (type instanceof CollectionType) {
					CollectionType ct = (CollectionType) type;

					filterEntitiesFromCollection(acls, ct.getCollectionElementType(), m.keySet(), resolved);
					filterEntitiesFromCollection(acls, ct.getCollectionElementType(), m.values(), resolved);

				} else {
					filterEntitiesFromCollection(acls, BaseType.INSTANCE, m.keySet(), resolved);
					filterEntitiesFromCollection(acls, BaseType.INSTANCE, m.values(), resolved);
				}

			} else {
				processNonCollection(acls, o, resolved);
			}
		}

		private void filterEntitiesFromCollection(Acls acls, GenericModelType elementType, Collection<?> c) {
			filterEntities(acls, elementType, c, false);
		}

		private void filterEntitiesFromCollection(Acls acls, GenericModelType elementType, Collection<?> c, boolean resolved) {
			if (elementType.isEntity() || elementType.isBase())
				for (Object o : c)
					processNonCollection(acls, o, resolved);
		}

		private void processNonCollection(Acls acls, Object o, boolean resolved) {
			if (!resolved)
				o = resolveEntity(context, o);

			if (o instanceof HasAcl)
				acls.entities.add((HasAcl) o);
		}

		private boolean isMap() {
			return propertyType.getTypeCode() == TypeCode.mapType;
		}

		private GenericModelType keyType() {
			return propertyType.isBase() ? BaseType.INSTANCE : ((CollectionType) propertyType).getParameterization()[0];
		}

		private GenericModelType elementType() {
			return propertyType.isBase() ? BaseType.INSTANCE : ((CollectionType) propertyType).getCollectionElementType();
		}

	}

	public static GenericEntity resolveEntity(ManipulationSecurityExpositionContext context, Object o) {
		if (o instanceof EntityReference)
			return context.resolveReference((EntityReference) o);
		else
			return null;
	}

	private static class Acls {
		Set<HasAcl> entities = newSet();
	}

	@Override
	public void validate(ManipulationSecurityContext context) {
		return;
	}

}
