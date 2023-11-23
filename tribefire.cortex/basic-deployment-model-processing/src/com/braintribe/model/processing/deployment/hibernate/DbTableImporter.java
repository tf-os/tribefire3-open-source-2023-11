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
package com.braintribe.model.processing.deployment.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
//import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.NullSafe;

/**
 * 
 */
public class DbTableImporter {

	private final GmMetaModel metaModel;
	private final GmTypeRegistry registry;
	private final GmEntityType genericEntityGmType;
	private final PersistenceGmSession session;
	private final Set<DbTable> tablesToProcess;
	private final Set<DbTable> processedTables;

	/**
	 * The session must be able to provide (via queries) at least the {@link GenericEntity} and all the {@link GmSimpleType}s.
	 */
	public DbTableImporter(GmMetaModel metaModel, PersistenceGmSession session, Set<DbTable> dbTables) throws GmSessionException {
		this.metaModel = metaModel;
		this.session = session;
		this.registry = new GmTypeRegistry(metaModel, session);
		this.genericEntityGmType = findPredefinedType(GenericEntity.class.getName());
		this.tablesToProcess = new HashSet<DbTable>(dbTables);
		this.processedTables = new HashSet<DbTable>();
	}

	private GmEntityType findPredefinedType(String typeSig) throws GmSessionException {
		GmEntityType gmEntityType = registry.getGmEntityType(typeSig);

		if (gmEntityType == null) {
			gmEntityType = getTypeFromSession(typeSig);
		}

		if (gmEntityType == null) {
			throw new GmSessionException("GmEntityType not found for " + typeSig + ".");
		}

		this.metaModel.getTypes().add(gmEntityType);

		return gmEntityType;
	}

	/**
	 * Creates a {@link GmEntityType} for given {@link DbTable}, as long as such entity isn't already present in the
	 * {@link PersistenceGmSession} (check is based on type signature).The entity is initialized with properties for
	 * given columns, if the column type is supported (Blobs and such are not supported, see
	 * {@link DbTableProcessingUtils}). MetaData needed for creating the Hibernate mapping are set as well (
	 * {@link EntityMapping}, {@link PropertyMapping}).
	 */
	public void importDbTables() throws GmSessionException {
		while (!tablesToProcess.isEmpty()) {
			DbTable dbTable = tablesToProcess.iterator().next();

			importDbTable(dbTable);

			tablesToProcess.remove(dbTable);
			processedTables.add(dbTable);
		}
	}

	private void importDbTable(DbTable dbTable) throws GmSessionException {
		GmEntityType gmEntityType = acquireGmEntityFor(dbTable);
		ensureHibernateMetaData(gmEntityType, dbTable);

		List<GmProperty> properties = getProperties(gmEntityType);

		for (DbColumn dbColumn : NullSafe.list(dbTable.getColumns())) {
			notifyColumn(dbColumn);

			GmProperty gmProperty = registry.getProperty(dbColumn);

			if (gmProperty == null) {
				GmType propertyType = getTypeForColumn(dbColumn);

				if (propertyType == null) {
					continue;
				}

				gmProperty = createProperty(gmEntityType, dbColumn, propertyType);
				registry.registerProperty(gmProperty);
				properties.add(gmProperty);
			}

			ensureHibernateMetaData(gmProperty, dbColumn);
		}
	}

	private void notifyColumn(DbColumn dbColumn) {
		if (dbColumn.getReferencedTable() != null) {
			notifyTable(dbColumn.getReferencedTable());
		}
	}

	private void notifyTable(DbTable dbTable) {
		if (!processedTables.contains(dbTable)) {
			tablesToProcess.add(dbTable);
		}
	}

	private GmEntityType getTypeFromSession(String typeSignature) throws GmSessionException {
		EntityQuery query = EntityQueryBuilder.from(GmEntityType.class).where().property("typeSignature").eq(typeSignature).done();
		List<GmEntityType> list = session.query().entities(query).list();
		return list.isEmpty() ? null : list.get(0);
	}

