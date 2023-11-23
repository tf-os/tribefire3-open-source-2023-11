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
package com.braintribe.model.processing.rpc.commons.impl.logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.processing.service.commons.NoOpServiceRequestSummaryLogger;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.HasServiceRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.CompiledFormatter;

public class RpcServiceRequestSummaryLogger implements ServiceRequestSummaryLogger {

	private static Logger logger = Logger.getLogger(RpcServiceRequestSummaryLogger.class);
	
	private final Logger log;
	private final Map<String, StopWatch> watches = new LinkedHashMap<>();
	private int level = 0;

	private int maxDesc;

	private final String totalDescription = "Full request";

	private static final String nl = System.lineSeparator();
	private static final String l = "-------------------------------------------------------------------";

	private static CompiledFormatter numberFormatter = new CompiledFormatter("%6d");

	private final Function<ServiceRequest, CmdResolver> metaDataResolverProvider;

	private final String requestedEndpoint;

	private final String requestorSessionId;

	private final String requestorUserName;

	private final String requestorAddress;

	private final String requestorId;
	
	private static class StopWatch {

		private final long time;
		private long duration = -1L;

		public StopWatch() {
			time = System.currentTimeMillis();
		}

		public long stop() {
			this.duration = System.currentTimeMillis() - time;
			return this.duration;
		}

		public long getDuration() {
			return duration;
		}
	}

