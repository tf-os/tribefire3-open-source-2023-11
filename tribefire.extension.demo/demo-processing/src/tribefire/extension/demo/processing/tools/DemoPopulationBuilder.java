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
package tribefire.extension.demo.processing.tools;

import static com.braintribe.exception.Exceptions.unchecked;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tools.GenericEntityStringifier;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.web.api.WebApps;

import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Person;

/**
 * This utility class is responsible for building a population of the DemoModel
 * (e.g, instances of {@link Person} and {@link Company}) and linking them
 * together.
 */
public class DemoPopulationBuilder {

	/**
	 * The GenericModelTypeReflection used to generically deal with
	 * {@link GenericEntity}'s.
	 */
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	/**
	 * The GenericModelType for {@link Resource}
	 */
	private static final EntityType<Resource> resourceType = typeReflection.getEntityType(Resource.class);

	/**
	 * The GenericModelType for {@link Icon}
	 */
	private static final EntityType<Icon> iconType = typeReflection.getEntityType(Icon.class);

	private final IdGenerator defaultIdGenerator = (entity) -> UUID.randomUUID().toString();

	/**
	 * IdGenerator used to generate id for each created entity.<br />
	 * Default implementation can be overridden with
	 * {@link #idGenerator(IdGenerator)}.<br/>
	 * {@link #noIdGenerator()}) can be used to turn off the IdGenerator.
	 */
	private IdGenerator idGenerator = defaultIdGenerator;

	/**
	 * The description of the property order per type.
	 */
	private final Map<Class<? extends GenericEntity>, String[]> descriptions = new HashMap<>();

	/**
	 * The raw data information per type.
	 */
	private final Map<Class<? extends GenericEntity>, Object[][]> rawData = new HashMap<>();

	/**
	 * Collection holding all created instances.
	 */
	private final Set<GenericEntity> population = new HashSet<>();

	/**
	 * Map holding all created instances filtered by their typeSignature.
	 */
	private final Map<String, Set<GenericEntity>> populationByType = new HashMap<>();

	/**
	 * Collection holding information about entities that needs to be linked in
	 * phase 2.
	 */
	private final Set<Link> links = new HashSet<>();

	/**
	 * If set to true definitions and rawData records will be initialized from
	 * {@link DemoPopulation} if not explicitly set.
	 */
	private boolean initDefaults = true;

	/**
	 * Private constructor. Use {@link #newInstance()} to get an instance.
	 */
	private DemoPopulationBuilder() {
	}

	// ***********************************************************************
	// Methods to build the creator instance.
	// ***********************************************************************

	/**
	 * Returns an instance of the {@link DemoPopulationBuilder}
	 */
	public static DemoPopulationBuilder newInstance() {
		return new DemoPopulationBuilder();
	}

	/**
	 * Returns the configured {@link IdGenerator}
	 */
	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	/**
	 * Adds a description for passed type.
	 */
	public DemoPopulationBuilder addDescription(Class<? extends GenericEntity> clazz, String[] description) {
		descriptions.put(clazz, description);
		return this;
	}

	/**
	 * Adds raw data records for passed type.
	 */
	public DemoPopulationBuilder addRecords(Class<? extends GenericEntity> clazz, Object[][] records) {
		rawData.put(clazz, records);
		return this;
	}

	/**
	 * Turns off initializing data from {@link DemoPopulation} in case no
	 * description is defined.
	 */
	public DemoPopulationBuilder noDefaults() {
		this.initDefaults = false;
		return this;
	}

