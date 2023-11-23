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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;

public interface HibernateLogging extends GenericEntity {

	EntityType<HibernateLogging> T = EntityTypes.T(HibernateLogging.class);

	String logLevel = "logLevel";
	String logGMStatements = "logGMStatements";
	String logHQLStatements = "logHQLStatements";
	String logSQLStatements = "logSQLStatements";
	String enrichSQLParameters = "enrichSQLParameters";
	String logTimings = "logTimings";
	String logStatistics = "logStatistics";

	@Mandatory
	@Initializer("enum(com.braintribe.model.logging.LogLevel,DEBUG)")
	@Name("Log Level")
	@Description("Sets the logLevel to be used for logging Hibernate related information")
	@Priority(1.0)
	LogLevel getLogLevel();
	void setLogLevel(LogLevel logLevel);

	@Mandatory
	@Initializer("true")
	@Name("Log GM Statements")
	@Description("Log Generic Model (GM) statements")
	boolean getLogGMStatements();
	void setLogGMStatements(boolean logGMStatements);

	@Mandatory
	@Initializer("false")
	@Name("Log HQL Statements")
	@Description("Log Hibernate Query Language (HQL) statements")
	boolean getLogHQLStatements();
	void setLogHQLStatements(boolean logHQLStatements);

	@Mandatory
	@Initializer("true")
	@Name("Log SQL Statements")
	@Description("Log Structured Query Language (SQL) statements")
	boolean getLogSQLStatements();
	void setLogSQLStatements(boolean logSQLStatements);

	@Mandatory
	@Initializer("true")
	@Name("Enrich SQL Parameters")
	@Description("Enrich Structured Query Language (SQL) parameters")
	boolean getEnrichSQLParameters();
	void setEnrichSQLParameters(boolean enrichSQLParameters);

	@Mandatory
	@Initializer("false")
	@Name("Log Timings")
	@Description("Log Timings of Hibernate Execution")
	boolean getLogTimings();
	void setLogTimings(boolean logTimings);

	@Mandatory
	@Initializer("false")
	@Name("Log Statistics")
	@Description("Log Hibernate Statistics")
	boolean getLogStatistics();
	void setLogStatistics(boolean logStatistics);

}
