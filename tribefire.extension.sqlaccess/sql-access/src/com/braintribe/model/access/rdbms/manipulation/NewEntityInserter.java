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
package com.braintribe.model.access.rdbms.manipulation;

import static com.braintribe.model.access.sql.tools.JdbcTools.tryDoPreparedStatement;
import static com.braintribe.utils.SysPrint.spOut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.access.rdbms.context.RdbmsManipulationContext;
import com.braintribe.model.access.sql.tools.SqlMappingExpert;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

/**
 * @author peter.gazdik
 */
public class NewEntityInserter {

	private final RdbmsManipulationContext context;
	private final EntityType<?> entityType;
	private final SqlMappingExpert sqlMappingExpert;

	private String sql;

	public NewEntityInserter(EntityType<?> entityType, RdbmsManipulationContext context) {
		this.entityType = entityType;
		this.context = context;
		sqlMappingExpert = context.getSqlMappingExpert();

		initialize();
	}

	private void initialize() {
		List<Property> directValueProperties = context.getDirectvaluePropertiesFor(entityType);

		String tableName = sqlMappingExpert.resolveDbTableName(entityType);
		String joinedColumnNames = directValueProperties.stream().map(p -> sqlMappingExpert.resolveDbColumnName(entityType, p))
				.collect(Collectors.joining(","));
		String valuesWildcards = String.join(",", Collections.nCopies(directValueProperties.size(), "?"));

		sql = context.getSqlDialect().insertIntoPsTemplate(tableName, joinedColumnNames, valuesWildcards);
	}

	public void doBulkInsert(Connection c, List<GenericEntity> newEntities) throws Exception {
		tryDoPreparedStatement(c, sql, ps -> insertEntities(ps, newEntities));
	}

	private void insertEntities(PreparedStatement ps, List<GenericEntity> newEntities) throws SQLException {
		for (GenericEntity newEntity : newEntities)
			insertEntity(ps, newEntity);
	}

	private void insertEntity(PreparedStatement ps, GenericEntity newEntity) throws SQLException {
		List<Property> directValueProperties = context.getDirectvaluePropertiesFor(entityType);

		int i = 1;
		for (Property p : directValueProperties)
			ps.setObject(i++, getPropertyValue(newEntity, p));

		spOut("Adding new entity: " + sql);

		ps.executeUpdate();
		// TODO log trace
	}

	private Object getPropertyValue(GenericEntity newEntity, Property p) {
		Object value = newEntity.read(p);

		if (value == null)
			return null;

		GenericModelType propertyType = p.getType();
		if (propertyType == BaseType.INSTANCE) {
			propertyType = GMF.getTypeReflection().getType(value);
		}

		switch (propertyType.getTypeCode()) {
			case enumType:
				return value.toString();
			case entityType:
				return ((GenericEntity) value).getId();
			default:
				return value;
		}
	}

}
