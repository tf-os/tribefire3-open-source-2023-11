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
package tribefire.extension.tracing.templates.api.connector.reporterconfiguration;

import tribefire.extension.tracing.templates.api.connector.senderconfiguration.TracingTemplateSenderConfigurationContext;

public class TracingTemplateReporterConfigurationContextImpl
		implements TracingTemplateReporterConfigurationContext, TracingTemplateReporterConfigurationContextBuilder {

	private int flushInterval;

	private int maxQueueSize;

	private boolean logSpans;

	private TracingTemplateSenderConfigurationContext senderConfigurationContext;

	@Override
	public TracingTemplateReporterConfigurationContextBuilder setFlushInterval(int flushInterval) {
		this.flushInterval = flushInterval;
		return this;
	}

	@Override
	public TracingTemplateReporterConfigurationContextBuilder setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		return this;
	}

	@Override
	public TracingTemplateReporterConfigurationContextBuilder setSenderConfigurationContext(
			TracingTemplateSenderConfigurationContext senderConfigurationContext) {
		this.senderConfigurationContext = senderConfigurationContext;
		return this;
	}

	@Override
	public int getFlushInterval() {
		return flushInterval;
	}

	@Override
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	@Override
	public TracingTemplateSenderConfigurationContext getSenderConfigurationContext() {
		return senderConfigurationContext;
	}

	@Override
	public TracingTemplateReporterConfigurationContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "TracingTemplateReporterConfigurationContextImpl [flushInterval=" + flushInterval + ", maxQueueSize=" + maxQueueSize + ", logSpans="
				+ logSpans + ", senderConfigurationContext=" + senderConfigurationContext + "]";
	}

}
