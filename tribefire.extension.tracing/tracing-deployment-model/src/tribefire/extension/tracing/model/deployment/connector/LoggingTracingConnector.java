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
package tribefire.extension.tracing.model.deployment.connector;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;

/**
 *
 */
public interface LoggingTracingConnector extends TracingConnector {

	final EntityType<LoggingTracingConnector> T = EntityTypes.T(LoggingTracingConnector.class);

	String logLevel = "logLevel";
	String logAttributes = "logAttributes";

	@Mandatory
	@Name("Log Level")
	@Description("Log Level for logging spans")
	@Initializer("enum(com.braintribe.model.logging.LogLevel,INFO)")
	LogLevel getLogLevel();
	void setLogLevel(LogLevel logLevel);
	
	@Name("Log Attributes")
	@Description("Indicates if attributes should be logged")
	@Initializer("true")
	boolean getLogAttributes();
	void setLogAttributes(boolean logAttributes);
}