	private GmEntityType acquireGmEntityFor(DbTable dbTable) throws GmSessionException {
		String typeSignature = DbTableProcessingUtils.getEntitySignatureFrom(metaModel, dbTable);
		GmEntityType gmEntityType = registry.getGmEntityType(typeSignature);

		if (gmEntityType != null) {
			return gmEntityType;
		}

		gmEntityType = getTypeFromSession(typeSignature);

		if (gmEntityType == null) {
			gmEntityType = registerNewTypeWithSignature(typeSignature);
			addInheritanceInformation(gmEntityType, genericEntityGmType);
			gmEntityType.setIsAbstract(false);
			gmEntityType.setDeclaringModel(metaModel);
		}

		registry.registerEntity(gmEntityType);
		metaModel.getTypes().add(gmEntityType);

		return gmEntityType;
	}

	private GmProperty createProperty(GmEntityType owner, DbColumn dbColumn, GmType propertyType) {
		// PGA TODO FIX
		GmProperty gp = session.create(GmProperty.T);
		gp.setName(DbTableProcessingUtils.getPropertyName(dbColumn));
		gp.setDeclaringType(owner);
		//gp.setIsId(dbColumn.getIsPrimaryKey());
		gp.setType(propertyType);

		return gp;
	}

	private GmType getTypeForColumn(DbColumn dbColumn) throws GmSessionException {
		if (dbColumn.getReferencedTable() != null) {
			return acquireGmEntityFor(dbColumn.getReferencedTable());
		}

		SimpleType type = DbTableProcessingUtils.getGmTypeFromSqlType(dbColumn.getDataType(), dbColumn.getTypeName());

		if (type == null) {
			return null;
		}

		return registry.getGmSimpleType(type.getTypeSignature());
	}

	private List<GmProperty> getProperties(GmEntityType gmEntityType) {
		if (gmEntityType.getProperties() == null) {
			gmEntityType.setProperties(new ArrayList<GmProperty>());
		}

		return gmEntityType.getProperties();
	}

	private void addInheritanceInformation(GmEntityType _sub, GmEntityType _super) {
		if (_sub.getSuperTypes() == null) {
			_sub.setSuperTypes(new ArrayList<GmEntityType>());
		}

		_sub.getSuperTypes().add(_super);
	}

	private GmEntityType registerNewTypeWithSignature(String typeSignature) {
		GmEntityType gmEntityType = session.create(GmEntityType.T);
		gmEntityType.setTypeSignature(typeSignature);

		return gmEntityType;
	}

	// ##################################
	// ## . . . . . MetaData . . . . . ##
	// ##################################

	private void ensureHibernateMetaData(GmEntityType gmEntityType, DbTable dbTable) {
//		EntityMapping mapping = null;
//
//		if (gmEntityType.getMetaData() == null) {
//			gmEntityType.setMetaData(new HashSet<>());
//		} else {
//			mapping = CollectionTools.getFirstElement(gmEntityType.getMetaData(), EntityMapping.class);
//		}
//
//		if (mapping == null) {
//			mapping = session.create(EntityMapping.T);
//			gmEntityType.getMetaData().add(mapping);
//		}
//
//		mapping.setTableName(dbTable.getName());
//		mapping.setSchema(dbTable.getSchema());
//		mapping.setCatalog(dbTable.getCatalog());
	}

	private void ensureHibernateMetaData(GmProperty gmProperty, DbColumn dbColumn) {
//		PropertyMapping mapping = null;
//
//		if (gmProperty.getMetaData() == null) {
//			gmProperty.setMetaData(new HashSet<>());
//		} else {
//			mapping = CollectionTools.getFirstElement(gmProperty.getMetaData(), PropertyMapping.class);
//		}
//
//		if (mapping == null) {
//			mapping = session.create(PropertyMapping.T);
//			gmProperty.getMetaData().add(mapping);
//		}
//
//		mapping.setColumnName(dbColumn.getName());
//
//		// PGA TODO FIX
//		// if (gmProperty.getIsId() && isIntegerType(gmProperty.getType())) {
//		// mapping.setAutoAssignable(Boolean.TRUE);
//		// }
	}

	@SuppressWarnings("unused")
	private boolean isIntegerType(GmType type) {
		if (!(type instanceof GmSimpleType)) {
			return false;
		}

		String ts = type.getTypeSignature();
		return ts.equals(GenericModelTypeReflection.TYPE_INTEGER.getTypeSignature())
				|| ts.equals(GenericModelTypeReflection.TYPE_LONG.getTypeSignature());
	}

}
