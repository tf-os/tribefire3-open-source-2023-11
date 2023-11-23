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
package com.braintribe.model.processing.cortex.service.connection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
//import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
//import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
//import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
//import com.braintribe.model.accessdeployment.jpa.meta.JpaColumn;
//import com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId;
import com.braintribe.model.cortexapi.connection.DbSchemaResponse;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.CollectionTools;

public class ModelWithDbSchemaSynchronizer extends ServiceBase {

//	private final Logger logger = Logger.getLogger(ModelWithDbSchemaSynchronizer.class);
//
//	private final GmMetaModel model;
//	private final Set<DbTable> tables;
//	private final PersistenceGmSession session;
//	private final Set<DbTable> tablesToProcess;
//	private final Set<DbTable> processedTables;
//	private final GmTypeRegistry registry;
//	private final GmEntityType baseEntityType;
//	private BasicModelMetaDataEditor modelEditor = null;
//	private CmdResolver metaDataResolver;
//	private final boolean resolveRelationships;
//	private final boolean ignoreUnsupportedTables;
//	private int ignoredTableCount = 0;
//	private final Set<DbTable> unmappedTables = new HashSet<>();
//
//	public ModelWithDbSchemaSynchronizer(GmMetaModel model, Set<DbSchema> schemas, PersistenceGmSession session,
//			boolean resolveRelationship, boolean ignoreUnsupportedTables) {
//		this.model = model;
//		this.session = session;
//		this.tables = allTablesFrom(schemas);
//		this.tablesToProcess = new HashSet<DbTable>(tables);
//		this.processedTables = new HashSet<DbTable>();
//		this.registry = new GmTypeRegistry(model, session);
//		this.baseEntityType = getTypeFromSession(GenericEntity.T.getTypeSignature());
//		this.resolveRelationships = resolveRelationship;
//		this.ignoreUnsupportedTables = ignoreUnsupportedTables;
//
//	}
//
//	public DbSchemaResponse run() {
//
//		if (tablesToProcess.size() == 0) {
//			return createConfirmationResponse("No tables found to process!", Level.WARNING, DbSchemaResponse.T);
//		}
//
//		syncTables();
//		addMetaData();
//
//		addNotifications(Notifications.build().add().command().refresh("Refresh access").close().list());
//
//		if (ignoredTableCount > 0) {
//			notifyInfo("Synchronized " + processedTables.size() + " tables with model");
//			return createConfirmationResponse(
//					"Synchronized with Warnings! \n" + ignoredTableCount + " of " + processedTables.size()
//							+ " tables have been ignored because of unsupported primary key declaration.",
//					Level.WARNING, DbSchemaResponse.T);
//		} else if (unmappedTables.size() > 0) {
//			notifyInfo("Synchronized " + processedTables.size() + " tables with model");
//			return createConfirmationResponse("Synchronized with Warnings! \n" + unmappedTables.size() + " of "
//					+ processedTables.size()
//					+ " tables have been synced but marked as unmapped because of unsupported primary key declaration.",
//					Level.WARNING, DbSchemaResponse.T);
//		}
//
//		return createResponse("Synchronized " + processedTables.size() + " tables with model.", DbSchemaResponse.T);
//	}
//
//	private Set<DbTable> allTablesFrom(Set<DbSchema> dbSchemas) {
//		Set<DbTable> result = new HashSet<DbTable>();
//
//		for (DbSchema schema : dbSchemas) {
//			result.addAll(schema.getTables());
//		}
//
//		return result;
//	}
//
//	/**
//	 * Creates a {@link GmEntityType} for given {@link DbTable}, as long as such
//	 * entity isn't already present in the {@link PersistenceGmSession} (check is
//	 * based on type signature).The entity is initialized with properties for given
//	 * columns, if the column type is supported (Blobs and such are not supported,
//	 * see {@link DbTableProcessingUtils}). MetaData needed for creating the
//	 * Hibernate mapping are set as well ( {@link EntityMapping},
//	 * {@link PropertyMapping}).
//	 */
//	public void syncTables() {
//		while (!tablesToProcess.isEmpty()) {
//			DbTable table = tablesToProcess.iterator().next();
//
//			if (table.getPrimaryKeyColumn() == null) {
//
//				if (!CollectionTools.isEmpty(table.getPrimaryKeyColumns())) {
//
//					if (!CollectionTools.isEmpty(table.getPrimaryKeyColumns())) {
//						logger.info("Table has no dedicated primary key. Will be handled later.");
//						syncTable(table);
//					}
//				} else {
//					if (ignoreUnsupportedTables) {
//						notifyInfo("Ignored table " + table.getName()
//								+ " because of missing or unsupported (e.g.: multiple columns declared) primary key column declaration.");
//						ignoredTableCount++;
//					} else {
//						syncTable(table);
//						unmappedTables.add(table);
//						notifyInfo("Synchronized unsupported table: " + table.getName()
//								+ " which will be  configured to be unmapped.");
//					}
//				}
//
//			} else {
//				syncTable(table);
//				notifyInfo("Synchronized table: " + table.getName() + " of schema: " + table.getSchema());
//			}
//
//			tablesToProcess.remove(table);
//			processedTables.add(table);
//		}
//	}
//
//	public void addMetaData() {
//
//		initMetaDataEditor();
//		initMetaDataResolver();
//
//		for (Map.Entry<GmEntityType, DbTable> typeToTable : registry.getEntityTableMappings().entrySet()) {
//			GmEntityType type = typeToTable.getKey();
//			DbTable table = typeToTable.getValue();
//
//			if (unmappedTables.contains(table)) {
//
//				ensureHibernateUnmappedMetaData(type, table);
//
//			} else {
//				ensureHibernateMetaData(type, table);
//
//				// add mapping
//				if (table.getPrimaryKeyColumn() != null) {
//					ensurePrimaryKeyMapping(type, table.getPrimaryKeyColumn());
//				} else {
//					addCompositeMetadata(type, table);
//				}
//
//				for (GmProperty property : type.getProperties()) {
//					DbColumn column = registry.getColumnForProperty(property);
//					if (column == null) {
//						notifyInfo("No column registered for property: " + property.getName() + " of type: "
//								+ type.getTypeSignature());
//						continue;
//					}
//					ensureHibernateMetaData(property, column);
//				}
//			}
//		}
//	}
//
//	private void addCompositeMetadata(GmEntityType type, DbTable table) {
//		JpaCompositeId compMd = acquirePropertyMetaData(type, GenericEntity.id, JpaCompositeId.T);
//		compMd.setColumns(table.getPrimaryKeyColumns().stream().map(this::mapColumn).collect(Collectors.toList()));
//	}
//
//	// maps DB column to JPA column
//	private JpaColumn mapColumn(DbColumn dbCol) {
//		JpaColumn col = session.create(JpaColumn.T);
//		col.setName(dbCol.getName());
//		col.setType(getJpaTypeForColumn(dbCol));
//		return col;
//	}
//
//	private String getJpaTypeForColumn(DbColumn column) {
//		// resolve simple or entity type
//		SimpleType type = DbTableProcessingUtils.getGmTypeFromSqlType(column.getDataType(), column.getTypeName());
//		
//		// unresolved type? should not happen...
//		if (type == null) {
//			throw new IllegalArgumentException("Cannot determine type for column '" + column.getName() + "', with type '" + column.getTypeName()
//					+ "' and dataType '" + column.getDataType() + "'.");
//		}
//			
//		switch (type.getTypeCode()) {
//			case dateType:
//				return "timestamp";
//			case decimalType:
//				return "big_decimal";
//			default:
//				return type.getTypeSignature();
//		}
//	}
//
//	private void initMetaDataResolver() {
//		ModelOracle modelOracle = new BasicModelOracle(model);
//
//		ResolutionContextBuilder rcb = new ResolutionContextBuilder(modelOracle);
//		rcb.addStaticAspect(AccessAspect.class, session.getAccessId());
//		rcb.addStaticAspect(AccessTypeAspect.class, HibernateAccess.T.getTypeSignature());
//
//		this.metaDataResolver = new CmdResolverImpl(rcb.build());
//	}
//
//	private void initMetaDataEditor() {
//		this.modelEditor = BasicModelMetaDataEditor.create(model).withSession(session).done();
//	}
//
//	private void syncTable(DbTable table) {
//		GmEntityType type = acquireGmEntityFor(table);
//		registry.registerTableForType(type, table);
//
//		List<GmProperty> properties = type.getProperties();
//		properties.clear();
//
//		DbColumn pkColumn = table.getPrimaryKeyColumn();
//
//		for (DbColumn column : table.getColumns()) {
//
//			notifyColumn(column);
//			// process column if it's not the primary key.
//			if (column != pkColumn) {
//				GmProperty property = getGmPropertyFor(column, type);
//				if (property != null) {
//					properties.add(property);
//					registry.registerColumnForProperty(property, column);
//				} else {
//					notifyInfo("Ignored column: " + column.getName() + " of table: " + table.getName());
//				}
//			}
//
//		}
//	}
//
//	private void ensurePrimaryKeyMapping(GmEntityType type, DbColumn primaryKeyColumn) {
//		PropertyMapping mapping = acquirePropertyMapping(type, GenericEntity.id);
//		mapping.setColumnName(primaryKeyColumn.getName());
//		mapping.setAutoAssignable(true);
//
//		TypeSpecification typeSpecification = acquirePropertyMetaData(type, GenericEntity.id, TypeSpecification.T);
//		GmType idPropertyType = getTypeForColumn(primaryKeyColumn);
//		typeSpecification.setType(idPropertyType);
//	}
//
//	private void notifyColumn(DbColumn column) {
//		if (column.getReferencedTable() != null) {
//			notifyTable(column.getReferencedTable());
//		}
//	}
//
//	private void notifyTable(DbTable table) {
//		if (!processedTables.contains(table)) {
//			tablesToProcess.add(table);
//		}
//	}
//
//	private GmEntityType getTypeFromSession(String typeSignature) throws GmSessionException {
//		EntityQuery query = EntityQueryBuilder.from(GmEntityType.class).where().property("typeSignature")
//				.eq(typeSignature).done();
//		return session.query().entities(query).unique();
//	}
//
//	private GmProperty getGmPropertyFor(DbColumn column, GmEntityType owner) throws GmSessionException {
//		String propertyName = DbTableProcessingUtils.getPropertyName(column);
//		String globalId = createGlobalIdFor(owner, propertyName);
//
//		GmProperty property = registry.getProperty(column);
//		if (property != null) {
//			return property;
//		}
//
//		property = session.query().findEntity(globalId);
//		if (property != null) {
//			return property;
//		}
//
//		GmType propertyType = getTypeForColumn(column);
//
//		if (propertyType == null) {
//			return null;
//		}
//
//		property = createProperty(owner, column, propertyType);
//
//		if (property == null) {
//			return null;
//		}
//
//		registry.registerProperty(property);
//		return property;
//	}
//
//	private GmEntityType acquireGmEntityFor(DbTable table) throws GmSessionException {
//		String typeSignature = DbTableProcessingUtils.getEntitySignatureFrom(model, table);
//		GmEntityType type = registry.getGmEntityType(typeSignature);
//
//		if (type != null) {
//			return type;
//		}
//
//		type = getTypeFromSession(typeSignature);
//
//		if (type != null) {
//			GmMetaModel declaringModel = type.getDeclaringModel();
//			if (declaringModel != model) {
//				notifyInfo("A type with signature: " + type.getTypeSignature() + " already exists "
//						+ ((declaringModel != null) ? "for another model: " + declaringModel.getName()
//								: "with no declaringModel defined."));
//				type = null;
//				typeSignature = getNextFreeTypeSignature(typeSignature);
//				notifyInfo("Using typeSignature: " + typeSignature + " to create type representing table: "
//						+ table.getName());
//
//			} else {
//				notifyInfo("Reuse existing type: " + typeSignature);
//			}
//		}
//
//		if (type == null) {
//			type = registerNewTypeWithSignature(typeSignature);
//			addInheritanceInformation(type, baseEntityType);
//			type.setIsAbstract(false);
//			type.setDeclaringModel(model);
//			type.setGlobalId(createGlobalIdFor(typeSignature));
//		}
//
//		registry.registerEntity(type);
//		model.getTypes().add(type);
//
//		return type;
//	}
//
//	private String getNextFreeTypeSignature(String typeSignature) {
//		String nextTypeSignature = null;
//		for (int i = 0; i < 1000; i++) {
//			nextTypeSignature = typeSignature + i;
//			if (getTypeFromSession(nextTypeSignature) == null) {
//				return nextTypeSignature;
//			}
//		}
//		throw new IllegalArgumentException("No available typeSignature could be found for: " + typeSignature);
//	}
//
//	private GmProperty createProperty(GmEntityType owner, DbColumn column, GmType propertyType) {
//
//		GmProperty gp = session.create(GmProperty.T);
//		String propertyName = DbTableProcessingUtils.getPropertyName(column);
//		gp.setName(propertyName);
//		gp.setDeclaringType(owner);
//		gp.setType(propertyType);
//		gp.setGlobalId(createGlobalIdFor(owner, propertyName));
//
//		return gp;
//	}
//
//	private String createGlobalIdFor(GmEntityType owner, String propertyName) {
//		return "property:" + owner.getTypeSignature() + "/" + propertyName;
//	}
//
//	private String createGlobalIdFor(String typeSignature) {
//		return "type:" + typeSignature;
//	}
//
//	private GmType getTypeForColumn(DbColumn column) throws GmSessionException {
//		if (resolveRelationships && column.getReferencedTable() != null) {
//			return acquireGmEntityFor(column.getReferencedTable());
//		}
//
//		SimpleType type = DbTableProcessingUtils.getGmTypeFromSqlType(column.getDataType(), column.getTypeName());
//
//		if (type == null) {
//			return null;
//		}
//
//		return registry.getGmSimpleType(type.getTypeSignature());
//	}
//
//	private void addInheritanceInformation(GmEntityType subType, GmEntityType superType) {
//		subType.getSuperTypes().add(superType);
//	}
//
//	private GmEntityType registerNewTypeWithSignature(String typeSignature) {
//		GmEntityType gmEntityType = session.create(GmEntityType.T);
//		gmEntityType.setTypeSignature(typeSignature);
//		return gmEntityType;
//	}
//
//	// ##################################
//	// ## . . . . . MetaData . . . . . ##
//	// ##################################
//
//	private EntityMapping ensureHibernateUnmappedMetaData(GmEntityType type, DbTable table) {
//		EntityMapping mapping = ensureHibernateMetaData(type, table);
//		mapping.setMapToDb(false);
//		return mapping;
//	}
//
//	private EntityMapping ensureHibernateMetaData(GmEntityType type, DbTable table) {
//		EntityMapping mapping = acquireEntityMapping(type);
//		mapping.setTableName(table.getName());
//		mapping.setSchema(table.getSchema());
//		mapping.setCatalog(table.getCatalog());
//		return mapping;
//	}
//
//	private PropertyMapping ensureHibernateMetaData(GmProperty property, DbColumn column) {
//		GmEntityType declaringType = property.getDeclaringType();
//		PropertyMapping mapping = acquirePropertyMapping(declaringType, property.getName());
//		mapping.setColumnName(column.getName());
//		return mapping;
//	}
//
//	private EntityMapping acquireEntityMapping(GmEntityType type) {
//
//		EntityMapping md = null;
//		EntityRelatedMdDescriptor mdDescriptor = metaDataResolver.getMetaData().entityType(type).meta(EntityMapping.T).exclusiveExtended();
//		if (mdDescriptor == null || mdDescriptor.getResolvedValue() == null || mdDescriptor.getOwnerTypeInfo().addressedType() != type) {
//			md = session.create(EntityMapping.T);
//			modelEditor.onEntityType(type).addMetaData(md);
//		} else {
//			md = (EntityMapping) mdDescriptor.getResolvedValue();
//		}
//		return md;
//	}
//
//	private PropertyMapping acquirePropertyMapping(GmEntityType type, String property) {
//		return acquirePropertyMetaData(type, property, PropertyMapping.T);
//	}
//
//	private <T extends MetaData> T acquirePropertyMetaData(GmEntityType type, String property,
//			EntityType<T> metaDataType) {
//		T md = null;
//		EntityRelatedMdDescriptor mdDescriptor = metaDataResolver.getMetaData().entityType(type).property(property).meta(metaDataType).exclusiveExtended();
//		
//		if (mdDescriptor == null || mdDescriptor.getResolvedValue() == null || mdDescriptor.getOwnerTypeInfo().addressedType() != type) {
//			md = session.create(metaDataType);
//			modelEditor.onEntityType(type).addPropertyMetaData(property, md);
//		} else {
//			md = (T) mdDescriptor.getResolvedValue();
//		}
//		return md;
//	}

}