	public static ServiceRequestSummaryLogger getInstance(Logger log, AttributeContext serviceRequestContext, Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {

		ServiceRequestSummaryLogger summaryLogger = null;

		if (log.isDebugEnabled()) {
			summaryLogger = new RpcServiceRequestSummaryLogger(log, serviceRequestContext, metaDataResolverProvider);
		} else {
			summaryLogger = NoOpServiceRequestSummaryLogger.INSTANCE;
		}

		return summaryLogger;

	}
	
	public static ServiceRequestSummaryLogger getInstance(Logger log, Function<ServiceRequest, CmdResolver> metaDataResolverProvider, String requestedEndpoint, String requestorSessionId, String requestorUserName, String requestorAddress, String requestorId) {
		
		ServiceRequestSummaryLogger summaryLogger = null;
		
		if (log.isDebugEnabled()) {
			summaryLogger = new RpcServiceRequestSummaryLogger(log, metaDataResolverProvider, requestedEndpoint, requestorSessionId, requestorUserName, requestorAddress, requestorId);
		} else {
			summaryLogger = NoOpServiceRequestSummaryLogger.INSTANCE;
		}
		
		return summaryLogger;
		
	}

	RpcServiceRequestSummaryLogger(Logger log, Function<ServiceRequest, CmdResolver> metaDataResolverProvider, String requestedEndpoint, String requestorSessionId, String requestorUserName, String requestorAddress, String requestorId) {
		this.log = log;
		this.metaDataResolverProvider = metaDataResolverProvider;
		this.requestedEndpoint = requestedEndpoint;
		this.requestorSessionId = requestorSessionId;
		this.requestorUserName = requestorUserName;
		this.requestorAddress = requestorAddress;
		this.requestorId = requestorId;
		
		startTimer(totalDescription);
	}
	
	RpcServiceRequestSummaryLogger(Logger log, AttributeContext requestContext, Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {
		this.log = log;
		this.metaDataResolverProvider = metaDataResolverProvider;
		this.requestedEndpoint = requestContext.findAttribute(RequestedEndpointAspect.class).orElse(null);
		this.requestorSessionId = requestContext.findAttribute(RequestorSessionIdAspect.class).orElse(null);
		this.requestorUserName = requestContext.findAttribute(RequestorUserNameAspect.class).orElse(null);
		this.requestorAddress = requestContext.findAttribute(RequestorAddressAspect.class).orElse(null);
		this.requestorId = requestContext.findAttribute(RequestorIdAspect.class).orElse(null);

		startTimer(totalDescription);
	}

	@Override
	public void startTimer(String partialDescription) {
		if (partialDescription == null)
			return;
		partialDescription = levelTab(partialDescription);
		if (partialDescription.length() > maxDesc) {
			maxDesc = partialDescription.length();
		}
		watches.put(partialDescription, new StopWatch());
		level++;
	}

	@Override
	public void stopTimer(String partialDescription) {
		if (partialDescription == null)
			return;
		level--;
		StopWatch stopWatch = watches.get(levelTab(partialDescription));
		if (stopWatch != null) {
			stopWatch.stop();
		}
	}

	@Override
	public void stopTimer() {
		stopTimer(totalDescription);
	}

	@Override
	public String oneLineSummary(ServiceRequest request) {

		StringBuilder sb = new StringBuilder(2048);

		if (requestedEndpoint != null) {
			sb.append("URL: ").append(requestedEndpoint);
		}

		if (request != null) {
			if (sb.length() > 0) {
				sb.append(", ");
			}

			sb.append("Denotation: ").append(request.type().getTypeSignature());
			if (request instanceof DispatchableRequest) {
				sb.append(", Service: ").append(((DispatchableRequest) request).getServiceId());
			}
			if (request instanceof HasServiceRequest) {
				HasServiceRequest jsr = (HasServiceRequest) request;
				ServiceRequest payloadServiceRequest = jsr.getServiceRequest();
				
				sb.append(", Payload Request: ").append(payloadServiceRequest.type().getTypeSignature());					
			}

			Map<String, Object> metaData = request.getMetaData();
			if (metaData != null && !metaData.isEmpty()) {

				boolean printMetadata = false;
				if (metaData.size() == 1) {
					Object sessionIdValue = metaData.get("sessionId");
					if ((sessionIdValue == null) || (sessionIdValue.toString().isEmpty())) {
						printMetadata = false;
					} else {
						if (requestorSessionId != null) {
							printMetadata = false;
						} else {
							printMetadata = true;
						}
					}
				} else {
					printMetadata = true;
				}

				if (printMetadata) {
					sb.append(", (Metadata: ");
					boolean first = true;
					for (Map.Entry<String, Object> entry : metaData.entrySet()) {
						if (entry.getKey().equals("sessionId") && requestorSessionId != null) {
							continue;
						}
						if (!first) {
							sb.append(", ");
						} else {
							first = false;
						}
						sb.append(entry.getKey()).append("=").append(entry.getValue());
					}
					sb.append(")");
				}
			}
			
			if (requestorSessionId != null) {
				sb.append(", Session: " + requestorSessionId);
			}
			if (requestorUserName != null) {
				sb.append(", User: " + requestorUserName);
			}
			if (requestorAddress != null) {
				sb.append(", IP: " + requestorAddress);
			}
			if (requestorId != null) {
				sb.append("Client: " + requestorId);
			}
		}

		StopWatch stopWatch = watches.get(totalDescription);
		if (stopWatch != null && stopWatch.duration >= 0) {
			sb.append(" [");
			sb.append(totalDescription);
			sb.append(": ");
			sb.append(String.format("%d", stopWatch.getDuration()));
			sb.append("ms]");
		}

		return sb.toString();
	}

	@Override
	public String summary(Object caller, ServiceRequest request) {

		StringBuilder sb = new StringBuilder(2048);

		sb.append(caller.getClass().getSimpleName() + " request summary").append(nl);
		sb.append(l).append(nl);

		if (requestedEndpoint != null) {
			sb.append("  URL        : ").append(requestedEndpoint).append(nl);
			sb.append(l).append(nl);
		}

		if (request != null) {

			CmdResolver resolver = null;
			if (this.metaDataResolverProvider != null) {
				try {
					resolver = this.metaDataResolverProvider.apply(request);
				} catch(Exception e) {
					//Ok, we trace this instead of logging on error level because it may happen ALL THE TIME. This would flood the logfile
					logger.trace(() -> "Could not get a resolver for request: "+request);
				}
			}

			EntityType<?> type = request.entityType();
			sb.append("  Denotation : ").append(type.getTypeSignature()).append(nl);
			if (request instanceof DispatchableRequest) {
				sb.append("  Service  : ").append(((DispatchableRequest) request).getServiceId());
			}
			sb.append("  Properties : ").append(nl);
			
			List<Property> properties = type.getProperties();
			for (Property property : properties) {					
				if (property.getDeclaringType() != ServiceRequest.T) {
					Object propertyValue = property.get(request);

					final boolean confidential; 
					
					if (resolver != null) {
						//boolean confidential = resolver.getMetaData().property(property).is(Confidential.T);
						confidential = resolver.getMetaData().lenient(true).property(property).is(Confidential.T);
					}
					else {
						confidential = property.isConfidential();
					}
					
					if (confidential) {
						propertyValue = "****";
					}
					
					sb.append("  ").append(property.getName()).append(" : ").append(propertyValue != null ? propertyValue.toString() : "null").append(nl);
				}
			}

			if (request.getMetaData() != null && !request.getMetaData().isEmpty()) {
				sb.append("  Metadata   : ").append(nl);
				int maxMetaData = 0;
				for (Map.Entry<String, Object> entry : request.getMetaData().entrySet()) {
					if (entry.getKey().length() > maxMetaData) {
						maxMetaData = entry.getKey().length();
					}
				}
				for (Map.Entry<String, Object> entry : request.getMetaData().entrySet()) {
					sb.append("  [ ").append(pad(entry.getKey(), maxMetaData)).append(" ] : ").append(entry.getValue()).append(nl);
				}
			}
			sb.append(l).append(nl);
		}

		if (requestorUserName != null || requestorId != null || requestorSessionId != null || requestorAddress != null) {
			if (requestorUserName != null) {
				sb.append("  User       : ").append(requestorUserName).append(nl);
			}
			if (requestorId != null) {
				sb.append("  Client     : ").append(requestorId).append(nl);
			}
			if (requestorSessionId != null) {
				sb.append("  Session    : ").append(requestorSessionId).append(nl);
			}
			if (requestorAddress != null) {
				sb.append("  IP         : ").append(requestorAddress).append(nl);
			}
			sb.append(l).append(nl);
		}

		for (Map.Entry<String, StopWatch> te : watches.entrySet()) {
			try {
				sb.append("  ").append(padDesc(te.getKey())).append(" : ").append(numberFormatter.format(te.getValue().getDuration())).append(" ms").append(nl);
			} catch (Exception e) {
				logger.debug(() -> "Error while applying number formatter on "+te, e);
			}
		}
		sb.append(l);

		return sb.toString();

	}

	@Override
	public void logOneLine(String prefix, ServiceRequest request) {
		String summary = oneLineSummary(request);
		if (summary.contains("com.braintribe.model.leadership.service.lifesign.PingTimestamp")) {
			log.trace(() -> prefix + ": " + summary);	
		} else {
			log.debug(() -> prefix + ": " + summary);
		}
		
	}

	@Override
	public void log(Object caller, ServiceRequest request) {
		stopTimer();
		log.trace(() -> summary(caller, request));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private String levelTab(String description) {
		return repeat("    ", level).concat(description);
	}

	private static String pad(String desc, int max) {
		return desc + repeat(' ', max - desc.length());
	}

	private String padDesc(String desc) {
		return desc + repeat(' ', maxDesc - desc.length());
	}

	private static String repeat(char c, int n) {
		if (n < 1) {
			return "";
		}
		final char[] buf = new char[n];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = c;
		}
		return new String(buf);
	}
	private static String repeat(String text, int n) {
		if (n < 1) {
			return "";
		}
		StringBuilder sb = new StringBuilder(text.length()*n);
		for (int i=0; i<n; ++i) {
			sb.append(text);
		}
		return sb.toString();
	}
}
