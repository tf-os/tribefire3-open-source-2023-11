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
package com.braintribe.product.rat.imp.impl.model;

import java.util.Optional;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.CommonTools;

/**
 * A {@link AbstractCustomTypeImpCave} specialized in {@link GmEntityType}
 */
public class EntityTypeImpCave extends AbstractCustomTypeImpCave<EntityTypeImp, GmEntityType> {

	EntityTypeImpCave(PersistenceGmSession session) {
		super(session, GmEntityType.T);
	}

	protected GmEntityType gmEntityTypeOfGenericEntity() {
		// @formatter:off
		GmEntityType genericEntityType = find(GenericEntity.T)
			.orElseThrow(() -> new RuntimeException("Fatal error: There is no GenericEntity type in your session." +
													" Please make sure your session runs in a fully set up cortex access"));
		// @formatter:on

		return genericEntityType;
	}

	public EntityTypeImp create(EntityType<?> entityType, GmMetaModel declaringModel) {
		return create(entityType.getTypeSignature(), declaringModel);
	}

	@Override
	public EntityTypeImp create(String typeSignature, GmMetaModel declaringModel) {
		EntityTypeImp imp = super.create(typeSignature, declaringModel);

		GmEntityType genericEntityType = gmEntityTypeOfGenericEntity();
		imp.get().setSuperTypes(CommonTools.getList(genericEntityType));

		return imp;
	}

	/**
	 * Creates "empty" EntitTypes that just have their type signature and inherit from GenericEntity and adds them to
	 * the passed metamodel.<br>
	 * use this method in the beginning when you create a new set of entity types. Then you don't need to worry about
	 * the order in which you declare them when they reference each other
	 *
	 * @param groupId
	 *            the group ID all these entities share
	 * @param entityTypeNames
	 *            the entityTypeNames without the group ID
	 * @param model
	 *            the metamodel these entity types should be added to
	 */
	public EntityTypeImpCave createPlainTypes(GmMetaModel model, String groupId, String... entityTypeNames) {
		for (String entityTypeName : entityTypeNames) {
			create(groupId, entityTypeName, model);
		}

		return this;
	}

	/**
	 * Creates "empty" EntitTypes that just have their type signature and inherit from GenericEntity and adds them to
	 * the passed metamodel.<br>
	 * use this method in the beginning when you create a new set of entity types. Then you don't need to worry about
	 * the order in which you declare them when they reference each other
	 *
	 * @param model
	 *            the metamodel these entity types should be added to
	 * @param entityTypes
	 *            the entity types you want to create
	 */
	public EntityTypeImpCave createPlainTypes(GmMetaModel model, EntityType<?>... entityTypes) {
		for (EntityType<?> entityType : entityTypes) {
			create(entityType.getTypeSignature(), model);
		}

		return this;
	}

	/**
	 * Creates an entitytype imp managing provided EntityType
	 *
	 * @param entityType
	 *            the entitytype that should be managed by the imp
	 * @return the newly created imp
	 */
	public EntityTypeImp with(EntityType<?> entityType) {
		// @formatter:off
		GmEntityType foundInstance = find(entityType)
				.orElseThrow(() -> new ImpException("Could not find an instance of GmEntityType with type signature " + entityType.getTypeSignature()));

		// @formatter:on
		return with(foundInstance);
	}

	@Override
	protected EntityTypeImp buildImp(GmEntityType instance) {
		return new EntityTypeImp(session(), instance);
	}

	/**
	 * finds the GmEntityType that belongs to passed EntityType
	 *
	 * @return the {@link GmEntityType} that was found or null
	 */
	public Optional<GmEntityType> find(EntityType<?> entityType) {
		Arguments.notNullWithName("entityType", entityType);
		return find(entityType.getTypeSignature());
	}

}
