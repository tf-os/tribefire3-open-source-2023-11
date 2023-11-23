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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpException;

/**
 * A {@link SimpleTypeImp} specialized in {@link GmEntityType}
 */
public class EntityTypeImp extends SimpleTypeImp<GmEntityType> {

	private final ModelImpCave models;

	EntityTypeImp(PersistenceGmSession session, GmEntityType gmEntityType) {
		super(session, gmEntityType);
		models = new ModelImpCave(session());
	}

	/**
	 * removes the property with the provided name from the type managed by this imp
	 */
	public EntityTypeImp removeProperty(String propertyName) {
		instance.getProperties().removeIf(p -> p.getName().equals(propertyName));

		return this;
	}

	/**
	 * removes the passed property from the type managed by this imp
	 */
	public EntityTypeImp removeProperty(GmProperty property) {
		instance.getProperties().removeIf(property::equals);

		return this;
	}

	/**
	 * adds the passed property to the type managed by this imp
	 */
	public EntityTypeImp addProperty(GmProperty property) {
		instance.getProperties().add(property);
		return this;
	}

	/**
	 * creates a new property with specified name and GmType and adds it to the type managed by this imp
	 */
	public EntityTypeImp addProperty(String name, GmType valueType) {
		logger.trace("creating property " + name + " with type " + valueType);

		GmProperty property = session().create(GmProperty.T);
		property.setName(name);
		property.setType(valueType);
		property.setDeclaringType(instance);

		return addProperty(property);
	}

	/**
	 * creates a new list property with specified element type and adds it to the type managed by this imp
	 */
	public EntityTypeImp addListProperty(String name, GmType elementType) {
		String typeSignature = "list<" + elementType.getTypeSignature() + ">";

		GmLinearCollectionType collectionType = (GmLinearCollectionType) models.type().find(typeSignature).orElse(null);

		if (collectionType == null) {
			collectionType = session().create(GmListType.T);
			collectionType.setElementType(elementType);
			collectionType.setTypeSignature(typeSignature);
		}

		return addProperty(name, collectionType);
	}

	/**
	 * creates a new list property with specified element type and adds it to the type managed by this imp
	 */
	public EntityTypeImp addSetProperty(String name, GmType elementType) {
		String typeSignature = "set<" + elementType.getTypeSignature() + ">";

		GmLinearCollectionType collectionType = (GmLinearCollectionType) models.type().find(typeSignature).orElse(null);

		if (collectionType == null) {
			collectionType = session().create(GmSetType.T);
			collectionType.setElementType(elementType);
			collectionType.setTypeSignature(typeSignature);
		}

		return addProperty(name, collectionType);
	}

	/**
	 * creates a new map property with specified key- and value type and adds it to the type managed by this imp
	 */
	public EntityTypeImp addMapProperty(String name, GmType keyType, GmType valueType) {
		String typeSignature = "map<" + keyType.getTypeSignature() + "," + valueType.getTypeSignature() + ">";

		GmMapType collectionType = (GmMapType) models.type().find(typeSignature).orElseGet(() -> {

			GmMapType mapType = session().create(GmMapType.T);
			mapType.setKeyType(keyType);
			mapType.setValueType(valueType);
			mapType.setTypeSignature(typeSignature);
		
			return mapType;
		});

		return addProperty(name, collectionType);
	}

	/**
	 * creates a new list property with specified element type and adds it to the type managed by this imp
	 * <p>
	 * i.e. addListProperty("users", User.T)
	 */
	public EntityTypeImp addListProperty(String name, EntityType<?> elementType) {
		GmType gmTypeOfElementType = new ModelImpCave(session()).entityType(elementType).get();

		return addListProperty(name, gmTypeOfElementType);
	}

	/**
	 * creates a new set property with specified element type and adds it to the type managed by this imp
	 * <p>
	 * i.e. addSetProperty("users", User.T)
	 */
	public EntityTypeImp addSetProperty(String name, EntityType<?> elementType) {
		GmType gmTypeOfElementType = models.entityType(elementType).get();

		return addSetProperty(name, gmTypeOfElementType);
	}

	/**
	 * creates a new map property with specified element type and adds it to the type managed by this imp
	 * <p>
	 * i.e. addMapProperty("users", SimpleType.TYPE_STRING, User.T)
	 */
	public EntityTypeImp addMapProperty(String name, EntityType<?> keyType, EntityType<?> valueType) {
		GmType gmTypeOfKeyType = models.entityType(keyType).get();
		GmType gmTypeOfValueType = models.entityType(valueType).get();
		return addMapProperty(name, gmTypeOfKeyType, gmTypeOfValueType);
	}

	/**
	 * creates a new property with specified name and type and adds it to the type managed by this imp
	 */
	public EntityTypeImp addProperty(String name, GenericModelType type) {
		return addProperty(name, type.getTypeSignature());
	}

	/**
	 * creates a new property with specified name and type and adds it to the type managed by this imp
	 */
	private EntityTypeImp addProperty(String name, String typeSignature) {
		logger.trace("Looking for GmType with ts " + typeSignature);

		GmType foundType = queryHelper.entityWithProperty(GmType.T, "typeSignature", typeSignature);

		return addProperty(name, foundType);
	}

	/**
	 * lets you set if an entity type is abstract or not
	 */
	public EntityTypeImp setAbstract(Boolean isAbstract) {
		instance.setIsAbstract(isAbstract);
		return this;
	}

	/**
	 * @param superEntity
	 *            the entity type this imp's entity type should inherit from
	 */
	public EntityTypeImp addInheritance(GmEntityType superEntity) {
		logger.trace("Adding inheritance [" + instance.getTypeSignature() + "] is subtype of [" + superEntity.getTypeSignature() + "]");
		instance.getSuperTypes().add(superEntity);
		return this;
	}

	/**
	 * @param superEntity
	 *            the entity type this imp's entity type should inherit from
	 */
	public EntityTypeImp addInheritance(EntityType<?> superEntity) {
		// @formatter:off
		GmEntityType foundType = new EntityTypeImpCave(session())
				.find(superEntity.getTypeSignature())
				.orElseThrow(() -> new ImpException("Could not add entity type inheritance to entity " + instance + ": Could not find type: " + superEntity));
		// @formatter:on

		return addInheritance(foundType);
	}

	public GmProperty getProperty(String name) {
		//@formatter:off
		return instance.getProperties().stream()
				.filter(p -> p.getName().equals(name))
				.findAny()
				.orElse(null);
		//@formatter:on
	}
}
