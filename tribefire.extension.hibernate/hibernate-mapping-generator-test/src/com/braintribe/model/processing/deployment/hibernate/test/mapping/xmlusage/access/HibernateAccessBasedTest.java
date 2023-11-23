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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlusage.access;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.access.hibernate.HibernateAccess;
import com.braintribe.model.access.hibernate.HibernateAccessInitializationContext;
import com.braintribe.model.access.hibernate.interceptor.GmAdaptionInterceptor;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlusage.DatabaseTest;
import com.braintribe.model.processing.deployment.hibernate.test.metamodel.MetaModelProvider;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * <p>
 * Tests based on {@link com.braintribe.model.access.hibernate.HibernateAccess}.
 * 
 */
public abstract class HibernateAccessBasedTest extends DatabaseTest {

	protected Random randon = new Random();
	protected AtomicInteger entityId = new AtomicInteger(1);

	private static final String mappingsDir = "src/test/expected/testSkeletonMetaModel";

	private static HibernateAccess hibernateAccess;

	private Set<EntityType<? extends GenericEntity>> types;

	public abstract Class<?>[] getTestEntityClasses();

	protected static void initialize() throws Exception {
		initializeDatabaseContext();
		hibernateAccess = createHibernateAccess();
	}

	protected static void destroy() throws Exception {
		destroyDatabaseContext();
		hibernateAccess = null;
	}

	protected static HibernateAccess createHibernateAccess() throws Exception {

		File file = new File(mappingsDir);
		if (file.listFiles() == null || file.listFiles().length == 0) {
			throw new Exception("No mappins in mappings dir " + mappingsDir);
		}

		HibernateAccessInitializationContext context = new HibernateAccessInitializationContext();
		context.setConnectionDriver(Class.forName(driver));
		context.setConnectionUsername(user);
		context.setConnectionPassword(password);
		context.setConnectionUrl(url);
		context.setDialect(dialect);

		context.setHibernateMappingsFolders(Arrays.asList(file));
		context.setInterceptor(new GmAdaptionInterceptor());

		// examples of property configuration:
		// context.getHibernateConfigurationProperties().put("hibernate.bytecode.use_reflection_optimizer", "false");
		// context.getHibernateConfigurationProperties().put("hibernate.bytecode.provider", "cglib");

		HibernateAccess access = HibernateAccessTestTools.newHibernateAccess(context);
		access.setModelSupplier(MetaModelProvider::provideModel);

		return access;

	}

	protected static HibernateAccess getHibernateAccess() throws Exception {
		return hibernateAccess;
	}

	protected PersistenceGmSession createPersistenceGmSession() throws Exception {
		BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(createHibernateAccess());
		return session;
	}

	protected void testPersistAndQuery() throws Exception {

		loadTypes();

		PersistenceGmSession gmSession = createPersistenceGmSession();

		cleanUp(gmSession);

		Map<EntityType<GenericEntity>, GenericEntity> created = persist(gmSession);

		gmSession.commit();

		query(gmSession, created);

		System.out.println("Test completed successfully.");

	}

	protected void loadTypes() throws Exception {
		types = getTestTypes(getTestEntityClasses());
	}

	protected void cleanUp(@SuppressWarnings("unused") PersistenceGmSession gmSession) throws Exception {
		// No impl so far
	}

	@SuppressWarnings("unchecked")
	private Map<EntityType<GenericEntity>, GenericEntity> persist(PersistenceGmSession gmSession) throws Exception {
		Map<EntityType<GenericEntity>, GenericEntity> created = new HashMap<EntityType<GenericEntity>, GenericEntity>();
		for (EntityType<? extends GenericEntity> type : types) {
			if (!type.isAbstract()) {
				created.put((EntityType<GenericEntity>) type, createEntity(gmSession, type));
			}
		}
		return created;
	}

	private void query(PersistenceGmSession gmSession, Map<EntityType<GenericEntity>, GenericEntity> created) throws Exception {
		List<EntityQuery> queries = createEntityQueries(created);
		executeQueries(gmSession, queries);
	}

