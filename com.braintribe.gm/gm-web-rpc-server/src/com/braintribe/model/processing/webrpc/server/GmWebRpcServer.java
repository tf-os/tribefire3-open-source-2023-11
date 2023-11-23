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
package com.braintribe.model.processing.webrpc.server;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.api.RpcHeaders;
import com.braintribe.model.processing.rpc.commons.impl.RpcUnmarshallingStreamManagement;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcExceptionContextualizer;
import com.braintribe.model.processing.rpc.commons.impl.logging.RpcServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.ParentAttributeContextAspect;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorIdAspect;
import com.braintribe.model.processing.service.api.aspect.SummaryLoggerAspect;
import com.braintribe.model.processing.service.common.ServiceResults;
import com.braintribe.model.processing.service.commons.NoOpServiceRequestSummaryLogger;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.processing.webrpc.server.multipart.DeferredInputStreamSupplier;
import com.braintribe.model.processing.webrpc.server.multipart.MultipartRequestUnmarshaller;
import com.braintribe.model.processing.webrpc.server.multipart.PartAcquiring;
import com.braintribe.model.processing.webrpc.server.multipart.PartOutputStreamProvider;
import com.braintribe.model.processing.webrpc.server.multipart.PartOutputStreamProvider.State;
import com.braintribe.model.processing.webrpc.server.multipart.TemporaryFileInputStreamSupplier;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.web.multipart.api.MultipartFormat;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.BasicMultipartFormat;
import com.braintribe.web.multipart.impl.MultipartSubFormat;
import com.braintribe.web.multipart.impl.Multiparts;

public class GmWebRpcServer extends HttpServlet implements InitializationAware {

	private static final MultipartFormat RPC1_DEFAULT_MULTIPART_FORMAT = new BasicMultipartFormat("multipart/form-data", MultipartSubFormat.formData);
	private static final List<MultipartSubFormat> SUPPORTED_MULTIPART_FORMATS = Arrays.asList(MultipartSubFormat.formData,
			MultipartSubFormat.chunked);
	// configurable
	private MarshallerRegistry marshallerRegistry;
	private String defaultMarshallerMimeType = "gm/xml";
	private Consumer<Set<String>> requiredTypesReceiver = new ValidatingRequiredTypesReceiver();
	private boolean processResourceRequestPartsAsynchronously;
	private ExecutorService resourceRequestPartExecutor;
	private Evaluator<ServiceRequest> evaluator;

