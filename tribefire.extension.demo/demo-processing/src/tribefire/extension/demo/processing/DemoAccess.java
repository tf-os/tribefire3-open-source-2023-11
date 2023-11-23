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
package tribefire.extension.demo.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.PageProvider;
import com.braintribe.model.access.PaginatedIterable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;

import tribefire.extension.demo.processing.tools.DemoPopulationBuilder;


/**
 * A demo access implementation that provides access to instances of the DemoModel provided by the
 * {@link DemoPopulationBuilder}. The population is kept in a collection. All changes are applied but are lost after JVM
 * shutdown.
 */
public class DemoAccess extends BasicAccessAdapter implements InitializationAware {

	/** A static number increased by every new created instance of an entity. */
	private static AtomicLong idCounter = new AtomicLong(0);

	/** Build initial population provided by {@link DemoPopulationBuilder} */
	private Collection<GenericEntity> population = new HashSet<>();

	/** The internally used population builder. */
	private final DemoPopulationBuilder populationBuilder = DemoPopulationBuilder.newInstance();

	/** @see #setInitDefaultPopulation(boolean) */
	private boolean initDefaultPopulation = true;

	/** @see #setUseInternalPaging(boolean) */
	private boolean useInternalPaging = true;

	private CmdResolver resolver = null;

	/** If set to true the population builder will be used to fill the population once this class is loaded. */
	@Configurable
	public void setInitDefaultPopulation(boolean initDefaultPopulation) {
		this.initDefaultPopulation = initDefaultPopulation;
	}

	/**
	 * If set to true the {@link #queryPopulation(String)} returns a special {@link Iterable} implementation that
	 * provides the resulting entities in bunches (page) during iteration.
	 */
	@Configurable
	public void setUseInternalPaging(boolean useInternalPaging) {
		this.useInternalPaging = useInternalPaging;
	}

	/**
	 * Will be called by Wire after bean is created (all properties set). It creates the default instances if
	 * {@link #initDefaultPopulation} is set to true.
	 */
	@Override
	public void postConstruct() {
		if (initDefaultPopulation)
			population = populationBuilder.idGenerator(this::buildId).build();
	}

	private Object buildId(GenericEntity entity) {
		GenericModelType idType = getMetaDataResolver().getIdType(entity.entityType().getTypeSignature());

		switch (idType.getTypeCode()) {
			case stringType:
				return UUID.randomUUID().toString();
			default:
				return idCounter.incrementAndGet();
		}
	}

	private CmdResolver getMetaDataResolver() {
		if (resolver == null)
			resolver = new CmdResolverImpl(new BasicModelOracle(getMetaModel()));

		return resolver;
	}

	/**
	 * This method is called by the {@link BasicAccessAdapter} while performing a query. Return the full population for
	 * the given type and let the {@link BasicAccessAdapter} do the rest. <br />
	 * In case {@link #useInternalPaging} is enabled this method returns a {@link PaginatedIterable} with an
	 * {@link PageProvider} that only return 10 entities in a chunk. This should demonstrate how pagination could be
	 * implemented.<br />
	 * Otherwise this method simple returns the population collection identified for given typeSignature.
	 */
	@Override
	protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
		// Acquire the collection of entities identified by the given typeSignature.
		EntityType<GenericEntity> type = typeReflection.getEntityType(typeSignature);
		Set<GenericEntity> typePopulation = collectTypePopulation(type);

