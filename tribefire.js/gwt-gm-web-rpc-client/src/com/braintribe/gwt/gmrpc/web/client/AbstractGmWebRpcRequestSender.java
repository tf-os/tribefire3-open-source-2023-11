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
package com.braintribe.gwt.gmrpc.web.client;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.exception.CanceledException;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.fileapi.client.Blob;
import com.braintribe.gwt.fileapi.client.FileReader;
import com.braintribe.gwt.fileapi.client.FormData;
import com.braintribe.gwt.fileapi.client.ProgressEvent;
import com.braintribe.gwt.fileapi.client.ProgressEventType;
import com.braintribe.gwt.fileapi.client.ProgressHandler;
import com.braintribe.gwt.fileapi.client.XMLHttpRequest2;
import com.braintribe.gwt.genericmodel.client.codec.api.GmAsyncCodec;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmEncodingContext;
import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.gwt.genericmodel.client.resource.GwtInputStreamProvider;
import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.gwt.gmrpc.api.client.itw.TypeEnsurer;
import com.braintribe.gwt.gmrpc.api.client.transport.GmRpcRequestSender;
import com.braintribe.gwt.gmrpc.api.client.transport.RpcHeaders;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.gwt.gmrpc.api.client.user.RequestUploadProgress;
import com.braintribe.gwt.gmrpc.api.client.user.RequestUploadProgressMonitor;
import com.braintribe.gwt.gmrpc.api.client.user.ResourceSupport;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class AbstractGmWebRpcRequestSender implements GmRpcRequestSender {

	public static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private static String jseMimeType;
	private String serverUrl;
	private Supplier<String> serverUrlProvider;
	private Supplier<String> sessionIdProvider;
	private TypeEnsurer typeEnsurer;
	private Map<String, GmAsyncCodec<Object, String>> codecs;
	private final GmAsyncCodec<Object, String> sendCodec = new GmXmlCodec<>();
	private String serviceId;
	private boolean serviceIdRequired = false;
	private boolean useProxyContext = true;
	private boolean forceXmlDecoding = false;

	public AbstractGmWebRpcRequestSender() {
	}

	private Supplier<Map<String, Object>> metaDataProvider;

	private boolean sendSessionIdExpressive = true;

	@Configurable
	public void setSendSessionIdExpressive(boolean sendSessionIdExpressive) {
		this.sendSessionIdExpressive = sendSessionIdExpressive;
	}

	@Configurable
	public void setServiceIdRequired(boolean serviceIdRequired) {
		this.serviceIdRequired = serviceIdRequired;
	}

	@Configurable
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Configurable
	public void setTypeEnsurer(TypeEnsurer typeEnsurer) {
		this.typeEnsurer = typeEnsurer;
	}

	@Configurable
	public void setUseProxyContext(boolean proxyAware) {
		this.useProxyContext = proxyAware;
	}

	@Configurable
	public void setForceXmlDecoding(boolean forceXmlDecoding) {
		this.forceXmlDecoding = forceXmlDecoding;
	}

	public Map<String, GmAsyncCodec<Object, String>> getCodecs() {
		if (codecs == null) {
			codecs = new HashMap<>();

			JseCodec jseCodec = new JseCodec();
			codecs.put("gm/jse", jseCodec);
			codecs.put("gm/jseh", jseCodec);
			codecs.put("gm/xml", sendCodec);
		}

		return codecs;
	}

	public static String getJseMimeType() {
		if (jseMimeType == null) {
			jseMimeType = isHostedMode() ? "gm/jseh" : "gm/jse"; // gm/jse gm/jseh gm/jsep gm/jsehp
		}

		return jseMimeType;
	}

	public static boolean isHostedMode() {
		return !GWT.isProdMode();
	}

	@Configurable
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getServerUrl() {
		if (serverUrl != null)
			return serverUrl;
		if (serverUrlProvider != null)
			return serverUrlProvider.get();
		return "";
	}

	@Configurable
	public void setServerUrlProvider(Supplier<String> serverUrlProvider) {
		this.serverUrlProvider = serverUrlProvider;
	}

	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Configurable
	public void setMetaDataProvider(Supplier<Map<String, Object>> metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	@Override
	public ServiceResult sendRequest(ServiceRequest request, EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, boolean reasoned)
			throws GmRpcException {
		try {
			enrichRequest(request);
		} catch (RuntimeException e1) {
			throw new GmRpcException("error while enriching RpcRequest", e1);
		}

		String requestData;
		try {
			requestData = sendCodec.encode(request, null);
			return sendEncoded(requestData, embeddedRequiredTypesExpert, reasoned);
		} catch (CodecException e1) {
			throw new GmRpcException("error while encoding RpcRequest", e1);
		}

	}

	private ServiceResult sendEncoded(String requestData, EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, boolean reasoned)
			throws GmRpcException, CodecException {
		SynchronousXMLHttpRequest xmlHttpRequest = XMLHttpRequest.create().cast();
		xmlHttpRequest.openSynchronous("POST", getServerUrl());
		enrich(xmlHttpRequest, reasoned);
		xmlHttpRequest.send(requestData);

		if (xmlHttpRequest.getResponseHeader(RpcHeaders.rpcBody.getHeaderName()) != null) {
			try {
				return decode(xmlHttpRequest, xmlHttpRequest.getResponseText(), embeddedRequiredTypesExpert);
			} catch (Exception e) {
				throw new GmRpcException("error while decoding response", e);
			}
		} else {
			throw new GmRpcException("error while sending RPC request: " + xmlHttpRequest.getStatusText()
					+ " - statusCode: " + xmlHttpRequest.getStatus());
		}
	}

	@Override
	public void sendRequest(ServiceRequest request, final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert,
			final AsyncCallback<ServiceResult> asyncCallback, boolean reasoned) {
		sendRequest(EmptyRpcContext.INSTANCE, request, embeddedRequiredTypesExpert, asyncCallback, reasoned);
	}

	public void sendRequest(RpcContext rpcContext, ServiceRequest request,
			final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert,
			final AsyncCallback<ServiceResult> asyncCallback, boolean reasoned) {
		try {
			enrichRequest(request);
		} catch (Exception e1) {
			asyncCallback.onFailure(new GmRpcException("error while enriching RpcRequest", e1));
			return;
		}
		final String phHintBase = getProfilingHintBase(request);
		List<TransientSource> transientSources = new ArrayList<>();
		GmEncodingContext context = entity -> {
			if (entity.type() == TransientSource.T) {
				transientSources.add((TransientSource) entity);
			}
		};

		final ProfilingHandle phRpcRequestEncoding = Profiling.start(this, phHintBase + " - encoding (async)", true);
		sendCodec.encodeAsync(request, context).get(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String requestData) {
				phRpcRequestEncoding.stop();
				sendEncoded(rpcContext, requestData, embeddedRequiredTypesExpert, phHintBase, asyncCallback,
						transientSources, reasoned);
			}

			@Override
			public void onFailure(Throwable caught) {
				phRpcRequestEncoding.stop();
				asyncCallback.onFailure(new GmRpcException("error while encoding RpcRequest", caught));
			}
		});
	}

	protected String getProfilingHintBase(ServiceRequest serviceRequest) {
			return getRpcRequestProfilingHintBase(serviceRequest);
	}

	private String getRpcRequestProfilingHintBase(ServiceRequest serviceRequest) {
		StringBuilder phHintBase = new StringBuilder();
		EntityType<GenericEntity> entityType = serviceRequest.entityType();
		phHintBase.append("RPC " + entityType.getTypeSignature());
		boolean first = true;
		phHintBase.append('{');
		for (Property property : entityType.getProperties()) {
			if (!first) {
				phHintBase.append(',');
			}
			String name = property.getName();
			Object value = property.get(serviceRequest);
			String parameterString = String.valueOf(value);
			if (parameterString.length() > 15) {
				parameterString = parameterString.substring(0, 15) + "...";
			}
			phHintBase.append(name);
			phHintBase.append(':');
			phHintBase.append(parameterString);
		}
		phHintBase.append('}');
		return phHintBase.toString();
	}

	private static final class ProgressHandlerAdapter implements ProgressHandler {
		RequestUploadProgressMonitor requestUploadProgressMonitor;
		
		public ProgressHandlerAdapter(RequestUploadProgressMonitor requestUploadProgressMonitor) {
			super();
			this.requestUploadProgressMonitor = requestUploadProgressMonitor;
		}

		@Override
		public void onProgress(ProgressEvent event) {
			requestUploadProgressMonitor.onProgress(new RequestUploadProgress() {
				
				@Override
				public double getTotal() {
					return event.getTotal();
				}
				
				@Override
				public double getLoaded() {
					return event.getLoaded();
				}
				
				@Override
				public boolean getLengthComputable() {
					return event.getLengthComputable();
				}
				
				@Override
				public String getErrorMessage() {
					return event.getErrorMessage();
				}
			});
		}
	}
	
	private void sendEncoded(RpcContext rpcContext, final String requestData,
			final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, final String profilingHintBase,
			final AsyncCallback<ServiceResult> asyncCallback, final List<TransientSource> transientSources, boolean reasoned) {

		final ProfilingHandle phRpcSend = Profiling.start(this, profilingHintBase + " - request (async)", true);

		// we always want to use blobs, rather than let the user activate the support for it;  
		 boolean useBlob = shouldUseBlob(rpcContext);

		XMLHttpRequest2 xmlHttpRequest = XMLHttpRequest2.create().cast();
		
		RequestUploadProgressMonitor requestUploadProgressMonitor = rpcContext.get(RequestUploadProgressMonitor.class);
		
		if (requestUploadProgressMonitor != null)
			xmlHttpRequest.getUpload().setProgressHandler(new ProgressHandlerAdapter(requestUploadProgressMonitor));
		
		xmlHttpRequest.open("POST", getServerUrl());
		xmlHttpRequest.setOnReadyStateChange(xhr -> {
			if (xhr.getReadyState() == XMLHttpRequest.DONE) {
				phRpcSend.stop();
				if (xhr.getResponseHeader(RpcHeaders.rpcBody.getHeaderName()) != null) {
					decode(/*rpcContext,*/ xhr, useBlob, embeddedRequiredTypesExpert, profilingHintBase, asyncCallback);
				} else {
					asyncCallback.onFailure(new GmRpcException("error while sending RPC request: "
							+ xhr.getStatusText() + " - statusCode: " + xhr.getStatus()));
				}
			}
		});

		if (useBlob) {
			String base = "gm/xml, multipart/form-data;sliceable=true";
			xmlHttpRequest.setRequestHeader("Accept", forceXmlDecoding ? base : getJseMimeType() + "," + base);
			xmlHttpRequest.setResponseType("blob");
			
			if (!isEmpty(transientSources)) {
				FormData fd = FormData.create();
				fd.append("rpc-request", Blob.createFromString(requestData, "gm/xml"));
				for (TransientSource ts : transientSources) {
					InputStreamProvider isp = ts.getInputStreamProvider();
					if (isp instanceof GwtInputStreamProvider) {
						Blob b = ((GwtInputStreamProvider) isp).blob();
						fd.append(ts.getGlobalId(), b);
					}
				}
				xmlHttpRequest.send(fd);
				return;
			}
		}

		try {
			enrich(xmlHttpRequest, reasoned);
		} catch (CodecException e) {
			asyncCallback.onFailure(e);
			return;
		}

		xmlHttpRequest.send(requestData);
	}

	private boolean shouldUseBlob(RpcContext rpcContext) {
		Boolean resSupport = rpcContext.get(ResourceSupport.class);
		return resSupport == null || resSupport;
	}

	private void enrich(XMLHttpRequest xmlHttpRequest, boolean reasoned) throws CodecException {
		xmlHttpRequest.setRequestHeader("Accept", forceXmlDecoding ? "gm/xml" : getJseMimeType() + ",gm/xml");
		xmlHttpRequest.setRequestHeader("Content-Type", "gm/xml");
		xmlHttpRequest.setRequestHeader(RpcHeaders.rpcReasoning.getHeaderName(), String.valueOf(reasoned));
	}

	protected void enrichRequest(ServiceRequest request) throws RuntimeException, GmRpcException {
		if (request instanceof DispatchableRequest) {
			DispatchableRequest dispatchableRequest = (DispatchableRequest) request;
			if (dispatchableRequest.getServiceId() == null) {
				if (serviceId != null) {
					dispatchableRequest.setServiceId(serviceId);
				} else if (serviceIdRequired)
					throw new GmRpcException(
							"serviceId was neither given by the request nor by the configuration of the request sender");
			}
		}

		if (sessionIdProvider != null && request instanceof AuthorizableRequest) {
			AuthorizableRequest authorizableRequest = (AuthorizableRequest) request;
			String sessionId = sessionIdProvider.get();

			if (sendSessionIdExpressive) {
				authorizableRequest.setSessionId(sessionId);
			} else {
				authorizableRequest.getMetaData().put("sessionId", sessionId);
			}
		}

		if (metaDataProvider != null) {
			Map<String, Object> genericMetaData = metaDataProvider.get();

			if (genericMetaData != null && !genericMetaData.isEmpty()) {
				request.getMetaData().putAll(genericMetaData);
			}
		}
	}

	private ServiceResult decode(XMLHttpRequest xhr, String responseText,
			EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert) throws CodecException {

		String contentType = xhr.getResponseHeader("Content-Type");

		final GmAsyncCodec<Object, String> codec = forceXmlDecoding ? sendCodec : getCodecs().get(contentType);

		if (codec == null) {
			throw new CodecException("unsupported content type: " + contentType);
		} else {
			if (embeddedRequiredTypesExpert != null) {
				return useProxyContext
						? decodeWithIntrinsicModelProxyAware(codec, responseText, embeddedRequiredTypesExpert)
						: decodeWithIntrinsicModelLegacy(codec, responseText, embeddedRequiredTypesExpert);
			} else {
				GmDecodingContext standardContext = new StandardDecodingContext(typeEnsurer);
				ServiceResult rpcResponse = codec.decode(responseText, standardContext);
				return rpcResponse;
			}
		}
	}

	private ServiceResult decodeWithIntrinsicModelLegacy(GmAsyncCodec<Object, String> codec, String responseText,
			EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert) throws CodecException {
		GmDecodingContext lenientContext = new EmbeddedTypesDecodingContext(embeddedRequiredTypesExpert);
		Object assembly = codec.decode(responseText, lenientContext);
		GmMetaModel model = embeddedRequiredTypesExpert.getModelFromAssembly(assembly);
		try {
			typeReflection.deploy(model);
		} catch (GmfException e) {
			throw new CodecException("error while ensuring types", e);
		}
		GmDecodingContext standardContext = new StandardDecodingContext(null);
		ServiceResult rpcResponse = codec.decode(responseText, standardContext);
		return rpcResponse;
	}

	private ServiceResult decodeWithIntrinsicModelProxyAware(GmAsyncCodec<Object, String> codec, String responseText,
			EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert) throws CodecException {
		StandardDecodingContext standardContext = new StandardDecodingContext(null);
		standardContext.setProxyContext(new ProxyContext());
		standardContext.setEmbeddedRequiredTypesExpert(embeddedRequiredTypesExpert);
		ServiceResult rpcResponse = codec.decode(responseText, standardContext);
		return rpcResponse;
	}

	private void decodeWithIntrinsicModelProxyAware(GmAsyncCodec<Object, String> codec, String responseText,
			EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, final AsyncCallback<ServiceResult> asyncCallback,
			String profilingHintBase, ResponseInfo responseInfo) {
		final ProfilingHandle phProxyDecoding = Profiling.start(this,
				profilingHintBase + " - decode with proxy context (async)", true);
		StandardDecodingContext standardContext = new StandardDecodingContext(null);
		standardContext.setProxyContext(new ProxyContext());
		standardContext.setEmbeddedRequiredTypesExpert(embeddedRequiredTypesExpert);
		standardContext.setLenientDecode(true);
		standardContext.setResponseInfo(responseInfo);
		
		
		codec.decodeAsync(responseText, standardContext).get(new AsyncCallback<Object>() {
			@Override
			public void onSuccess(Object result) {
				phProxyDecoding.stop();
				if(responseInfo != null) responseInfo.transfer();
				asyncCallback.onSuccess((ServiceResult) result);
			}

			@Override
			public void onFailure(Throwable caught) {
				phProxyDecoding.stop();
				asyncCallback.onFailure(caught);
			}
		});
	}

	private void decodeWithIntrinsicModelLegacy(final GmAsyncCodec<Object, String> codec, final String responseText,
			final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert,
			final AsyncCallback<ServiceResult> asyncCallback, final String profilingHintBase,
			ResponseInfo responseInfo) {
		final ProfilingHandle phLenientDecoding = Profiling.start(this,
				profilingHintBase + " - decode required model types (async)", true);
		GmDecodingContext lenientContext = new EmbeddedTypesDecodingContext(embeddedRequiredTypesExpert);
		codec.decodeAsync(responseText, lenientContext).get(new AsyncCallback<Object>() {
			@Override
			public void onSuccess(Object result) {
				phLenientDecoding.stop();
				ServiceResult response = (ServiceResult) result;
				ResponseEnvelope responseEnvelope = response.asResponse();
				Object returnValue = responseEnvelope != null ? responseEnvelope.getResult() : null;
				GmMetaModel model = embeddedRequiredTypesExpert.getModelFromAssembly(returnValue);

				final ProfilingHandle phEnsureModel = Profiling.start(this,
						profilingHintBase + " - ensure embedded model types (async)", true);
				typeReflection.deploy(model, new com.braintribe.processing.async.api.AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void future) {
						phEnsureModel.stop();
						StandardDecodingContext standardContext = new StandardDecodingContext(null);
						standardContext.setResponseInfo(responseInfo);
						final ProfilingHandle phDecode = Profiling.start(this, profilingHintBase + " - decode (async)",
								true);
						codec.<ServiceResult>decodeAsync(responseText, standardContext)
								.get(new AsyncCallback<ServiceResult>() {
									@Override
									public void onSuccess(ServiceResult result) {										
										phDecode.stop();
										if(responseInfo != null) responseInfo.transfer();
										asyncCallback.onSuccess(result);
									}

									@Override
									public void onFailure(Throwable caught) {
										phDecode.stop();
										asyncCallback.onFailure(caught);
									}
								});
					}

					@Override
					public void onFailure(Throwable t) {
						phEnsureModel.stop();
						asyncCallback.onFailure(t);
					}
				});
			}

			@Override
			public void onFailure(Throwable caught) {
				phLenientDecoding.stop();
				asyncCallback.onFailure(new GmRpcException(
						"error while decoding leniently to extract model for required types", caught));
			}
		});
	}

	@SuppressWarnings("incomplete-switch")
	private static void getTextFromBlob(Blob blob, AsyncCallback<String> callback) {
		FileReader reader = FileReader.create();

		reader.addEventHandler((e, t) -> {
			switch (t) {
			case abort:
				callback.onFailure(CanceledException.emptyInstance);
				break;
			case error:
				callback.onFailure(new RuntimeException(e.getErrorMessage()));
				break;
			case load:
				String text = reader.getStringResult();
				callback.onSuccess(text);
				break;
			}
		}, ProgressEventType.abort, ProgressEventType.load, ProgressEventType.error);

		reader.readAsText(blob);
	}

	private static native Blob getResponseBlob(XMLHttpRequest xhr) /*-{
																	return xhr.response;
																	}-*/;

	private static native String getResponseString(XMLHttpRequest xhr) /*-{
																		return xhr.response;
																		}-*/;

	@SuppressWarnings("incomplete-switch")
	private static void getBlobParts(String boundary, Blob blob, AsyncCallback<Map<String, Blob>> callback) {
		try{
			long blobSize = blob.size();
	
			long end = blobSize - boundary.length() - 4 - 2;
			long start = end - 8;
	
			Blob overviewSizeBlob = blob.slice(start, end);
	
			FileReader overviewSizeReader = FileReader.create();
	
			overviewSizeReader.addEventHandler((e, t) -> {
				try{
					switch (t) {
					case abort:
						callback.onFailure(CanceledException.emptyInstance);
						break;
					case error:
						callback.onFailure(new RuntimeException(e.getErrorMessage()));
						break;
					case load:
						String sizeAsStr = overviewSizeReader.getStringResult();
						
						int overviewSize = toUint16(sizeAsStr);
		
						Blob overviewBlob = blob.slice(start - overviewSize - 2, start - 2);
		
						FileReader overviewReader = FileReader.create();
		
						overviewReader.addEventHandler((event, type) -> {
							try {
								switch (type) {
								case abort:
									callback.onFailure(CanceledException.emptyInstance);
									break;
								case error:
									callback.onFailure(new RuntimeException(event.getErrorMessage()));
									break;
								case load:
									String overview = overviewReader.getStringResult();
									String parts[] = overview.split("\r\n");
									Map<String, Blob> partBlobs = new LinkedHashMap<>();
									for (String part : parts) {
										UrlParameters params = new UrlParameters(part, false);
										String name = params.getParameter("name");
										String contentType = params.getParameter("content-type");
										double partStart = Double.parseDouble(params.getParameter("start"));
										double partEnd = Double.parseDouble(params.getParameter("end"));
										
										Blob partBlob = blob.slice(partStart, partEnd, contentType);
			
										partBlobs.put(name, partBlob);
									}
			
									callback.onSuccess(partBlobs);
									break;
								}
							}catch(Exception ex) {
								callback.onFailure(ex);
							}
						}, ProgressEventType.abort, ProgressEventType.load, ProgressEventType.error);
		
						overviewReader.readAsText(overviewBlob);
		
						break;
					}
				}catch(Exception ex) {
					callback.onFailure(ex);
				}
			}, ProgressEventType.abort, ProgressEventType.load, ProgressEventType.error);
	
			overviewSizeReader.readAsText(overviewSizeBlob);
		}catch(Exception ex) {
			callback.onFailure(ex);
		}
	}

	private native static int toUint16(String s) /*-{
													var n = new Number("0x"+s);
													return n & 0xFFFF
													}-*/;

	private static void getResponseInfo(XMLHttpRequest xhr, AsyncCallback<ResponseInfo> callback) {
		Blob blob = null;
		try {
			try {
				blob = getResponseBlob(xhr);
			}
			catch(ClassCastException ex) {
				String response = getResponseString(xhr);
				blob = Blob.createFromString(response, "multipart/form-data");	
			}
			
			System.err.println(blob.url());
			String contentType = xhr.getResponseHeader("Content-Type");
			String boundary = extractBoundaryFromContentType(contentType);
	
			getBlobParts(boundary, blob, new AsyncCallback<Map<String, Blob>>() {
				@Override
				public void onSuccess(Map<String, Blob> partBlobs) {
					Blob assemblyBlob = partBlobs.get("rpc-response");
					getTextFromBlob(assemblyBlob, new AsyncCallback<String>() {
						@Override
						public void onSuccess(String assemblyAsStr) {
							ResponseInfo info = new ResponseInfo();
							info.parts = partBlobs;
							info.responseText = assemblyAsStr;
							info.responseMimeType = assemblyBlob.type();
							callback.onSuccess(info);
						}
	
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});
	
				}
	
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}catch(Exception ex) {
			callback.onFailure(ex);
		}

	}

	private static String extractBoundaryFromContentType(String contentType) {
		String parts[] = contentType.split("(,|;)");
		Map<String, String> params = new HashMap<>();
		for (String part : parts) {
			part = part.trim();
			int index = part.indexOf('=');
			if (index != -1) {
				String key = part.substring(0, index);
				String value = part.substring(index + 1);
				params.put(key, value);
			}
		}

		String boundaryStr = params.get("boundary");
		return boundaryStr;
	}

	private void decode(/*RpcContext rpcContext,*/ XMLHttpRequest xhr, final boolean useBlob,
			final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, final String profilingHintBase,
			final AsyncCallback<ServiceResult> asyncCallback) {
		String contentType = xhr.getResponseHeader("Content-Type");
		if (contentType.startsWith("multipart/form-data")) {
			getResponseInfo(xhr, new AsyncCallback<ResponseInfo>() {
				@Override
				public void onSuccess(ResponseInfo result) {
					decode(/*rpcContext, xhr,*/ result.responseText, result.responseMimeType, embeddedRequiredTypesExpert,
							profilingHintBase, asyncCallback, result);
				}

				@Override
				public void onFailure(Throwable caught) {
					asyncCallback.onFailure(caught);
				}
			});
		} else {
			if (useBlob) {
				Blob blob = getResponseBlob(xhr);
				getTextFromBlob(blob, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String responseText) {
						decode(/*rpcContext, xhr,*/ responseText, contentType, embeddedRequiredTypesExpert,
								profilingHintBase, asyncCallback, null);
					}

					@Override
					public void onFailure(Throwable caught) {
						asyncCallback.onFailure(caught);
					}
				});
			} else {
				decode(/*rpcContext, xhr,*/ xhr.getResponseText(), contentType, embeddedRequiredTypesExpert,
						profilingHintBase, asyncCallback, null);
			}
		}
	}

	private void decode(/*RpcContext rpcContext, XMLHttpRequest xhr,*/ String responseText, String responseMimeType,
			final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, final String profilingHintBase,
			final AsyncCallback<ServiceResult> asyncCallback, ResponseInfo responseInfo) {
//		String contentType = xhr.getResponseHeader("Content-Type");
		if (responseMimeType != null) {
			int index = responseMimeType.indexOf(';');
			if (index > 0) {
				responseMimeType = responseMimeType.substring(0, index).trim();
			}
		}

		final GmAsyncCodec<Object, String> codec = forceXmlDecoding ? sendCodec : getCodecs().get(responseMimeType);

		if (codec == null) {
			asyncCallback.onFailure(new GmRpcException("unsupported content type: " + responseMimeType));
			return;
		} else {
			if (embeddedRequiredTypesExpert != null) {
				if (useProxyContext)
					decodeWithIntrinsicModelProxyAware(codec, responseText, embeddedRequiredTypesExpert, asyncCallback,
							profilingHintBase, responseInfo);
				else
					decodeWithIntrinsicModelLegacy(codec, responseText, embeddedRequiredTypesExpert, asyncCallback,
							profilingHintBase, responseInfo);
			} else {
				final ProfilingHandle phDecode = Profiling.start(this, profilingHintBase + " - decode (async)", true);

				StandardDecodingContext context = new StandardDecodingContext(typeEnsurer);
				context.setResponseInfo(responseInfo);
				context.setLenientDecode(true);
				codec.<ServiceResult>decodeAsync(responseText, context).get(new AsyncCallback<ServiceResult>() {
					@Override
					public void onSuccess(ServiceResult result) {						
						if(responseInfo != null) responseInfo.transfer();						
						phDecode.stop();
						asyncCallback.onSuccess(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						phDecode.stop();
						asyncCallback.onFailure(caught);
					}
				});
			}
		}
	}
	
	/*private static class AsynchronousXMLHttpRequest extends XMLHttpRequest {

		protected AsynchronousXMLHttpRequest() {
		}

		public final native void openSynchronous(String httpMethod, String url) /*-{
																				this.open(httpMethod, url, true);
																				}-/;

		public final native void openSynchronous(String httpMethod, String url, String user) /*-{
																								this.open(httpMethod, url, true, user);
																								}-/;

		public final native void openSynchronous(String httpMethod, String url, String user, String password) /*-{
																												this.open(httpMethod, url, true, user, password);
																												}-/;
	}*/
	
	private static class SynchronousXMLHttpRequest extends XMLHttpRequest {

		protected SynchronousXMLHttpRequest() {
		}

		public final native void openSynchronous(String httpMethod, String url) /*-{
																				this.open(httpMethod, url, false);
																				}-*/;

		public final native void openSynchronous(String httpMethod, String url, String user) /*-{
																								this.open(httpMethod, url, false, user);
																								}-*/;

		public final native void openSynchronous(String httpMethod, String url, String user, String password) /*-{
																												this.open(httpMethod, url, false, user, password);
																												}-*/;
	}
}