	// constants
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GmWebRpcServer.class);
	protected static final String MDC_SESSIONID = "sessionId";

	private Function<ServiceRequest, CmdResolver> metaDataResolverProvider;
	private int partTransferBufferSize = IOTools.SIZE_32K;
	private StreamPipeFactory streamPipeFactory;

	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setPartTransferBufferSize(int partTransferBufferSize) {
		this.partTransferBufferSize = partTransferBufferSize;
	}

	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Configurable
	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Configurable
	public void setDefaultMarshallerMimeType(String defaultMarshallerMimeType) {
		Objects.requireNonNull(defaultMarshallerMimeType, "defaultMarshallerMimeType cannot be set to null");
		this.defaultMarshallerMimeType = defaultMarshallerMimeType;
	}

	/**
	 * @deprecated Cryptographic capabilities were removed from the RPC layer. This setter is now obsolete and will be
	 *             removed in future version.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	@Configurable
	public void setCryptoContext(com.braintribe.model.processing.rpc.commons.api.crypto.RpcServerCryptoContext cryptoContext) {
		// no-op
	}

	/**
	 * @deprecated this function has no influence as the parts are always written to the disc for reopening
	 */
	@Configurable
	@Deprecated
	public void setFullPartsBackup(@SuppressWarnings("unused") boolean fullPartsBackup) {
		// noop
	}

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Configurable
	public void setProcessResourceRequestPartsAsynchronously(boolean processResourceRequestPartsAsynchronously) {
		this.processResourceRequestPartsAsynchronously = processResourceRequestPartsAsynchronously;
	}

	@Configurable
	public void setResourceRequestPartExecutor(ExecutorService resourceRequestPartExecutor) {
		this.resourceRequestPartExecutor = resourceRequestPartExecutor;
	}

	@Override
	public void postConstruct() {
		if (processResourceRequestPartsAsynchronously && resourceRequestPartExecutor == null) {
			throw new IllegalStateException("resourceRequestPartExecutor must be configured when processResourceRequestPartsAsynchronously is true");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.println("GM web RPC functional interface");
	}

	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {

		StopWatch stopWatch = new StopWatch();

		if (logger.isTraceEnabled()) {
			logger.trace("Processing POST request " + httpRequest);
		}

		// set headers to avoid any caching
		httpResponse.setHeader("Pragma", "no-cache");
		httpResponse.setHeader("Cache-Control", "private, no-cache, max-age=0");

		AttributeContext attributeContext = initializeContext(httpRequest);
		ServiceRequestSummaryLogger summaryLogger = attributeContext.findAttribute(SummaryLoggerAspect.class)
				.orElse(NoOpServiceRequestSummaryLogger.INSTANCE);

		MarshallerRegistryEntry marshallerEntry;
		// final PartAcquiring partAcquiring = new PartAcquiring(httpResponse, getMultipartMimetypeAcceptance(httpRequest));

		ServiceRequest request = null;
		MultipartRequestUnmarshaller multipartRequestUnmarshaller = null;

		try (final PartAcquiring partAcquiring = new PartAcquiring(httpResponse, getMultipartMimetypeAcceptance(httpRequest), streamPipeFactory)) {

			ResponseMarshaller responseMarshaller = new ResponseMarshaller(summaryLogger, httpRequest, partAcquiring);
			SequentialFormDataReader multiparts = null;

			try {

				httpResponse.addHeader(RpcHeaders.rpcBody.getHeaderName(), "1");

				// check if multipart
				MultipartFormat multipartFormat = Multiparts.parseFormat(httpRequest.getContentType());

				boolean multipart = multipartFormat.getSubFormat() != MultipartSubFormat.none;

				RpcUnmarshallingStreamManagement streamManagement = new RpcUnmarshallingStreamManagement("gm-web-rpc", streamPipeFactory);

				InputStream inputStream = httpRequest.getInputStream();

				if (multipart) {
					multipartRequestUnmarshaller = createMultipartRequestUnmarshaller();
					multiparts = Multiparts //
							.buildFormDataReader(inputStream) //
							.subFormat(multipartFormat.getSubFormat()).boundary(multipartFormat.getParameter("boundary")) //
							.sequential(); //
					request = multipartRequestUnmarshaller.unmarshall(summaryLogger, multiparts, streamManagement,
							responseMarshaller::setMarshallerEntry);
				} else {
					String requestMimeType = getMimeType(httpRequest.getContentType());
					marshallerEntry = requireMarshallerRegistryEntry(requestMimeType);
					responseMarshaller.setMarshallerEntry(marshallerEntry);
					InputStream in = inputStream;
					request = unmarshallRequest(summaryLogger, marshallerEntry, in,
							GmDeserializationOptions.defaultOptions.derive().setRequiredTypesReceiver(requiredTypesReceiver)
									.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor()).build());
				}

				List<PartOutputStreamProvider> captureOutputStreamProviders = bindCallStreamCaptures(streamManagement, partAcquiring);

				summaryLogger.logOneLine(RpcConstants.RPC_LOGSTEP_INITIAL, request);

				ServiceResult returnValue;
				try {
					stopWatch.intermediate("Pre-flight");

					boolean reasoned = Boolean.TRUE.toString().equals(httpRequest.getHeader(RpcHeaders.rpcReasoning.getHeaderName()));

					EvalContext<?> evalContext = request.eval(evaluator) //
							.with(ParentAttributeContextAspect.class, attributeContext) //
							.with(ResponseConsumerAspect.class, responseMarshaller::marshallEagerlyReturnedValue);

					if (reasoned) {
						returnValue = ServiceResults.fromMaybe(evalContext.getReasoned());
					} else {
						returnValue = ServiceResults.envelope(evalContext.get());
					}

					stopWatch.intermediate("Processing");
				} catch (RuntimeException | Error e) {
					try {
						checkOpenCaptures(request, captureOutputStreamProviders);
					} catch (RuntimeException checkEx) {
						e.addSuppressed(checkEx);
					}
					throw e;
				}

				checkOpenCaptures(request, captureOutputStreamProviders);

				final List<TransientSource> transientSources;

				if (!responseMarshaller.marshalled()) {
					if (partAcquiring.isExpensiveMultipart()) {
						transientSources = new ArrayList<>();
						new EntityCollector() {
							@Override
							protected boolean add(GenericEntity entity, EntityType<?> type) {
								boolean added = super.add(entity, type);

								if (added) {
									if (entity instanceof TransientSource) {
										TransientSource transientSource = (TransientSource) entity;
										transientSources.add(transientSource);
									}
								}

								return added;
							}
						}.visit(returnValue);

						if (!transientSources.isEmpty()) {
							// activate multipart mode
							partAcquiring.setMultipartResponse(true);
						}
						responseMarshaller.marshallReturnedValue(returnValue);
					} else {
						// activate multipart mode
						partAcquiring.setMultipartResponse(true);

						responseMarshaller.marshallReturnedValue(returnValue);
						transientSources = responseMarshaller.getTransientSources();
					}

				} else {
					transientSources = responseMarshaller.getTransientSources();
				}

				streamCallResources(partAcquiring, transientSources);

			} finally {
				IOTools.closeCloseable(multipartRequestUnmarshaller, "multipart request unmarshaller", logger);
				IOTools.closeCloseable(multiparts, "form data reader", logger);
			}

		} catch (IOException e) {
			throw GmRpcExceptionContextualizer.enhanceException(e, attributeContext, request);
		} catch (RuntimeException e) {
			throw GmRpcExceptionContextualizer.enhanceException(e, attributeContext, request);
		} catch (Exception e) {
			throw new ServletException(GmRpcExceptionContextualizer.enhanceException(e, attributeContext, request));

		} finally {

			summaryLogger.log(this, request);
			summaryLogger.logOneLine(RpcConstants.RPC_LOGSTEP_FINAL, request);

			logger.trace(() -> "GmWebRpcServer.doPost: " + stopWatch);
		}

	}

	private void checkOpenCaptures(ServiceRequest request, List<PartOutputStreamProvider> captureOutputStreamProviders) {
		List<PartOutputStreamProvider> openedProviders = null;

		for (PartOutputStreamProvider provider : captureOutputStreamProviders) {
			if (provider.getState() == State.opened) {
				if (openedProviders == null) {
					openedProviders = new ArrayList<>();
				}
				try {
					provider.getOut().close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				openedProviders.add(provider);
			}
		}

		if (openedProviders != null) {
			String openBindIds = openedProviders.stream().map(PartOutputStreamProvider::getBindId).collect(Collectors.joining(","));
			String msg = "The request " + request + " has opened but not closed the following CallStreamCaptures: " + openBindIds;
			throw new IllegalStateException(msg);
		}
	}

	protected class ResponseMarshaller {

		private boolean marshalled = false;

		private final ServiceRequestSummaryLogger summaryLogger;
		private final HttpServletRequest httpRequest;
		private final PartAcquiring partAcquiring;

		private MarshallerRegistryEntry marshallerEntry;

		private Object eagerReturnValue;

		private final List<TransientSource> transientSources = new ArrayList<>();

		public ResponseMarshaller(ServiceRequestSummaryLogger summaryLogger, HttpServletRequest httpRequest, PartAcquiring partAcquiring) {
			super();
			this.summaryLogger = summaryLogger;
			this.httpRequest = httpRequest;
			this.partAcquiring = partAcquiring;
		}

		public void setMarshallerEntry(MarshallerRegistryEntry marshallerEntry) {
			this.marshallerEntry = marshallerEntry;
		}

		public List<TransientSource> getTransientSources() {
			return transientSources;
		}

		public void marshallEagerlyReturnedValue(Object returnValue) {

			if (marshalled) {
				logger.warn("Response already marshalled, value will be ignored: " + returnValue);
				return;
			}

			// forces a multipart response regardless of call resources or call stream captures being previously
			// written to.
			partAcquiring.setMultipartResponse(true);

			ResponseEnvelope response = ServiceResults.envelope(returnValue);
			marshall(response);

			if (logger.isTraceEnabled()) {
				logger.trace("Marshalled eagerly: " + response);
			}

			this.eagerReturnValue = returnValue;

		}

		public void marshallReturnedValue(ServiceResult serviceResult) {

			if (marshalled) {
				logger.warn("Response already marshalled, value will be ignored: " + serviceResult);
				return;
			}

			marshall(serviceResult);

		}

		public void marshallFailure(Failure failure) {

			if (marshalled) {
				logger.warn("Response already marshalled, value will be ignored: " + failure);
				return;
			}

			// partAcquiring.setMultipartResponse(false);

			marshall(failure);

		}

		private void marshall(ServiceResult response) {
			// @formatter:off
			marshallResponse(
					httpRequest, 
					response, 
					marshallerEntry, 
					partAcquiring
			);
			// @formatter:on

			marshalled = true;
		}

		protected void marshallResponse(HttpServletRequest req, ServiceResult result, MarshallerRegistryEntry marshallerRegistryEntry,
				PartAcquiring partAcquiring) {
			try {
				summaryLogger.startTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_RESPONSE);

				if (marshallerRegistryEntry == null) {
					marshallerRegistryEntry = marshallerRegistry.getMarshallerRegistryEntry(defaultMarshallerMimeType);
				}

				marshallerRegistryEntry = getResponseMarshallerRegistryEntry(req, marshallerRegistryEntry);
				Marshaller responseMarshaller = marshallerRegistryEntry.getMarshaller();

				OutputStream out = partAcquiring.openRpcResponseStream(marshallerRegistryEntry.getMimeType());

				try {
					responseMarshaller.marshall(out, result, GmSerializationOptions.deriveDefaults().set(EntityVisitorOption.class, this::visitEntity)
							.outputPrettiness(OutputPrettiness.low).build());

					if (logger.isTraceEnabled()) {
						logger.trace("Marshalled " + result);
					}

				} finally {
					out.close();
				}

			} catch (Exception e) {
				// TODO: add context instead of wrapping directly
				throw new MarshallException("Failed to marshall RPC response" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
			} finally {
				summaryLogger.stopTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_RESPONSE);
			}
		}

		private void visitEntity(GenericEntity entity) {
			if (entity instanceof TransientSource) {
				TransientSource transientSource = (TransientSource) entity;
				transientSources.add(transientSource);
			}
		}

		public boolean marshalled() {
			return marshalled;
		}

		public Object getEagerReturnValue() {
			return eagerReturnValue;
		}

	}

	protected AttributeContext initializeContext(HttpServletRequest httpRequest) {

		AttributeContext attributeContext = AttributeContexts.peek();
		AttributeContext derivedContext = attributeContext.derive() //
				.set(RequestedEndpointAspect.class, httpRequest.getRequestURL().toString()) //
				.set(RequestorIdAspect.class, httpRequest.getHeader(RpcHeaders.rpcClientId.getHeaderName())) //
				.set(SummaryLoggerAspect.class, RpcServiceRequestSummaryLogger.getInstance(logger, attributeContext, metaDataResolverProvider)) //
				.build();

		return derivedContext;
	}

	protected ServiceRequest unmarshallRequest(ServiceRequestSummaryLogger summaryLogger, MarshallerRegistryEntry marshallerRegistryEntry,
			InputStream in, GmDeserializationOptions options) throws MarshallException {
		summaryLogger.startTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_REQUEST);
		try {
			ServiceRequest request = (ServiceRequest) marshallerRegistryEntry.getMarshaller().unmarshall(in, options);

			// Some marshallers might return null for unknown entity types. (BTT-6981)
			// Here we explicitly provide the caller with this information.
			Objects.requireNonNull(request, "unmarshall() call returned null");

			return request;
		} catch (MarshallException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshallException("Failed to unmarshall " + marshallerRegistryEntry.getMimeType() + " request"
					+ (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		} finally {
			summaryLogger.stopTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_REQUEST);
		}
	}

	protected MultipartRequestUnmarshaller createMultipartRequestUnmarshaller() {
		if (processResourceRequestPartsAsynchronously) {
			return new NonBlockingMultipartRequestUnmarshaller();
		} else {
			return new BlockingMultipartRequestUnmarshaller();
		}
	}

	protected List<PartOutputStreamProvider> bindCallStreamCaptures(RpcUnmarshallingStreamManagement streamManagement, PartAcquiring partAcquiring) {
		List<CallStreamCapture> callStreamCaptures = streamManagement.getCallStreamCaptures();
		if (!callStreamCaptures.isEmpty()) {
			List<PartOutputStreamProvider> providers = new ArrayList<>(callStreamCaptures.size());
			for (CallStreamCapture capture : callStreamCaptures) {
				String bindId = requireNonNull(capture.getGlobalId(), "CallStreamCapture.globalId (i.e. bindId for PartAcquiring) cannot be null.");
				PartOutputStreamProvider outputStreamProvider = new PartOutputStreamProvider(partAcquiring, bindId);
				providers.add(outputStreamProvider);
				capture.setOutputStreamProvider(outputStreamProvider);
			}
			partAcquiring.setMultipartResponse(true);

			return providers;
		} else {
			return Collections.emptyList();
		}
	}

	protected MarshallerRegistryEntry requireMarshallerRegistryEntry(String requestMimeType) throws ServletException {
		MarshallerRegistryEntry marshallerRegistryEntry = marshallerRegistry.getMarshallerRegistryEntry(requestMimeType);
		if (marshallerRegistryEntry == null) {
			throw new ServletException("no marshaller for mime type '" + requestMimeType + "' is configured in the registry");
		}
		return marshallerRegistryEntry;
	}

	protected String getRpcVersion(HttpServletRequest req) {
		String rpcVersion = req.getHeader(RpcHeaders.rpcVersion.getHeaderName());

		if (rpcVersion == null)
			return "1";
		else
			return rpcVersion;
	}

	protected MultipartFormat getMimeTypeFromOptions(String accepts[], MultipartFormat defaultFormat) {
		for (String accept : accepts) {
			MultipartFormat format = Multiparts.parseFormat(accept);

			if (SUPPORTED_MULTIPART_FORMATS.contains(format.getSubFormat()))
				return format;
		}

		return defaultFormat;
	}

	private static final String[] emptyStringArray = {};

	protected MultipartFormat getMultipartMimetypeAcceptance(HttpServletRequest req) {
		String acceptHeader = req.getHeader("Accept");
		String accepts[] = acceptHeader != null ? acceptHeader.split(",") : emptyStringArray;

		switch (getRpcVersion(req)) {
			case "1":
				return getMimeTypeFromOptions(accepts, RPC1_DEFAULT_MULTIPART_FORMAT);
			default:
				return getMimeTypeFromOptions(accepts, null);
		}
	}

	protected MarshallerRegistryEntry getResponseMarshallerRegistryEntry(HttpServletRequest req,
			MarshallerRegistryEntry requestMarshallerRegistryEntry) {
		String acceptHeader = req.getHeader("Accept");

		if (acceptHeader != null && acceptHeader.length() > 0) {
			String accepts[] = acceptHeader.split(",");

			for (String accept : accepts) {
				MarshallerRegistryEntry acceptHeaderMarshallerEntry = marshallerRegistry.getMarshallerRegistryEntry(accept);
				if (acceptHeaderMarshallerEntry != null && acceptHeaderMarshallerEntry.getMarshaller() != null) {
					if (logger.isTraceEnabled())
						logger.trace("Found [ " + acceptHeaderMarshallerEntry.getMarshaller().getClass().getSimpleName() + " ] for [ " + accept
								+ " ] type from Accept header");
					return acceptHeaderMarshallerEntry;
				}
			}
		}

		return requestMarshallerRegistryEntry;
	}

	/**
	 * <p>
	 * Extracts the mime type part from the content type header, since the given content type may include a character
	 * encoding specification, for example, {@code text/html;charset=UTF-8}.
	 */
	protected String getMimeType(String requestContentType) {

		if (requestContentType == null)
			return requestContentType;

		int scIdx = requestContentType.indexOf(';');
		if (scIdx == -1)
			return requestContentType;

		return requestContentType.substring(0, scIdx).trim();
	}

	protected void streamCallResources(PartAcquiring partAcquiring, List<TransientSource> transientSources) throws Exception {
		if (transientSources != null && !transientSources.isEmpty()) {

			for (TransientSource transientSource : transientSources) {
				String partName = transientSource.getGlobalId();

				try (OutputStream partOut = partAcquiring.openPartStream(partName)) {
					transientSource.writeToStream(partOut);
				}
			}
		}
	}

	private final class BlockingMultipartRequestUnmarshaller implements MultipartRequestUnmarshaller {

		@Override
		public ServiceRequest unmarshall(ServiceRequestSummaryLogger summaryLogger, SequentialFormDataReader multiparts,
				RpcUnmarshallingStreamManagement streamManagement, Consumer<MarshallerRegistryEntry> marshallerEntryReceiver) throws Exception {

			// Regarding the fullPartsBackup flag:
			// Currently every part needs to be backed up to allow the ServiceRequest interception.
			// This is required unless there is a way of knowing at this point that the request will be intercepted.

			ServiceRequest request = null;
			PartReader part = null;
			MarshallerRegistryEntry marshallerRegistryEntry = null;

			while ((part = multiparts.next()) != null) {

				String name = part.getName();

				if (RpcConstants.RPC_MAPKEY_REQUEST.equals(name)) {

					// master part containing the request assembly
					String requestMimeType = getMimeType(part.getContentType());
					marshallerRegistryEntry = requireMarshallerRegistryEntry(requestMimeType);
					marshallerEntryReceiver.accept(marshallerRegistryEntry);

					try (InputStream partIn = part.openStream()) {
						//@formatter:off
						request = unmarshallRequest(summaryLogger, marshallerRegistryEntry, partIn,
								GmDeserializationOptions.deriveDefaults()
								.setRequiredTypesReceiver(requiredTypesReceiver)
								.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor())
								.build());
						//@formatter:on
					}

				} else {
					StreamPipe pipe = streamManagement.acquirePipe(part.getName());

					try (OutputStream partBackupOut = pipe.openOutputStream(); InputStream partIn = part.openStream()) {
						IOTools.transferBytes(partIn, partBackupOut, () -> new byte[partTransferBufferSize]);
						logger.trace(() -> "Performed eager backup of part " + name);
					}
				}

			}

			streamManagement.bindTransientSources();
			streamManagement.checkPipeSatisfaction();

			return request;

		}

		@Override
		public void close() {
			// no-op
		}

	}

	private final class NonBlockingMultipartRequestUnmarshaller implements MultipartRequestUnmarshaller {

		private final Map<String, DeferredInputStreamSupplier> backups = new ConcurrentHashMap<>();
		private Future<Boolean> asyncProcessingResult;

		@Override
		public ServiceRequest unmarshall(ServiceRequestSummaryLogger summaryLogger, SequentialFormDataReader multiparts,
				RpcUnmarshallingStreamManagement streamManagement, Consumer<MarshallerRegistryEntry> marshallerEntryReceiver) throws Exception {

			PartReader part = multiparts.next();

			if (part == null) {
				throw new GmRpcException("Invalid multipart request. No part available");
			}

			String name = part.getName();

			if (!RpcConstants.RPC_MAPKEY_REQUEST.equals(name)) {
				throw new GmRpcException("Invalid multipart request. The \"" + RpcConstants.RPC_MAPKEY_REQUEST + "\" part must be the first");
			}

			// master part containing the request assembly
			ServiceRequest request = null;
			String requestMimeType = getMimeType(part.getContentType());
			MarshallerRegistryEntry marshallerRegistryEntry = requireMarshallerRegistryEntry(requestMimeType);
			marshallerEntryReceiver.accept(marshallerRegistryEntry);

			try (InputStream partIn = part.openStream()) {
				request = unmarshallRequest(summaryLogger, marshallerRegistryEntry, partIn,
						GmDeserializationOptions.deriveDefaults().setRequiredTypesReceiver(requiredTypesReceiver)
								.set(EntityVisitorOption.class, streamManagement.getMarshallingVisitor()).build());
			}

			streamManagement.bindTransientSources();

			processCallResources(multiparts.next(), multiparts);

			return request;

		}

		@Override
		public void close() {

			if (asyncProcessingResult != null) {
				try {
					asyncProcessingResult.get();
				} catch (InterruptedException e) {
					// Ignored
				} catch (ExecutionException e) {
					logger.error("Failed to process request resources asynchronously: " + e.getCause(), e.getCause());
				}
			}

		}

		protected void processCallResources(PartReader firstPart, SequentialFormDataReader multiparts) {
			if (firstPart == null) {
				// We avoid spawning a thread if no part but the request part is available.
				// Any bound call resource is invalidated right away.
				invalidateMissingBindings();
			} else {
				asyncProcessingResult = resourceRequestPartExecutor.submit(() -> processCallResourcesAsynchronously(firstPart, multiparts));
			}
		}

		protected Boolean processCallResourcesAsynchronously(PartReader firstPart, SequentialFormDataReader multiparts) {

			PartReader part = firstPart;

			try {
				do {
					String partName = part.getName();

					DeferredInputStreamSupplier supplier = backups.remove(partName);

					if (supplier == null) {
						try {
							part.consume();
							logger.warn("Ignoring request part which was not bound as a call resource: " + partName);
						} catch (Throwable t) {
							logger.error("Failed consume part which is not referenced in the request: " + partName, t);
						}
						continue;
					}

					Path tempFile;
					try {
						tempFile = Files.createTempFile("rpc-part-" + partName, ".tmp");
						FileTools.deleteFileWhenOrphaned(tempFile.toFile());
					} catch (Throwable e) {
						supplier.markAsFailed(e);
						logger.trace(() -> "Marked backup of part " + partName + " as failed: " + e);
						continue;
					}

					TemporaryFileInputStreamSupplier delegateSupplier = new TemporaryFileInputStreamSupplier(tempFile);

					supplier.bindDelegate(delegateSupplier, true);

					try (OutputStream out = Files.newOutputStream(tempFile); InputStream in = part.openStream()) {
						long totalBytes = IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
						supplier.markAsConcluded(delegateSupplier, totalBytes);
						logger.trace(() -> "Marked backup of part " + partName + " as concluded: " + tempFile);
					} catch (IOException e) {
						supplier.markAsFailed(e);
						logger.trace(() -> "Marked backup of part " + partName + " as failed: " + e);
					}

				} while ((part = multiparts.next()) != null);
			} catch (Throwable t) {
				throw new GmRpcException("Failed to process call resource parts asynchronously: " + t, t);
			}

			invalidateMissingBindings(); // Invalidates any bound call resource missing a correspondent part

			return true;

		}

		/**
		 * <p>
		 * Invalidates the call resource bindings which missed a correspondent part.
		 */
		protected void invalidateMissingBindings() {
			try {
				if (backups != null) {
					Iterator<Entry<String, DeferredInputStreamSupplier>> it = backups.entrySet().iterator();
					Entry<String, DeferredInputStreamSupplier> entry = null;
					while (it.hasNext() && (entry = it.next()) != null) {
						String bindId = null;
						try {
							bindId = entry.getKey();
							IllegalStateException failure = new IllegalStateException(
									"There is no part in the request corresponding to the CallResource " + bindId);
							DeferredInputStreamSupplier supplier = entry.getValue();
							if (supplier != null) {
								supplier.markAsFailed(failure);
							}
							it.remove();
						} catch (Throwable t) {
							logger.error("Failed to process with the invalidation of the binding for the missing part " + bindId, t);
						}
					}
				}
			} catch (Throwable t) {
				logger.error("Failed to process with the invalidation of missing parts bindings", t);
			}
		}

	}

	@Configurable
	public void setMetaDataResolverProvider(Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {
		this.metaDataResolverProvider = metaDataResolverProvider;
	}
}
