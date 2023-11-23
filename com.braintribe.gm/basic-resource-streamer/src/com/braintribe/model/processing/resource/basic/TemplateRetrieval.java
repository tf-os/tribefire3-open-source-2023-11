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
package com.braintribe.model.processing.resource.basic;

import static com.braintribe.utils.lcd.NullSafe.nonNull;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.TemplateSource;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.utils.stream.MemoryThresholdBuffer;
import com.braintribe.utils.velocity.VelocityTools;

public class TemplateRetrieval extends AbstractDispatchingServiceProcessor<BinaryRetrievalRequest, BinaryRetrievalResponse> {

	private static final Logger log = Logger.getLogger(TemplateRetrieval.class);

	private VelocityEngine engine = null;

	private PersistenceGmSessionFactory requestSessionFactory;
	private PersistenceGmSessionFactory systemSessionFactory;

	@Required // only used for backwards compatibility with templates using 'streamContext'
	public void setRequestSessionFactory(PersistenceGmSessionFactory requestSessionFactory) {
		this.requestSessionFactory = requestSessionFactory;
	}

	@Required // only used for backwards compatibility with templates using 'streamContext'
	public void setSystemSessionFactory(PersistenceGmSessionFactory systemSessionFactory) {
		this.systemSessionFactory = systemSessionFactory;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<BinaryRetrievalRequest, BinaryRetrievalResponse> dispatching) {
		dispatching.register(StreamBinary.T, this::stream);
		dispatching.register(GetBinary.T, this::get);
	}

	public GetBinaryResponse get(ServiceRequestContext context, GetBinary request) {
		Resource resource = request.getResource();
		TemplateSource source = retrieveTemplateSource(resource);
		VelocityContext renderContext = createContext(context, source, request);

		GetBinaryResponse response = GetBinaryResponse.T.create();

		MemoryThresholdBuffer os = new MemoryThresholdBuffer();
		try {
			streamTemplate(renderContext, source.getTemplate(), os);
			try (InputStream in = os.openInputStream(true)) {
				Resource callResource = Resource.createTransient(() -> in);
				response.setResource(callResource);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} finally {
			os.delete();
		}

		return response;
	}

	public StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary request) {
		Resource resource = request.getResource();
		TemplateSource source = retrieveTemplateSource(resource);
		VelocityContext renderContext = createContext(context, source, request);

		streamTemplate(renderContext, source.getTemplate(), request.getCapture().openStream());

		StreamBinaryResponse response = StreamBinaryResponse.T.create();
		return response;
	}

	private void streamTemplate(VelocityContext renderContext, Resource template, OutputStream outputStream) {
		if (engine == null)
			engine = VelocityTools.newVelocityEngine();

		Reader templateReader = null;
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		try {
			templateReader = new BufferedReader(new InputStreamReader(template.openStream()));
			engine.evaluate(renderContext, writer, this.getClass().getName(), templateReader);
		} catch (Exception e) {
			throw new ServiceProcessorException("Error while rendering template of resource: " + template, e);
		} finally {
			close(template, templateReader);
			close(template, writer);
		}
	}

	protected static TemplateSource retrieveTemplateSource(Resource resource) {
		nonNull(resource, "resource");

		ResourceSource source = resource.getResourceSource();
		nonNull(source, "resource source");

		if (source instanceof TemplateSource)
			return (TemplateSource) source;
		else
			throw new IllegalStateException(TemplateRetrieval.class.getName() + " instances cannot handle " + source);
	}

	protected VelocityContext createContext(ServiceRequestContext context, TemplateSource source, BinaryRetrievalRequest request) {
		VelocityContext renderContext = new DeprecationLoggingVelocityContext(context, request);
		renderContext.put("source", source);
		renderContext.put("context", context);
		renderContext.put("streamContext", backwardsCompatibleContext(context, request)); //
		return renderContext;
	}

	protected void templateMerge(Template template, Context context, Writer writer) throws IOException {
		try {
			template.merge(context, writer);
		} catch (Exception e) {
			throw new IOException("failed to write to template: " + e.getMessage(), e);
		}
	}

	protected void close(Resource source, Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			log.warn("Could not close Template reader for resource: " + source, e);
		}
	}

	private Object backwardsCompatibleContext(ServiceRequestContext context, BinaryRetrievalRequest request) {
		return new CopyOf_BasicAccessAwareRequestContext<AccessRequest>(context, requestSessionFactory, systemSessionFactory, request);
	}

	class DeprecationLoggingVelocityContext extends VelocityContext {
		private final ServiceRequestContext context;
		private final BinaryRetrievalRequest request;

		public DeprecationLoggingVelocityContext(ServiceRequestContext context, BinaryRetrievalRequest request) {
			this.context = context;
			this.request = request;
		}

		@Override
		public Object get(String key) {
			if ("streamContext".equals(key))
				logDeprecatedUsage(context, request);

			return super.get(key);
		}
	}

	private final Map<String, String> loggedDomainIds = new ConcurrentHashMap<>();

	private void logDeprecatedUsage(ServiceRequestContext context, BinaryRetrievalRequest request) {
		String id = request.getResource().getId();
		if (id == null)
			return;

		String domainId = context.getDomainId();
		String key = domainId + "/" + id;

		if (loggedDomainIds.putIfAbsent(key, key) == null)
			log.warn("Variable 'streamContext' (AccessRequestContext) is accessed in a TemplateSource of Resource with id: [" + id + "]. "
					+ "'context' (ServiceRequestContext) should be used instead! DomainId: [" + domainId + "].");
	}
}
