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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.braintribe.model.cortexapi.connection.DbSchemaResponse;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.dbs.DbSchemaProvider;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class DbSchemaSynchronizer extends ServiceBase {

	private DeployRegistry deployRegistry;
	private DatabaseConnectionPool connectionPool;
	private PersistenceGmSession session;
	
	public DbSchemaSynchronizer(DatabaseConnectionPool connectionPool, DeployRegistry deployRegistry, PersistenceGmSession session) {
		this.connectionPool = connectionPool;
		this.deployRegistry = deployRegistry;
		this.session = session;
	}
	
	public DbSchemaResponse run() {
		
		DataSource dataSource = deployRegistry.resolve(connectionPool, DatabaseConnectionPool.T);
		if (dataSource == null) {
			return createResponse("No deployed datasource found for ConnectionPool: "+connectionPool.getExternalId(), Level.ERROR, DbSchemaResponse.T);
		}
		
		DbSchemaProvider schemaProvider = new DbSchemaProvider();
		Set<DbSchema> currentSchemas = schemaProvider.apply(dataSource);
		
		
		synchronize(currentSchemas);
		
		// @formatter:off
		addNotifications(
				Notifications
					.build()
					.add()
						.command().refresh("Refresh ConnectionPool")
					.close()
					.list());
		// @formatter:on
		
		SyncSummary summary = createSyncSummary();
		
		
		if (summary.hasWarnings()){
			if (summary.syncedSchemas == 0) {
				return createConfirmationResponse("No db schemas have been synchronized.", Level.WARNING, DbSchemaResponse.T);
			}
			if (summary.syncedTables == 0) {
				return createConfirmationResponse("Warning: Synchronized "+summary.syncedSchemas+" db schemas with no tables.", Level.WARNING, DbSchemaResponse.T);
			}
			
			notifyInfo("Synchronized "+currentSchemas.size()+" schema(s) with "+summary.syncedTables+" tables.");	
			return createConfirmationResponse("Synchronized with Warnings! "
					+ "\n "+summary.tablesWithMissingPrimaryKey+" of "+summary.syncedTables+" tables are missing a primary key declaration."
					+ "\n "+summary.tablesWithMultiplePrimaryKeys+" of "+summary.syncedTables+" tables have multiple columns declared as primary key."
					, Level.WARNING, DbSchemaResponse.T);			
			
		} else {
			notifyInfo("Synchronized "+currentSchemas.size()+" schema(s) with "+summary.syncedTables+" tables.");
			return createResponse("Synchronized "+summary.syncedSchemas+" db schemas with "+summary.syncedTables+" tables.", Level.INFO, DbSchemaResponse.T);
		}
		
	}
	

	private SyncSummary createSyncSummary() {
		SyncSummary summary = new SyncSummary();
		
		for (DbSchema schema : connectionPool.getDbSchemas()) {
			summary.syncedSchemas++;
			for (DbTable table : schema.getTables()) {
				summary.syncedTables++;
				
				// TODO
//				if (table.getPrimaryKeyColumns() != null) {
//					if (table.getPrimaryKeyColumns().isEmpty()) {
//						summary.tablesWithMissingPrimaryKey++;
//					} else {
//						summary.tablesWithMultiplePrimaryKeys++;
//					}
//				} else 
				if (table.getPrimaryKeyColumn() == null) {

					if (getPrimaryKeyColumnCount(table) == 0) {
						summary.tablesWithMissingPrimaryKey++;
					} else {
						summary.tablesWithMultiplePrimaryKeys++;
					}
					
				}
			}
		}
		return summary;
	}

	private int getPrimaryKeyColumnCount(DbTable table) {
		int count = 0;
		for (DbColumn column : table.getColumns()) {
			if (column.getIsPrimaryKey()) {
				count++;
			}
		}
		return count;
	}

	public void synchronize(Set<DbSchema> dbSchemas) throws GenericModelException {
		Set<DbSchema> existingDbSchemas = connectionPool.getDbSchemas();
		SynchronizerRegistry synchronizerRegistry = new SynchronizerRegistry(existingDbSchemas);
		SynchronizerCloningContext cloningContext = new SynchronizerCloningContext(session, synchronizerRegistry);

		dbSchemas = cloneSchemas(cloningContext, dbSchemas);
		connectionPool.setDbSchemas(dbSchemas);
	}

	@SuppressWarnings("unchecked")
	private Set<DbSchema> cloneSchemas(SynchronizerCloningContext cloningContext, Set<DbSchema> dbSchemas) {
		return (Set<DbSchema>) GMF.getTypeReflection().getBaseType().clone(cloningContext, dbSchemas, StrategyOnCriterionMatch.skip);
	}

	protected static class SynchronizerRegistry {
		public Map<String, DbSchema> existingSchemas = new HashMap<String, DbSchema>();
		public Map<String, DbTable> existingTables = new HashMap<String, DbTable>();
		public Map<String, DbColumn> existingColumns = new HashMap<String, DbColumn>();

		public SynchronizerRegistry(Set<DbSchema> dbSchemas) {
			if (dbSchemas == null) {
				return;
			}

			for (DbSchema dbSchema : dbSchemas) {
				existingSchemas.put(getSchemaSignature(dbSchema), dbSchema);

				for (DbTable dbTable : dbSchema.getTables()) {
					existingTables.put(getTableSignature(dbTable), dbTable);

					for (DbColumn dbColumn : dbTable.getColumns()) {
						existingColumns.put(getColumnSignature(dbColumn), dbColumn);
					}
				}
			}
		}
	}

	protected static class SynchronizerCloningContext extends StandardCloningContext {
		private final PersistenceGmSession session;
		private final SynchronizerRegistry synchronizerRegistry;

		public SynchronizerCloningContext(PersistenceGmSession session, SynchronizerRegistry synchronizerRegistry) {
			this.session = session;
			this.synchronizerRegistry = synchronizerRegistry;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			if (instanceToBeCloned instanceof DbSchema) {
				DbSchema dbSchema = (DbSchema) instanceToBeCloned;
				DbSchema existingSchema = synchronizerRegistry.existingSchemas.get(getSchemaSignature(dbSchema));

				if (existingSchema != null)
					return existingSchema;

			} else if (instanceToBeCloned instanceof DbTable) {
				DbTable dbTable = (DbTable) instanceToBeCloned;
				DbTable existingTable = synchronizerRegistry.existingTables.get(getTableSignature(dbTable));

				if (existingTable != null)
					return existingTable;

			} else if (instanceToBeCloned instanceof DbColumn) {
				DbColumn dbColumn = (DbColumn) instanceToBeCloned;
				DbColumn existingColumn = synchronizerRegistry.existingColumns.get(getColumnSignature(dbColumn));

				if (existingColumn != null)
					return existingColumn;
			}

			return session.create(entityType);
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
				GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
			return !property.isIdentifier();
		}
	}

	protected static String getSchemaSignature(DbSchema dbSchema) {
		return dbSchema.getName();
	}

	protected static String getTableSignature(DbTable dbTable) {
		return dbTable.getName();
	}

	protected static String getColumnSignature(DbColumn dbColumn) {
		return dbColumn.getOwner().getName() + "#" + dbColumn.getName();
	}
	
	private class SyncSummary {
		int syncedSchemas = 0;
		int syncedTables = 0;
		int tablesWithMissingPrimaryKey = 0;
		int tablesWithMultiplePrimaryKeys = 0;
		
		boolean hasWarnings() {
			return syncedSchemas == 0 || syncedTables == 0 || tablesWithMissingPrimaryKey > 0 || tablesWithMultiplePrimaryKeys > 0;
		}
		
		
	}
	

}
