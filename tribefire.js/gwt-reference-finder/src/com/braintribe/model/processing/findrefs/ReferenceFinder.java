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
package com.braintribe.model.processing.findrefs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ReferencesCandidate;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;

/**
 * The {@link ReferenceFinder} is used to find references to a specified entity. It requires an {@link IncrementalAccess}
 * to access meta-model and type-reflection and perform entity queries.
 * 
 * 
 * TODO PGA: We should not use entityTypeLookup, but get a ModelOracle Instead.
 */
public class ReferenceFinder {
	private RefereeCandidateFinder referenceCandidateFinder = new RefereeCandidateFinder();
	private RefereeQueryBuilder queryBuilder = new RefereeQueryBuilder();
	private final IncrementalAccess access;

	private Map<String, GmEntityType> entityTypeLookup;

	public ReferenceFinder(IncrementalAccess access) {
		this.access = access;
	}
	
	/**
	 * Finds references to an entity.
	 * 
	 * @param reference
	 *            The reference to the requested entity.
	 * @return A list of {@link EntityProperty}, see the parameter <code>noActualRefereesRequested</code>
	 * @throws ModelAccessException
	 *             In case any error occurs while looking for references.
	 */
	public Set<ReferencesCandidate> findReferences(EntityReference reference) throws ModelAccessException {
		List<CandidateProperty> refereeCandidates = findRefereeCandidates(reference);
		Set<ReferencesCandidate> candidateReferences = findReferencesInCandidates(refereeCandidates, reference);

		/*
		 * TODO: "transformationTest" stuff from original getReferences implementation by Stefan Prieler? (check history
		 * of HibernateAccess in the i2z_ecm project)
		 */
		return candidateReferences;
	}

	private List<CandidateProperty> findRefereeCandidates(EntityReference reference) {
		GmEntityType referencedEntityType = getEntityTypeForReference(reference);
		List<GmEntityType> entityTypesToCheck = getEntityTypesFromMetaModel();
		return referenceCandidateFinder.findRefereeCandidateProperties(referencedEntityType, entityTypesToCheck);
	}

	private GmEntityType getEntityTypeForReference(EntityReference reference) {
		String typeSignature = reference.getTypeSignature();
		GmEntityType referencedEntityType = getEntityTypeLookup().get(typeSignature);
		return referencedEntityType;
	}

	private List<GmEntityType> getEntityTypesFromMetaModel() {
		GmMetaModel metaModel = getMetaModelFromAccess();
		return (List<GmEntityType>) (List<?>) metaModel.getTypes().stream().filter(GmType::isGmEntity).collect(Collectors.toList());
	}

	private Map<String, GmEntityType> getEntityTypeLookup() {
		if (entityTypeLookup == null) {
			initializeEntityTypeLookup();
		}
		return entityTypeLookup;
	}

	private void initializeEntityTypeLookup() {
		Map<String, GmEntityType> lookup = new HashMap<String, GmEntityType>();
		for (GmEntityType entityType : getEntityTypesFromMetaModel()) {
			String typeSignature = entityType.getTypeSignature();
			lookup.put(typeSignature, entityType);
		}
		entityTypeLookup = lookup;
	}

	private GmMetaModel getMetaModelFromAccess() {
		return access.getMetaModel();
	}

	private Set<ReferencesCandidate> findReferencesInCandidates(List<CandidateProperty> candidateProperties,
			EntityReference reference) throws ModelAccessException {

		Set<ReferencesCandidate> allReferingEntityProperties = new HashSet<ReferencesCandidate>();
		for (CandidateProperty candidateProperty : candidateProperties) {
			EntityQuery entityQuery = queryBuilder.buildQuery(candidateProperty, reference);

			EntityQueryResult queryResult = access.queryEntities(entityQuery);
			List<GenericEntity> matchingEntities = queryResult.getEntities();
			if (matchingEntities.isEmpty()) {
				continue;
			}

			ReferencesCandidate referencesCandidate = ReferencesCandidate.T.create();
			referencesCandidate.setProperty(candidateProperty.getPropertyName());
			referencesCandidate.setTypeSignature(candidateProperty.getEntityTypeSignature());
			allReferingEntityProperties.add(referencesCandidate);
		}
		return allReferingEntityProperties;
	}

	/*private List<EntityProperty> createEntityProperties(List<GenericEntity> matchingEntities, CandidateProperty property) {
		List<EntityProperty> referingEntityProperties = new ArrayList<EntityProperty>();
		
		EntityType<GenericEntity> entityType = null;

		for (GenericEntity entity : matchingEntities) {
			if (entityType == null) {
				//only fetching the type once because all matching entities returned by a query must be of the same type
				entityType = typeReflection.getEntityType(entity);
			}
			EntityReference refereeReference = entityType.getReference(entity);

			EntityProperty entityProperty = createEntityProperty(property, refereeReference);
			referingEntityProperties.add(entityProperty);
		}
		return referingEntityProperties;
	}*/

	/*private EntityProperty createEntityProperty(CandidateProperty property, EntityReference reference) {
		EntityProperty entityProperty = new EntityProperty();
		String propertyName = property.getPropertyName();
		entityProperty.setPropertyName(propertyName);
		entityProperty.setReference(reference);
		return entityProperty;
	}*/

	/*private PersistentEntityReference createEntityReferenceWithoutId(String entityTypeSignature) {
		PersistentEntityReference reference = PersistentEntityReference.T.create();
		reference.setId(null);
		//reference.setLocalReference(null);
		reference.setTypeSignature(entityTypeSignature);
		return reference;
	}*/

	/**
	 * Clears the internal lookups that are used for increasing performance
	 */
	public void clearLookups() {
		this.entityTypeLookup = null;
	}

	public void setQueryBuilder(RefereeQueryBuilder queryBuilder) {
		this.queryBuilder = queryBuilder;
	}

	public void setReferenceCandidateFinder(RefereeCandidateFinder referenceCandidateFinder) {
		this.referenceCandidateFinder = referenceCandidateFinder;
	}
}