		if (useInternalPaging) {
			/* We build a PaginatedIterable and pass an instance of CollectionPageProvider with a pageSize of 10. (The
			 * implementation can be found as nested class on bottom of this class.) The PaginatedIterable basically
			 * calls the passed PageProvider for chunks (pages) of entities during iteration. This can be used for
			 * convenience when implementing dynamic paging behavior (e.g.: dealing with mass data). */
			return new PaginatedIterable<>(new CollectionPageProvider(typePopulation, 10));

		} else {
			// No paging required. Thus we can simply return the full population collection.
			return typePopulation;
		}
	}

	/**
	 * This method is called by the {@link BasicAccessAdapter} when manipulations are committed from the client.<br />
	 * The passed context contains informations about the affected (created, updated and deleted) entities. This method
	 * applies the changes described in the context to the actual entities stored in the population collection.
	 */
	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {
		// Handle new entities
		for (GenericEntity entity : context.getCreatedEntities()) {
			// Creating an id based on the Id-Property type.
			// Only String and Long is supported.
			setIdForEntity(entity);

			// detach the new entity from the persistence session (see BAA.applyLocal(...))
			entity.detach();

			// Add the new entity to the in-memory storage (type population).
			population.add(entity);
		}

		// Handle updated entities by iterating through map of touched properties.
		for (Map.Entry<GenericEntity, Set<Property>> updateEntry : context.getTouchedPropertiesOfEntities().entrySet()) {
			GenericEntity updatedEntity = updateEntry.getKey();
			Set<Property> updatedProperties = updateEntry.getValue();

			// Lookup the entity from the in-memory storage by using the
			// EntityRefernce
			GenericEntity entityFromPopulation = getEntityFromPopulation(updatedEntity);

			// Iterate through list of updated properties and update each of the
			// properties of current entity
			// by using the EntityType.
			for (Property updatedProperty : updatedProperties) {

				// Get the property value by inspecting the updated property.
				Object propertyValue = updatedProperty.get(updatedEntity);

				Object localPropertyValue = convertToLocalValue(propertyValue);

				// Update property using Reflection
				updatedProperty.set(entityFromPopulation, localPropertyValue);
			}
		}

		// Handle deleted entities.
		for (GenericEntity deletedEntity : context.getDeletedEntities()) {

			// Get the actual entity from our population
			GenericEntity entityFromPopulation = getEntityFromPopulation(deletedEntity);

			// Detach all existing references to the entity in our population
			detachDeletedEntity(entityFromPopulation);

			// Finally remove it from the population.
			population.remove(entityFromPopulation);
		}
	}

	/**
	 * This method traverses through the whole population and detaches all references to the given entity. That means
	 * that single entity references will be cleared (set to null) and references in collections will be removed from
	 * the collection. Note: that this traversing algorithm is not optimized and simply walks through potential single
	 * and collection properties without inspecting the actual property or collection element type in advance.
	 */
	private void detachDeletedEntity(GenericEntity entityFromPopulation) {
		BaseType.INSTANCE.traverse(population, null, new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {

				for (Property p : entity.entityType().getProperties()) {
					Object value = p.get(entity);
					if (value != null) {

						TypeCode typeCode = typeReflection.getType(value).getTypeCode();
						switch (typeCode) {
							case entityType:
								if (value == entityFromPopulation)
									p.set(entity, null); // detach direct reference.
								break;
							case listType:
							case setType:
								Collection<?> collectionValue = (Collection<?>) value;
								Iterator<?> iterator = collectionValue.iterator();
								while (iterator.hasNext()) {
									Object element = iterator.next();
									if (element == entityFromPopulation)
										iterator.remove(); // detach from collection.
								}
								break;
							case mapType:
								Set<Object> keysToBeRemoved = new HashSet<>();
								Map<?, ?> mapValue = (Map<?, ?>) value;
								for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
									Object entryKey = entry.getKey();
									Object entryValue = entry.getValue();

									if (entryKey == entityFromPopulation || entryValue == entityFromPopulation)
										keysToBeRemoved.add(entryKey);
								}
								mapValue.keySet().removeAll(keysToBeRemoved); // detach from map.
								break;
							default:
								break;

						}
					}

				}

			}

		});
	}

	/**
	 * Replace given value, which might contain instances attached to the session from
	 * {@link BasicAccessAdapter#applyLocal} method, with instances from {@link #population}.
	 */
	private Object convertToLocalValue(Object value) {
		return value == null ? null : BaseType.INSTANCE.clone(localizingCloningContext, value, null);
	}

	private final CloningContext localizingCloningContext = new StandardCloningContext() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T result = super.getAssociated(entity);
			return result != null ? result : (T) getEntityFromPopulation(entity);
		}
	};

	private GenericEntity getEntityFromPopulation(GenericEntity updatedEntity) {
		EntityReference ref = updatedEntity.reference();

		GenericEntity result = findEntityInPopulation(ref);
		if (result == null) {
			throw new GenericModelException("No entity with reference: " + ref + " found.");
		}

		return result;
	}

	protected void setIdForEntity(GenericEntity entity) throws ModelAccessException {
		// Check if there's already an id assigned by the client.
		// If not, we generate a new one and assign it to the entity.
		if (entity.getId() != null)
			return;

		try {
			// We use the IdGenerator from the population builder to generate new ids
			IdGenerator idGenerator = populationBuilder.getIdGenerator();
			Object id = idGenerator.generateId(entity);

			/* Assigning the new id to the entity is important since this manipulations are tracked outside and finally
			 * sent back to the caller as InducedManipulations. */
			entity.setId(id);

		} catch (Exception e) {
			throw new ModelAccessException("Can't create id for given entity: " + entity, e);
		}
	}

	/**
	 * Internal method that runs through the population collection and searches for an entity matching the typeSignature
	 * and id of passed entityReference.
	 * 
	 * @return the entity matching the entityReference. Null if no entity is found.
	 */
	protected GenericEntity findEntityInPopulation(EntityReference ref) {
		return population.stream() //
				.filter(e -> isReferenceForEntity(ref, e)) //
				.findFirst() //
				.orElse(null);
	}

	private boolean isReferenceForEntity(EntityReference ref, GenericEntity e) {
		return e.type().getTypeSignature().equals(ref.getTypeSignature()) && //
				e.getId().equals(ref.getRefId());
	}

	/** Returns a collection of GenericEntity instances that are assignable for the given type. */
	private Set<GenericEntity> collectTypePopulation(EntityType<GenericEntity> entityType) {
		// Filtering the population collection for instances that matches the given type
		return population.stream() //
				.filter((e) -> entityType.isInstance(e)) //
				.collect(Collectors.toSet());
	}

	/**
	 * A nested {@link PageProvider} implementation that splits a given collection of entities into chunks (depending on
	 * the given pageSize) and returns according sub lists (pages) when requested.
	 */
	private final class CollectionPageProvider implements PageProvider<GenericEntity> {
		private final List<GenericEntity> internalPopulation;
		private final int internalPageSize;
		private int currentPage = 0;
		private boolean end = false;

		/**
		 * Takes the population collection and the expected internal pageSize to construct the PageProvider.
		 */
		public CollectionPageProvider(Collection<GenericEntity> population, int internalPageSize) {
			// We build an internal used list of entities based on the typePopulation.
			this.internalPopulation = new ArrayList<>(population);
			this.internalPageSize = internalPageSize;
		}

		/**
		 * Called by the {@link PaginatedIterable} during iterating through the population.
		 */
		@Override
		public Collection<GenericEntity> nextPage() {
			// We have no more elements to process. Returning null to indicate end.
			if (end) {
				return null;
			}

			// Calculate start and end index based on current page and pageSize.
			int startIndex = currentPage * internalPageSize;
			int endIndex = startIndex + internalPageSize;

			if (endIndex >= internalPopulation.size()) {
				// We reached the last page to provide. Adapt the endIndex and signal end by setting the internal flag.
				endIndex = internalPopulation.size();
				end = true;
			}

			// Increment page counter to be prepared for next iteration.
			currentPage++;

			// Return the sub list (page).
			return internalPopulation.subList(startIndex, endIndex);
		}
	}

}
