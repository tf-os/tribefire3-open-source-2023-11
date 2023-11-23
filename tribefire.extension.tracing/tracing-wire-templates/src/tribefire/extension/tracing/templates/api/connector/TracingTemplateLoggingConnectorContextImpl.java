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
package tribefire.extension.tracing.templates.api.connector;

import com.braintribe.model.logging.LogLevel;

/**
 *
 */
public class TracingTemplateLoggingConnectorContextImpl extends TracingTemplateConnectorContextImpl
		implements TracingTemplateLoggingConnectorContext, TracingTemplateLoggingConnectorContextBuilder {

	private LogLevel logLevel;

	private boolean logAttributes;

	@Override
	public TracingTemplateLoggingConnectorContextBuilder setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
		return this;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}
	
	@Override
	public TracingTemplateLoggingConnectorContextBuilder setLogAttributes(boolean logAttributes) {
		this.logAttributes = logAttributes;
		return this;
	}
	
	@Override
	public boolean getLogAttributes() {
		return logAttributes;
	}

	@Override
	public TracingTemplateLoggingConnectorContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "TracingTemplateLoggingConnectorContextImpl [logLevel=" + logLevel + ", logAttributes=" + logAttributes + "]";
	}

}
