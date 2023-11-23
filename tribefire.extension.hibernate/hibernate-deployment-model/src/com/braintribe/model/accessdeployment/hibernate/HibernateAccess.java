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
package com.braintribe.model.accessdeployment.hibernate;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author gunther.schenk
 */
/* TODO: It would be great to extract this to hibernate-deployment-model. That's currently not possible as it would lead to a cyclic group dependency,
 * because tf.cortex references hibernate, and hibernate would have to reference tf.cortex, as HibernateComponent references DatabaseConnectionPool
 * from tf.cortex:database-deployment-model.
 * 
 * When extracting hibernate stuff, tf.cortex should not know anything about it and hibernate should be tribefire.extension.hibernate. */
public interface HibernateAccess extends IncrementalAccess, HibernateComponent {

	EntityType<HibernateAccess> T = EntityTypes.T(HibernateAccess.class);

	String durationWarningThreshold = "durationWarningThreshold";
	String objectNamePrefix = "objectNamePrefix";
	String tableNamePrefix = "tableNamePrefix";
	String foreignKeyNamePrefix = "foreignKeyNamePrefix";
	String uniqueKeyNamePrefix = "uniqueKeyNamePrefix";
	String indexNamePrefix = "indexNamePrefix";
	String schemaUpdate = "schemaUpdate";
	String deadlockRetryLimit = "deadlockRetryLimit";
	String logging = "logging";
	String schemaUpdateOnlyOnModelChange = "schemaUpdateOnlyOnModelChange";

	/** If a query exceeds this number of milliseconds, a warning entry is logged. */
	Long getDurationWarningThreshold();
	void setDurationWarningThreshold(Long durationWarningThreshold);

	String getObjectNamePrefix();
	void setObjectNamePrefix(String objectNamePrefix);

	String getTableNamePrefix();
	void setTableNamePrefix(String tableNamePrefix);

	String getForeignKeyNamePrefix();
	void setForeignKeyNamePrefix(String foreignKeyNamePrefix);

	String getUniqueKeyNamePrefix();
	void setUniqueKeyNamePrefix(String uniqueKeyNamePrefix);

	@Description("Defines the prefix for each index name")
	String getIndexNamePrefix();
	void setIndexNamePrefix(String indexNamePrefix);

	@Description("Maximum number of times 'applyManipulation' is attempted in case it fails due to a deadlock.")
	Integer getDeadlockRetryLimit();
	void setDeadlockRetryLimit(Integer deadlockRetryLimit);

	@Name("Logging")
	@Description("Manual Logging configuration - if set Hibernate related information will be logged")
	HibernateLogging getLogging();
	void setLogging(HibernateLogging logging);

	@Name("Schema Update on Model Change")
	@Description("When true, the DB schema will only be checked if a model was changed."
			+ " Model checksums is stored in the TF_SCHEMA_UPDATE_TMP table."
			+ " Set this to false if, e.g. the Access must not create this extra table.")
	@Initializer("true")
	Boolean getSchemaUpdateOnlyOnModelChange();
	void setSchemaUpdateOnlyOnModelChange(Boolean schemaUpdateOnlyOnModelChange);
}
