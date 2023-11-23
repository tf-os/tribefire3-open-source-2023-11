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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class DbSchemaSynchronizer {

	private final PersistenceGmSession session;
	private final DatabaseConnectionPool connector;
	private Set<DbSchema> dbSchemas;

	public DbSchemaSynchronizer(PersistenceGmSession session, DatabaseConnectionPool connector, Set<DbSchema> dbSchemas) {
		this.session = session;
		this.connector = connector;
		this.dbSchemas = dbSchemas;
	}

	public void synchronize() throws GenericModelException {
		Set<DbSchema> existingDbSchemas = connector.getDbSchemas();

		SynchronizerRegistry synchronizerRegistry = new SynchronizerRegistry(existingDbSchemas);

		SynchronizerCloningContext cloningContext = new SynchronizerCloningContext(session, synchronizerRegistry);

		dbSchemas = cloneSchemas(cloningContext);

		connector.setDbSchemas(dbSchemas);
	}

	private Set<DbSchema> cloneSchemas(SynchronizerCloningContext cloningContext) {
		return (Set<DbSchema>) BaseType.INSTANCE.clone(cloningContext, dbSchemas, StrategyOnCriterionMatch.skip);
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

}