	/**
	 * Sets an alternative {@link IdGenerator}
	 */
	public DemoPopulationBuilder idGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		return this;
	}

	/**
	 * Turns off the Id generation.<br/>
	 * It's equal to call {@link #idGenerator(IdGenerator)} with a null argument.
	 */
	public DemoPopulationBuilder noIdGenerator() {
		return idGenerator(null);
	}

	/**
	 * This method creates and return the full demo population (instances of the
	 * DemoModel types). <br />
	 * Every instance of the object network is flattened contained in the resulting
	 * Set.
	 */
	public Collection<GenericEntity> build() {

		// Preparation - Initialize with default data and descriptions.
		initDefaults();

		// Phase 1 - Build raw instances and collect link information
		buildEntities();

		// Phase 2 - Link entities based on previously collected information
		linkEntities();

		return population;
	}

	/**
	 * Returns the created instance of the demo population filtered by their
	 * typeSignature.
	 */
	public Map<String, Set<GenericEntity>> buildForTypes() {
		build();
		return populationByType;
	}

	// ***********************************************************************
	// Private helper methods.
	// ***********************************************************************

	/**
	 * Initializes the {@link #descriptions} and {@link #rawData} with the data
	 * retrieved from {@link DemoPopulation} if no descriptions are set.
	 */
	private void initDefaults() {
		if (initDefaults && descriptions.isEmpty()) {
			descriptions.putAll(DemoPopulation.demoDescriptions);
			rawData.putAll(DemoPopulation.demoRawData);
		}
	}

	/**
	 * Iterates through all available rawData types and generically builds instances
	 * of each of the types based on the data and description registered for this
	 * type.
	 */
	private <T extends GenericEntity> void buildEntities() {

		for (Class<? extends GenericEntity> clazz : rawData.keySet()) {

			// Get the GM-type for the given clazz
			EntityType<T> type = typeReflection.getEntityType(clazz);

			// Get description and raw data records for given type
			String[] description = descriptions.get(clazz);
			Object[][] records = rawData.get(clazz);

			if (description == null || records == null || records.length > description.length) {
				// No or invalid definition. Ignore.
				return;
			}

			// Build an entity per raw data record and add it to population
			for (Object[] record : records) {
				buildEntity(type, description, record);
			}

		}

	}

	/**
	 * Link the created entities based on the collected informations..
	 */
	@SuppressWarnings("unchecked")
	private void linkEntities() {

		for (Link link : links) {
			Property linkProperty = link.property;

			if (link instanceof SingleLink) {
				// Search for the entity based on the key of the SingleLink and
				// directly assign the result to the linkProperty.
				EntityType<GenericEntity> type = (EntityType<GenericEntity>) linkProperty.getType();
				GenericEntity referencedEntity = findEntityByKey(type, ((SingleLink) link).key);
				linkProperty.set(link.entity, referencedEntity);
			} else if (link instanceof CollectionLink) {
				// Search all entities identified by the link keys and assign
				// them to the collection of the linkProperty.
				CollectionType collectionType = (CollectionType) linkProperty.getType();
				EntityType<GenericEntity> elementType = (EntityType<GenericEntity>) collectionType
						.getCollectionElementType();
				Collection<GenericEntity> propertyCollection = linkProperty.get(link.entity);
				for (String refKey : ((CollectionLink) link).keys) {
					GenericEntity elementEntity = findEntityByKey(elementType, refKey);
					if (elementEntity != null) {
						propertyCollection.add(elementEntity);
					}
				}
			} else if (link instanceof MapLink) {
				CollectionType mapType = (CollectionType) linkProperty.getType();
				EntityType<GenericEntity> mapValueType = (EntityType<GenericEntity>) mapType.getParameterization()[1];
				Map<Object, GenericEntity> propertyMap = linkProperty.get(link.entity);

				for (Map.Entry<Object, String> linkEntry : ((MapLink) link).keys.entrySet()) {
					Object mapKey = linkEntry.getKey();
					GenericEntity mapValue = findEntityByKey(mapValueType, linkEntry.getValue());
					propertyMap.put(mapKey, mapValue);
				}

			}
		}

	}

	/**
	 * Searches an entity of given type identified by the given key (first field in
	 * description).
	 */
	@SuppressWarnings("unchecked")
	private <T extends GenericEntity> T findEntityByKey(EntityType<T> type, String referenceKey) {

		String[] description = descriptions.get(type.getJavaType());
		String keyField = description[0];

		// Run through the full population
		for (GenericEntity entity : population) {
			EntityType<GenericEntity> currentType = entity.entityType();
			// Check whether current entity type is same or sub-type of
			// requested type.
			if (type.isAssignableFrom(currentType)) {
				// Get the value of the keyField of current entity.
				Object keyValue = currentType.getProperty(keyField).get(entity);
				if (referenceKey.equals(keyValue)) {
					// Requested key matches current entity value.
					return (T) entity;
				}
			}
		}
		return null;

	}

	/**
	 * Generically builds an instance of the given type based on the data record and
	 * the given description.
	 */
	private <T extends GenericEntity> T buildEntity(EntityType<T> type, String[] description, Object[] record) {

		T entity = createAndRegisterEntity(type);

		for (int i = 0; i < record.length; i++) {
			Object rawValue = record[i];
			String field = description[i];
			Property property = type.findProperty(field);
			if (property != null) {
				GenericModelType propertyType = property.getType();
				Object propertyValue = buildValue(propertyType, rawValue);

				if (propertyValue instanceof Link) {
					Link reference = (Link) propertyValue;
					reference.setOwner(entity, property);
					// Remember reference for phase 2.
					links.add(reference);
				} else {
					// Assign propertyValue to property
					property.set(entity, propertyValue);
				}

			}
		}

		return entity;
	}

	/**
	 * Same as {@link #createAndRegisterEntity(EntityType)} but takes a
	 * {@link Class} parameter and converts it in an {@link EntityType}
	 */
	@SuppressWarnings("unchecked")
	private <T extends GenericEntity> T createAndRegisterEntity(Class<T> clazz) {
		return (T) createAndRegisterEntity(typeReflection.getEntityType(clazz));
	}

	/**
	 * Creates an instance of the given type and assigns the id generated by the
	 * {@link IdGenerator}. <br />
	 */
	private <T extends GenericEntity> T createAndRegisterEntity(EntityType<T> type) {

		// Create raw instance
		T entity = type.create();

		// Set the id property (if exists) of the entity with value provided by the
		// idGenerator.
		try {
			if (idGenerator != null) {
				entity.setId(idGenerator.generateId(entity));
			}
		} catch (Exception e) {
			throw new GenericModelException("Error while generating id for entity: " + entity, e);
		}

		// Adding created entity to population collection(s).
		population.add(entity);
		Set<GenericEntity> typePopulation = populationByType.get(type.getTypeSignature());
		if (typePopulation == null) {
			typePopulation = new HashSet<>();
			populationByType.put(type.getTypeSignature(), typePopulation);
		}
		typePopulation.add(entity);

		return entity;

	}

	/**
	 * Builds the entity value for a given property based on the given rawValue
	 */
	@SuppressWarnings("unchecked")
	private Object buildValue(GenericModelType valueType, Object rawValue) {

		if (rawValue == null) {
			return null;
		}

		Object propertyValue = null;

		switch (valueType.getTypeCode()) {
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case enumType:
			case stringType:
				propertyValue = rawValue;
				break;
			case objectType:
				GenericModelType actualType = typeReflection.getType(rawValue);
				propertyValue = buildValue(actualType, rawValue);
				break;
			case entityType:
				EntityType<GenericEntity> entityType = (EntityType<GenericEntity>) valueType;

				if (rawValue instanceof String) {
					// Referenced entity
					String stringValue = (String) rawValue;

					// First we check whether the entity type is a known type
					// (Resource or Icon). Otherwise we check for a link.
					if (resourceType.isAssignableFrom(entityType)) {
						// We should build a Resource.
						Resource resource = buildResource(stringValue);
						propertyValue = resource;
					} else if (iconType.isAssignableFrom(entityType)) {
						// We should build an (simple) Icon.
						Icon icon = buildIcon(new String[] { stringValue });
						propertyValue = icon;
					} else if (stringValue.startsWith("ref:")) {
						// We should collect a Link
						String refKey = stringValue.substring("ref:".length(), stringValue.length());
						// Building a reference instead of the actual value.
						propertyValue = new SingleLink(refKey);
					}
				} else if (rawValue instanceof Object[]) {

					if (iconType.isAssignableFrom(entityType)) {
						// We should build an (adaptive) Icon.
						Icon icon = buildIcon((String[]) rawValue);
						propertyValue = icon;
					} else {
						// Nested entity
						Object[] nestedRecord = (Object[]) rawValue;
						String[] nestedDescription = descriptions.get(entityType.getJavaType());
						GenericEntity nestedEntity = buildEntity(entityType, nestedDescription, nestedRecord);
						propertyValue = nestedEntity;
					}

				}
				break;
			case listType:
			case setType:
				if (rawValue instanceof Object[]) {
					propertyValue = buildCollectionValue((CollectionType) valueType, (Object[]) rawValue);
				}
				break;
			case mapType:
				if (rawValue instanceof Object[][]) {
					CollectionType mapType = (CollectionType) valueType;
					Object[][] mapRawValue = (Object[][]) rawValue;

					MapLink mapLink = null;
					Map<Object, Object> map = null;
					for (Object[] mapElement : mapRawValue) {
						Object mapKey = buildValue(mapType.getParameterization()[0], mapElement[0]);
						Object mapValue = buildValue(mapType.getParameterization()[1], mapElement[1]);

						if (mapValue instanceof SingleLink) {
							if (mapLink == null) {
								mapLink = new MapLink();
								propertyValue = mapLink;
							}
							mapLink.addReferenceKey(mapKey, (SingleLink) mapValue);
						} else {
							if (map == null) {
								map = (Map<Object, Object>) mapType.createPlain();
								propertyValue = map;
							}
							map.put(mapKey, mapValue);
						}

					}
				}
				break;
			default:
				System.out.println("Unsupported property type: " + valueType.getTypeCode());
				break;
		}
		return propertyValue;

	}

	/**
	 * Builds the collection value for given raw value which is either a collection
	 * or {@link CollectionLink}.
	 */
	@SuppressWarnings("unchecked")
	private Object buildCollectionValue(CollectionType collectionType, Object[] collectionRawValue) {
		Object propertyValue = null;
		CollectionLink collectionLink = null;
		Collection<Object> collection = null;
		for (Object elementRawValue : collectionRawValue) {
			Object elementValue = buildValue(collectionType.getCollectionElementType(), elementRawValue);
			if (elementValue instanceof SingleLink) {
				if (collectionLink == null) {
					collectionLink = new CollectionLink();
					// Building a collection reference instead of the
					// actual value.
					propertyValue = collectionLink;
				}
				collectionLink.addReferenceKey((SingleLink) elementValue);
			} else {
				if (collection == null) {
					collection = (Collection<Object>) collectionType.createPlain();
					propertyValue = collection;
				}
				collection.add(elementValue);
			}
		}
		return propertyValue;
	}

	/**
	 * Builds either a {@link SimpleIcon} or {@link AdaptiveIcon} based on the
	 * passed path(s).
	 */
	private Icon buildIcon(String[] paths) {

		List<Resource> iconResources = new ArrayList<>();
		for (String path : paths) {
			iconResources.add(buildResource(path));
		}

		switch (iconResources.size()) {
			case 0:
				return null;
			case 1:
				Resource iconResource = iconResources.get(0);
				SimpleIcon simpleIcon = createAndRegisterEntity(SimpleIcon.class);
				simpleIcon.setImage(iconResource);
				simpleIcon.setName(buildIconName(iconResource));
				return simpleIcon;
			default:
				AdaptiveIcon adaptiveIcon = createAndRegisterEntity(AdaptiveIcon.class);
				adaptiveIcon.getRepresentations().addAll(iconResources);
				adaptiveIcon.setName(buildIconName(iconResources.get(0)));
				return adaptiveIcon;
		}

	}

	/**
	 * Builds the name of the icon based on the given resource.
	 */
	private String buildIconName(Resource resource) {
		String iconName = resource.getName();
		int idx = iconName.indexOf(".");
		if (idx > 1) {
			iconName = iconName.substring(0, idx);
		}
		return iconName + " Icon";
	}

	/**
	 * Builds a {@link Resource} based on the given path
	 */
	private Resource buildResource(String path) {

		Resource resource = createAndRegisterEntity(Resource.class);
		FileSystemSource source = createAndRegisterEntity(FileSystemSource.class);

		String filename = path;
		int idx = path.lastIndexOf("/");
		if (idx > 1) {
			filename = path.substring(idx + 1, path.length());
		}
		resource.setName(filename);
		resource.setResourceSource(source);
		source.setPath(resourcePath(path));

		return resource;

	}

	private String resourcePath(String resourcePath) {
		Path path = null;
		try {
			path = Paths.get(WebApps.servletContext().getResource("/WEB-INF").toURI());
		} catch (Exception e) {
			throw unchecked(e, "Failed to resolve " + resourcePath);
		}
		return path.toString()  + resourcePath;
	}


	// ***********************************************************************
	// Inner Classes used to collect link informations that is used in Phase 2
	// ***********************************************************************

	private abstract class Link {
		GenericEntity entity;
		Property property;

		public void setOwner(GenericEntity holder, Property property) {
			this.entity = holder;
			this.property = property;
		}

	}

	private class SingleLink extends Link {
		String key;

		public SingleLink(String key) {
			this.key = key;
		}

	}

	private class CollectionLink extends Link {
		List<String> keys = new ArrayList<>();

		public void addReferenceKey(SingleLink singleLink) {
			this.keys.add(singleLink.key);
		}

	}

	private class MapLink extends Link {
		Map<Object, String> keys = new HashMap<>();

		public void addReferenceKey(Object mapKey, SingleLink singleLink) {
			this.keys.put(mapKey, singleLink.key);
		}

	}

	// **********************************************************************
	// Main method to test this class.
	// **********************************************************************

	/**
	 * Calls {@link DemoPopulationBuilder#build()} and prints the resulting entities
	 * to {@link System#out}
	 */
	public static void main(String[] args) {

		Collection<GenericEntity> population = DemoPopulationBuilder.newInstance().build();
		GenericEntityStringifier stringifier = GenericEntityStringifier.newInstance();
		int populationCount = 0;
		for (GenericEntity entity : population) {
			// Leveraging GenericEntityStringifier to produce readable output.
			System.out.println(stringifier.stringify(entity));
			populationCount++;
		}
		System.out.println("Found " + populationCount + " instances.");

	}

}
