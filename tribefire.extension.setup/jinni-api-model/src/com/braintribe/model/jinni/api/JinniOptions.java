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
package com.braintribe.model.jinni.api;

import java.util.Map;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author Christina Wilpernig, Dirk Scheffler
 */
@Description("This type models typesafe command line options for Jinni.")
@Alias("options")
public interface JinniOptions extends MalaclypseOptions, JinniInternalOptions {

	EntityType<JinniOptions> T = EntityTypes.T(JinniOptions.class);

	@Description("Store the passed command as an alias with this parameter's name and suppress its execution.")
	@Alias("a")
	String getAlias();
	void setAlias(String alias);

	@Description("Print more details and exception stacktraces in case of errors. What exactly si printed depends on the actual request processor implementation.")
	@Alias("v")
	boolean getVerbose();
	void setVerbose(boolean verbose);

	@Description("Protocol starts with an output of the Jinni version.")
	boolean getPrintVersion();
	void setPrintVersion(boolean printVersion);

	@Description("Route the log output to a channel defined by the given value. " + //
			"Possible values are stdout, stderr and none, and any other value is interpreted as a filepath.")
	@Initializer("'none'")
	@Alias("l")
	String getLog();
	void setLog(String log);

	@Description("Route the response of the request to a channel defined by this value. " + //
			"The response is serialized in a format defined by 'responseMimeType'. " + //
			"Possible values are stdout, stderr and none, and any other value is interpreted as a filepath.")
	@Initializer("'none'")
	@Alias("r")
	String getResponse();
	void setResponse(String response);

	@Description("The mimetype of the serialization format for the response output. Possible values: text/yaml, application/x-yaml, text/xml, application/json, gm/bin, gm/jse, gm/man")
	@Initializer("'application/yaml'")
	@Mandatory
	String getResponseMimeType();
	void setResponseMimeType(String responseMimeType);

	@Description("Route the protocol of the request processing to a channel defined by the given value. " + //
			"The output is styled in case the channel supports ANSI escape sequences. " + //
			"Possible values are stdout, stderr and none, and any other value is interpreted as a filepath.")
	@Initializer("'stdout'")
	@Alias("p")
	String getProtocol();
	void setProtocol(String protocol);

	@Description("Activates the ANSI escaped sequence styled output of the protocol.")
	Boolean getColored();
	void setColored(Boolean ansi);

	@Description("The charset being used to write the protocol.")
	String getProtocolCharset();
	void setProtocolCharset(String protocolCharset);

	@Description("Print the executing command including all its defined properties in yaml format.")
	@Alias("e")
	boolean getEchoCommand();
	void setEchoCommand(boolean echoCommand);

	@Description("Suppresses recording of the executed request within history (e.g. because of its confidentiality).")
	boolean getSuppressHistory();
	void setSuppressHistory(boolean suppressHistory);

	@Description("Configures environment variables for the Virtual Environment which overrides System.env variables.")
	@Alias("env")
	Map<String, String> getEnvironmentVariables();
	void setEnvironmentVariables(Map<String, String> environmentVariables);
}
