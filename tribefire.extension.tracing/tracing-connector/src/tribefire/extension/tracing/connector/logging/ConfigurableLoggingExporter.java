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
package tribefire.extension.tracing.connector.logging;

import java.util.Collection;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Configurable logging reporter
 *
 */
public class ConfigurableLoggingExporter implements SpanExporter {

	private final static Logger logger = Logger.getLogger(ConfigurableLoggingExporter.class);

	private LogLevel logLevel;
	private boolean logAttributes;

	public ConfigurableLoggingExporter(final LogLevel logLevel, final boolean logAttributes) {
		this.logLevel = logLevel;
		this.logAttributes = logAttributes;
	}

	@Override
	public CompletableResultCode export(Collection<SpanData> spans) {
		if (spans == null || spans.isEmpty()) {
			return CompletableResultCode.ofSuccess();
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (SpanData span : spans) {
			
			sb.append(span.getName());
			sb.append(": (TraceId: ");
			sb.append(span.getTraceId());
			sb.append(") (SpanId: ");
			sb.append(span.getSpanId());
			sb.append(") (SpanContext: ");
			sb.append(span.getSpanContext());
			sb.append(") (");
			sb.append((span.getEndEpochNanos() - span.getStartEpochNanos()) / 1000 / 1000);
			sb.append("ms) ");
			if (logAttributes) {
				sb.append("attributes: ");
				sb.append(span.getAttributes());
			}
		}
		
		logger.log(logLevel, "Span reported: " + sb.toString());
		
		return CompletableResultCode.ofSuccess();
	}


	@Override
	public CompletableResultCode flush() {
		return CompletableResultCode.ofSuccess();
	}


	@Override
	public CompletableResultCode shutdown() {
		return CompletableResultCode.ofSuccess();
	}
}
