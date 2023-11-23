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
package com.braintribe.model.deployment.http.client;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;

/**
 * Connection settings are representing the options of org.apache.http.client.config.RequestConfig
 */
public interface GmHttpClient extends HttpClient {

	final EntityType<GmHttpClient> T = EntityTypes.T(GmHttpClient.class);

	String requestLogging = "requestLogging";
	String responseLogging = "responseLogging";

	String getProxy();
	void setProxy(String proxy);

	String getLocalAddress();
	void setLocalAddress(String address);

	String getCookieSpec();
	void setCookieSpec(String cookieSpec);

	Integer getConnectTimeout();
	void setConnectTimeout(Integer connectTimeout);

	Integer getConnectionRequestTimeout();
	void setConnectionRequestTimeout(Integer connectionRequestTimeout);

	Integer getSocketTimeout();
	void setSocketTimeout(Integer socketTimeout);

	@Initializer("50")
	Integer getMaxRedirects();
	void setMaxRedirects(Integer maxRedirects);

	@Initializer("true")
	boolean getAuthenticationEnabled();
	void setAuthenticationEnabled(boolean authenticationEnabled);

	@Initializer("true")
	boolean getRedirectsEnabled();
	void setRedirectsEnabled(boolean redirectsEnabled);

	@Initializer("true")
	boolean getRelativeRedirectsAllowed();
	void setRelativeRedirectsAllowed(boolean relativeRedirectsAllowed);

	@Initializer("true")
	boolean getContentCompressionEnabled();
	void setContentCompressionEnabled(boolean contentCompressionEnabled);

	@Name("Request Logging")
	@Description("Dynamic LogLevel for request logging")
	LogLevel getRequestLogging();
	void setRequestLogging(LogLevel requestLogging);

	@Name("Response Logging")
	@Description("Dynamic LogLevel for response logging")
	LogLevel getResponseLogging();
	void setResponseLogging(LogLevel responseLogging);

}
