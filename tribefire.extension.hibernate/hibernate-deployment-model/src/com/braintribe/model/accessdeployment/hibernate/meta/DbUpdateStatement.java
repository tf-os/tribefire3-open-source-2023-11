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
package com.braintribe.model.accessdeployment.hibernate.meta;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * Defines schema modifications for the entity type. This can be used e.g. to create db indexes, creating sequences,...
 * 
 */
@SelectiveInformation("${#type_short} - ${expression}")
public interface DbUpdateStatement extends EntityTypeMetaData {

	EntityType<DbUpdateStatement> T = EntityTypes.T(DbUpdateStatement.class);

	String expression = "expression";
	String before = "before";
	String dialects = "dialects";
	String logLevelOnError = "logLevelOnError";
	String stopOnError = "stopOnError";

	@Mandatory
	@Description("Native SQL expression to be executed before or after the schema creation of Hibernate. The syntax and compatibility of the SQL expression (e.g. name restrictions of an index name,...) needs to be checked manually")
	String getExpression();
	void setExpression(String expression);

	@Mandatory
	@Description("If true the expression will be executed before the schema creation of Hibernate - otherwise after")
	boolean getBefore();
	void setBefore(boolean before);

	@Mandatory
	@Description("Log level for error message which is logged if stopOnError is false")
	@Initializer("DEBUG")
	LogLevel getLogLevelOnError();
	void setLogLevelOnError(LogLevel logLevelOnError);

	@Mandatory
	@Description("Stop the execution and throws an error if the SQL statement cannot be executed")
	boolean getStopOnError();
	void setStopOnError(boolean stopOnError);

	@Override
	@Initializer("false")
	boolean getInherited();

}
