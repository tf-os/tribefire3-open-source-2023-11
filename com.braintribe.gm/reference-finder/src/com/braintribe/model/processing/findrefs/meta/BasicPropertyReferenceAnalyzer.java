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
package com.braintribe.model.processing.findrefs.meta;

import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.tools.GmModelTools;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.manipulator.api.PropertyReferenceAnalyzer;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * This class might be extended, e.g. we do that in HibernateAccess to check for properties to ignore based on hibernate
 * meta-data (ignore those not mapped).
 * 
 * The instance must be initialized with the so called root properties before the first use. Root properties are ones
 * that should be considered as possible reference owners (ones that could reference entities and are not
 * {@link #ignoreProperty(GmEntityType, GmProperty)} ignored). This initialization happens by default, but it is
 * possible to delay it from a sub-type implementation with a constructor argument, in order to do some internal
 * initialization first. The sub-type then has to do the initialization - typically by calling the {@link #initialize()}
 * method.
 * 
 * This implementation is thread-safe.
 */
public class BasicPropertyReferenceAnalyzer implements PropertyReferenceAnalyzer {

	protected final ModelOracle modelOracle;
	protected final List<QualifiedProperty> rootProperties;

	protected final Map<GmType, Set<QualifiedProperty>> resolvedRootProperties = newConcurrentMap();

	public BasicPropertyReferenceAnalyzer(ModelOracle modelOracle) {
		this(modelOracle, true);
	}

	public List<QualifiedProperty> getRootProperties() {
		return rootProperties;
	}

	/** This exists so a sub-type might do some internal initialization first and only then trigger the traversing */
	protected BasicPropertyReferenceAnalyzer(ModelOracle modelOracle, boolean initialize) {
		this.modelOracle = modelOracle;
		this.rootProperties = initialize ? findAllRootProperties(modelOracle) : newList();
	}

	/**
	 * Perform the internal initialization - i.e. indexes all the root properties for given {@link ModelOracle}.
	 * 
	 * @throws IllegalStateException
	 *             if this instance was already initialized
	 */
	protected final void initialize() {
		if (!rootProperties.isEmpty())
			throw new IllegalStateException("Cannot initialize this instance, rootProperties already contains some entries: " + rootProperties);

		rootProperties.addAll(findAllRootProperties(modelOracle));
	}

	protected List<QualifiedProperty> findAllRootProperties(ModelOracle modelOracle) {
		List<QualifiedProperty> rootProperties = newList();

		modelOracle.getTypes().onlyEntities().<EntityTypeOracle> asTypeOracles().forEach(entityTypeOracle -> {
			GmEntityType gmEntityType = entityTypeOracle.asGmEntityType();
			if (ignoreEntity(gmEntityType))
				return;

			entityTypeOracle.getProperties() //
					.asGmProperties() //
					.filter(gmProperty -> GmModelTools.areEntitiesReachable(gmProperty.getType())) //
					.filter(gmProperty -> !isIdOrIgnored(gmEntityType, gmProperty)) //
					.map(gmProperty -> MetaModelBuilder.qualifiedProperty(gmEntityType, gmProperty)) //
					.forEach(rootProperties::add);
		});

		return rootProperties;
	}

	/**
	 * @param entityType
	 *            type for which we decide whether or not we want to ignore all of it's properties
	 * 
	 * @see #ignoreProperty(GmEntityType, GmProperty)
	 */
	protected boolean ignoreEntity(GmEntityType entityType) {
		return false;
	}

	private boolean isIdOrIgnored(GmEntityType actualOwner, GmProperty gmProperty) {
		return gmProperty.isId() || ignoreProperty(actualOwner, gmProperty);
	}

	/**
	 * Ignoring a property means making sure the {@link #findReferencingRootPropertiesByType(EntityTypeOracle)} method
	 * (or the "signature" overload) does not return this property.
	 * 
	 * @param actualOwner
	 *            the owner type of the property, cause the property itself might only reference some super-type
	 * 
	 * @param gmProperty
	 *            property for which we decide whether or not we ignore it.
	 */
	protected boolean ignoreProperty(GmEntityType actualOwner, GmProperty gmProperty) {
		return false;
	}

	@Override
	public Set<QualifiedProperty> findReferencingRootProperties(String entitySignature) {
		EntityTypeOracle entityTypeOracle = modelOracle.getEntityTypeOracle(entitySignature);

		return findReferencingRootPropertiesByType(entityTypeOracle);
	}

	private Set<QualifiedProperty> findReferencingRootPropertiesByType(EntityTypeOracle referencedTypeOracle) {
		GmCustomType referencedType = referencedTypeOracle.asGmType();
		return resolvedRootProperties.computeIfAbsent(referencedType, key -> resolve(referencedTypeOracle));
	}

	private Set<QualifiedProperty> resolve(EntityTypeOracle referencedTypeOracle) {
		Set<QualifiedProperty> found = newSet();
		Set<GmType> assignableTypes = getAssignableTypes(referencedTypeOracle);

		for (QualifiedProperty rootProperty : rootProperties)
			if (isTypeReferencing(rootProperty.getProperty().getType(), assignableTypes))
				found.add(rootProperty);

		return found;
	}

	private Set<GmType> getAssignableTypes(EntityTypeOracle referencedTypeOracle) {
		return referencedTypeOracle.getSuperTypes().transitive().includeSelf().includeBaseType().asGmTypes();
	}

	private boolean isTypeReferencing(GmType type, Set<GmType> assignableTypes) {
		if (assignableTypes.contains(type))
			return true;

		switch (type.typeKind()) {
			case LIST:
			case SET:
				GmLinearCollectionType collectionType = (GmLinearCollectionType) type;
				return isTypeReferencing(collectionType.getElementType(), assignableTypes);
			case MAP:
				GmMapType mapType = (GmMapType) type;
				return isTypeReferencing(mapType.getKeyType(), assignableTypes) || isTypeReferencing(mapType.getValueType(), assignableTypes);
			default:
				return false;
		}
	}

}