	private static List<EntityQuery> createEntityQueries(Map<EntityType<GenericEntity>, GenericEntity> created) {

		List<EntityQuery> queries = new ArrayList<EntityQuery>();

		for (Map.Entry<EntityType<GenericEntity>, GenericEntity> entry : created.entrySet()) {

			EntityType<GenericEntity> type = entry.getKey();
			GenericEntity entity = entry.getValue();

			queries.add(EntityQueryBuilder.from(type).done());

			List<Property> properties = type.getProperties();

			for (Property property : properties) {

				if (property.getName().startsWith("$")) {
					continue;
				}

				String propertyName = property.getName();
				Object propertyValue = type.getProperty(propertyName).get(entity);
				if (property.getType() instanceof CollectionType) {
					CollectionType colType = (CollectionType) property.getType();
					if (colType.getCollectionKind() != CollectionKind.map && propertyValue instanceof Collection) {
						Collection<?> collection = (Collection<?>) propertyValue;
						if (collection.isEmpty()) {
							if (colType.getCollectionElementType() instanceof SimpleType) {
								queries.add(EntityQueryBuilder.from(type).where().value(null).in().property(propertyName).done());
							}
						} else {
							for (Object collectionElement : collection) {
								queries.add(EntityQueryBuilder.from(type).where().value(collectionElement).in().property(propertyName).done());
							}
						}
					}

				} else {
					queries.add(EntityQueryBuilder.from(type).where().property(propertyName).eq(propertyValue).done());
				}

			}
		}

		return queries;
	}

	protected void executeQueries(PersistenceGmSession gmSession, List<EntityQuery> queries) throws GmSessionException {
		for (EntityQuery entityQuery : queries) {
			List<GenericEntity> list = gmSession.query().entities(entityQuery).list();
			System.out.println("Queried (" + entityQuery.getEntityTypeSignature() + "). Results: " + GMCoreTools.getDescription(list));
		}
	}

	protected <T extends GenericEntity> T createEntity(PersistenceGmSession gmSession, EntityType<T> entityType) {
		T entity = createEntity(gmSession, entityType, null);
		System.out.println("Created: " + GMCoreTools.getDescription(entity));
		return entity;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends GenericEntity> T createEntity(PersistenceGmSession gmSession, EntityType<T> entityType, Map<EntityType<T>, T> created) {

		if (created != null && created.containsKey(entityType)) {
			return created.get(entityType);
		}

		if (created == null) {
			created = new HashMap<EntityType<T>, T>();
		}

		T entity = gmSession.create(entityType);
		String tempIdent = entityType.getShortName() + "-" + entityId.getAndIncrement();

		List<Property> properties = entityType.getProperties();

		for (Property property : properties) {

			if (property.isIdentifying() || property.isGlobalId()) {
				continue;
			}

			GenericModelType propertyType = property.getType();

			if (propertyType.isSimple()) {
				if (propertyType.getTypeCode() == TypeCode.stringType) {
					property.set(entity, property.getName() + " property for " + tempIdent);
				} else if (propertyType.isNumber()) {
					Number randomNumber = null;
					String typeName = ((SimpleType) propertyType).getTypeName();
					if (typeName.equals("integer")) {
						randomNumber = randon.nextInt();
					} else if (typeName.equals("long")) {
						randomNumber = Integer.valueOf(randon.nextInt()).longValue();
					} else if (typeName.equals("double")) {
						randomNumber = Integer.valueOf(randon.nextInt()).doubleValue();
					}
					property.set(entity, randomNumber);
				}
			} else if (propertyType.isEntity()) {
				EntityType propertyEntityType = (EntityType) propertyType;
				if (!propertyEntityType.isAbstract()) {
					property.set(entity, createEntity(gmSession, propertyEntityType, created));
				}
			}

		}

		created.put(entityType, entity);

		return entity;
	}

	@SuppressWarnings("unchecked")
	public Set<EntityType<? extends GenericEntity>> getTestTypes(Class<?>[] typeArray) {
		Set<EntityType<? extends GenericEntity>> res = new HashSet<EntityType<? extends GenericEntity>>();
		for (Class<?> type : typeArray) {
			res.add(GMF.getTypeReflection().getEntityType((Class<? extends GenericEntity>) type));
		}
		return res;
	}
}
