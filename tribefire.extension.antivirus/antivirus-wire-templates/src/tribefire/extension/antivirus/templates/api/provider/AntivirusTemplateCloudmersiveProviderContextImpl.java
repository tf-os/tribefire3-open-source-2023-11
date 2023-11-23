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
package tribefire.extension.antivirus.templates.api.provider;

import com.braintribe.model.logging.LogLevel;

import tribefire.extension.antivirus.templates.api.AntivirusTemplateContextImpl;

/**
 *
 */
public class AntivirusTemplateCloudmersiveProviderContextImpl extends AntivirusTemplateContextImpl
		implements AntivirusTemplateCloudmersiveProviderContext, AntivirusTemplateCloudmersiveProviderContextBuilder {

	private LogLevel logLevel;

	private boolean logTags;

	@Override
	public AntivirusTemplateCloudmersiveProviderContextBuilder setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
		return this;
	}

	@Override
	public AntivirusTemplateCloudmersiveProviderContextBuilder setLogTags(boolean logTags) {
		this.logTags = logTags;
		return this;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public boolean getLogTags() {
		return logTags;
	}

	@Override
	public AntivirusTemplateCloudmersiveProviderContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "AntivirusTemplateLoggingConnectorContextImpl [logLevel=" + logLevel + ", logTags=" + logTags + "]";
	}

}
