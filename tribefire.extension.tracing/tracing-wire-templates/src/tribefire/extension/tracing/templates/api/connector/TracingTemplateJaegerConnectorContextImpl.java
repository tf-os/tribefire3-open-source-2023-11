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

import tribefire.extension.tracing.templates.api.connector.reporterconfiguration.TracingTemplateReporterConfigurationContext;
import tribefire.extension.tracing.templates.api.connector.samplerconfiguration.TracingTemplateSamplerConfigurationContext;

/**
 *
 */
public class TracingTemplateJaegerConnectorContextImpl extends TracingTemplateConnectorContextImpl
		implements TracingTemplateJaegerConnectorContext, TracingTemplateJaegerConnectorContextBuilder {

	private TracingTemplateReporterConfigurationContext reporterConfigurationContext;
	private TracingTemplateSamplerConfigurationContext samplerConfigurationContext;

	@Override
	public TracingTemplateJaegerConnectorContextBuilder setSamplerConfigurationContext(
			TracingTemplateSamplerConfigurationContext samplerConfigurationContext) {
		this.samplerConfigurationContext = samplerConfigurationContext;
		return this;
	}

	@Override
	public TracingTemplateJaegerConnectorContextBuilder setReporterConfigurationContext(
			TracingTemplateReporterConfigurationContext reporterConfigurationContext) {
		this.reporterConfigurationContext = reporterConfigurationContext;
		return this;
	}

	@Override
	public TracingTemplateSamplerConfigurationContext getSamplerConfigurationContext() {
		return samplerConfigurationContext;
	}

	@Override
	public TracingTemplateReporterConfigurationContext getReporterConfigurationContext() {
		return reporterConfigurationContext;
	}

	@Override
	public TracingTemplateJaegerConnectorContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "TracingTemplateJaegerConnectorContextImpl [reporterConfigurationContext=" + reporterConfigurationContext
				+ ", samplerConfigurationContext=" + samplerConfigurationContext + "]";
	}

}
