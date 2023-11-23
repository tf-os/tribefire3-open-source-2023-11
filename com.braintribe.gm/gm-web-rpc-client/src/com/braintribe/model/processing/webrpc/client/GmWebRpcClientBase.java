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
package com.braintribe.model.processing.webrpc.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.CommunicationException;
import com.braintribe.exception.HttpCommunicationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.api.RpcHeaders;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientBase;
import com.braintribe.model.processing.rpc.commons.impl.client.GmRpcClientRequestContext;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.transport.http.ClientParameters;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.ErrorHelper;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MultipartFormat;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartHeaders;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.MultipartSubFormat;
import com.braintribe.web.multipart.impl.Multiparts;

public abstract class GmWebRpcClientBase extends GmRpcClientBase {

	static final Logger logger = Logger.getLogger(GmWebRpcClientBase.class);

	private Marshaller marshaller;
	private String contentType;
	private String url;
	private boolean compress;
	private CloseableHttpClient client = null;
	private final Object clientMonitor = new Object();
	protected long retryInterval = 1000L;
	protected long callTimeout = -1;
	protected HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
	private String version = "2";
	private Integer socketTimeout = null;
	private StreamPipeFactory streamPipeFactory;

	public static final String RPC_LOGSTEP_HTTP_REQUEST = "HTTP request";
	public static final String RPC_LOGSTEP_MULTIPART = "Multiparts processing";

	protected GmWebRpcClientBase() {
	}

	protected void setConfig(BasicGmWebRpcClientConfig config) {
		super.setConfig(config);

		this.marshaller = config.getMarshaller();
		this.contentType = config.getContentType();
		this.url = config.getUrl();
		this.version = config.getVersion();
		this.streamPipeFactory = config.getStreamPipeFactory();
		super.setExecutorService(config.getExecutorService());

		if (config.getRetryInterval() != null) {
			retryInterval = config.getRetryInterval();
		}

		if (config.getCallTimeout() != null) {
			callTimeout = config.getCallTimeout();
		}

		socketTimeout = config.getSocketTimeout();

		if (config.getHttpClientProvider() != null) {
			httpClientProvider = config.getHttpClientProvider();
		}
	}

	public void close() {

		this.stopProcessing = true;

		logger.debug(() -> getClass().getSimpleName() + " to " + url + " is getting closed.");

		if (client != null) {
			synchronized (clientMonitor) {
				if (client != null) {
					try {
						client.close();
					} catch (Throwable e) {
						logger.error("Failed to close the cached http client: " + e.getMessage(), e);
					} finally {
						client = null;
					}
				}
			}
		}

	}

	protected CloseableHttpClient getClient() {
		if (client == null) {
			synchronized (clientMonitor) {

				// here we avoid reinitializing a client if it is null as a result of close()
				if (stopProcessing) {
					throw new GmRpcException("RPC client is closed: " + toString());
				}

				if (client == null) {
					try {
						ClientParameters parameters = new ClientParameters(socketTimeout);
						client = this.httpClientProvider.provideHttpClient(parameters);
					} catch (Exception e) {
						throw new RuntimeException("Could not create HTTP client with SSL Context", e);
					}
				}

			}
		}

		return client;
	}

	@Override
	protected ServiceResult sendRequest(GmRpcClientRequestContext requestContext) {

		StopWatch stopWatch = new StopWatch();
		stopWatch.intermediate(Thread.currentThread().getName());

		CloseableHttpClient requestClient = getClient();
		HttpPost post = new HttpPost(url);

		post.setHeader(RpcHeaders.rpcVersion.getHeaderName(), version);

		post.setHeader(RpcHeaders.rpcReasoning.getHeaderName(), String.valueOf(requestContext.isReasoned()));

		if (!version.equals("1"))
			post.setHeader("Accept", "multipart/chunked,multipart/form-data," + contentType);
		else
			post.setHeader("Accept", contentType);

		RpcRequestHttpEntity httpEntity = new RpcRequestHttpEntity(requestContext, version);
		post.setEntity(httpEntity);

		long callStart = System.currentTimeMillis();

		stopWatch.intermediate("Create Entity");

		while (!this.stopProcessing) {
			CloseableHttpResponse response = null;
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("Connecting to URL " + url);
				}

				try {
					requestContext.summaryLogger().startTimer(RPC_LOGSTEP_HTTP_REQUEST);
					stopWatch.intermediate("Preparation");

					response = requestClient.execute(post);
					stopWatch.intermediate("HTTP Execution");

				} catch (Exception e) {
					throw stopProcessing ? new GmRpcException("RPC client is closed: " + toString(), e) : e;
				} finally {
					requestContext.summaryLogger().stopTimer(RPC_LOGSTEP_HTTP_REQUEST);
				}

				int code = response.getStatusLine().getStatusCode();

				if (response.getFirstHeader(RpcHeaders.rpcBody.getHeaderName()) == null) {
					String msg = response.getStatusLine().getReasonPhrase();
					IOException exc = ErrorHelper.processErrorResponse(url, "POST", response, null);
					if (code == 200) {
						throw new HttpCommunicationException(code, "Missing [ " + RpcHeaders.rpcBody.getHeaderName()
								+ " ] header on successful RPC response from [" + url.toString() + "].", exc);
					} else {
						throw new HttpCommunicationException(code,
								"Unexpected [ " + code + " ] response from [ " + url.toString() + " ]: [" + msg + "].", exc);
					}
				}

				InputStream in = null;
				SequentialFormDataReader formDataReader = null;
				try {
					in = new ResponseEntityInputStream(response);
					stopWatch.intermediate("Open Entity IS");

					// // TODO: eliminate this because it is just for testing
					// FileBackedPipe pipe = new FileBackedPipe("test");
					// try (OutputStream out = pipe.openOutputStream()) {
					// IOTools.transferBytes(in, out);
					// }
					//
					// in = pipe.openInputStream();

					String responseContentType = response.getFirstHeader("Content-Type").getValue();

					MultipartFormat multipartFormat = Multiparts.parseFormat(responseContentType);

					stopWatch.intermediate("Parse MultiParts");

					boolean multiparts = multipartFormat.getSubFormat() != MultipartSubFormat.none;

					ServiceResult serviceResult = null;

					// consume streams
					if (multiparts) {

						try {
							requestContext.summaryLogger().startTimer(RPC_LOGSTEP_MULTIPART);

							formDataReader = Multiparts.buildFormDataReader(in).subFormat(multipartFormat.getSubFormat())
									.boundary(multipartFormat.getParameter("boundary")).sequential();

							PartReader part = null;

							try (StreamCache streamCache = new StreamCache(requestContext.getCallStreamCaptures(), streamPipeFactory)) {

								while ((part = formDataReader.next()) != null) {
									String name = part.getName();

									if (name.equals(RpcConstants.RPC_MAPKEY_RESPONSE)) {
										InputStream partIn = null;
										try {
											requestContext.summaryLogger().startTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);
											partIn = part.openStream();

											List<TransientSource> transientSources = new ArrayList<>();

											serviceResult = unmarshallRpcResponse(partIn, marshaller, transientSources);

											bindTransientResources(transientSources, streamCache);

											ResponseEnvelope responseEnvelope = serviceResult.asResponse();

											if (responseEnvelope != null) {
												requestContext.notifyResponse(responseEnvelope.getResult());
											}

										} finally {
											requestContext.summaryLogger().stopTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);
											IOTools.closeCloseable(partIn, name + " part input stream", requestContext.getClientLogger());
										}
									} else {
										Pair<OutputStream, Boolean> captureOutPair = streamCache.acquireStream(name);

										OutputStream captureOut = captureOutPair.first();
										boolean fresh = captureOutPair.second();
										boolean close = false;

										if (fresh) {
											close = !Boolean.TRUE.toString().equals(part.getHeader(PartHeaders.MULTIPLEXED));
										} else {
											close = Boolean.TRUE.toString().equals(part.getHeader(PartHeaders.LOGICAL_EOF));
										}

										try (InputStream partIn = part.openStream()) {
											IOTools.pump(partIn, captureOut);
										} finally {
											if (close) {
												captureOut.close();
											}
										}
									}
								}

								streamCache.checkPipeSatisfaction();

							}

						} finally {
							requestContext.summaryLogger().stopTimer(RPC_LOGSTEP_MULTIPART);
							stopWatch.intermediate("Captures");
						}

					} else {

						try {
							requestContext.summaryLogger().startTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);
							stopWatch.intermediate("Before Unmarshall");
							serviceResult = (ServiceResult) marshaller.unmarshall(in);
							stopWatch.intermediate("Unmarshall");

							ResponseEnvelope responseEnvelope = serviceResult.asResponse();

							if (responseEnvelope != null) {
								requestContext.notifyResponse(responseEnvelope.getResult());
							}
							stopWatch.intermediate("Notify Response");

						} finally {
							requestContext.summaryLogger().stopTimer(RpcConstants.RPC_LOGSTEP_UNMARSHALL_RESPONSE);
						}

					}

					return serviceResult;

				} catch (MarshallException me) {
					throw new GmRpcException("Failed to unmarshall RPC response from url [" + url.toString() + "], code ["
							+ response.getStatusLine().getStatusCode() + "]: " + response.getStatusLine().getReasonPhrase(), me);
				} finally {
					IOTools.closeCloseable(formDataReader, "form data reader", requestContext.getClientLogger());
					IOTools.closeCloseable(in, "response input stream", requestContext.getClientLogger());
				}

			} catch (NoHttpResponseException | HttpHostConnectException nhre) {
				this.evaluateRetry(callStart, nhre);
			} catch (RuntimeException e) {
				throw new RuntimeException("Received a RuntimeException while sending a request to " + url + " after "
						+ (System.currentTimeMillis() - callStart) + " ms", e);
			} catch (Error e) {
				throw new Error("Received an Error while sending a request to " + url + " after " + (System.currentTimeMillis() - callStart) + " ms",
						e);
			} catch (Exception e) {
				throw new GmRpcException("Error while sending request: " + e.getMessage() + " to " + url + " after "
						+ (System.currentTimeMillis() - callStart) + " ms", e);
			} finally {
				HttpTools.consumeResponse(url, response);
				if (logger.isTraceEnabled()) {
					logger.trace("Done connecting to URL " + url + ": " + stopWatch);
				}
				HttpTools.closeResponseQuietly(url, response);
			}
		}

		throw new GmRpcException("RPC client is closed: " + toString() + ". Won't send request to " + url);

	}

	private void bindTransientResources(List<TransientSource> transientSources, StreamCache streamCache) {
		for (TransientSource transientSource : transientSources) {
			StreamPipe pipe = streamCache.acquirePipe(transientSource.getGlobalId());
			transientSource.setInputStreamProvider(() -> pipe.openInputStream());
		}
	}

	protected void evaluateRetry(long callStart, Exception hce) {
		if (this.callTimeout <= 0) {
			throw new CommunicationException("Could not connect to host at " + url, hce);
		}
		logger.debug("Could not connect to host at " + url);
		try {
			synchronized (this) {
				Thread.sleep(this.retryInterval);
			}
		} catch (Exception ignore) {
			// Ignore
		}

		long now = System.currentTimeMillis();
		long callDuration = now - callStart;
		if (callDuration > this.callTimeout) {
			throw new CommunicationException("Could not get a connection to the server at " + url + " after " + this.callTimeout
					+ " ms (total duration: " + callDuration + " ms).");
		}

		logger.debug("Retrying now after " + this.retryInterval + " ms.");
	}

	protected void logClientConfiguration(Logger callerLogger, boolean basic) {
		Logger log = (callerLogger == null) ? logger : callerLogger;

		if (log.isDebugEnabled()) {
			String nl = System.lineSeparator();
			StringBuilder sb = new StringBuilder();
			sb.append("Configured ").append(this.toString()).append(nl);
			if (!basic) {
				sb.append("\tService ID:          ").append(serviceId).append(nl);
				sb.append("\tService Interface:   ").append(serviceInterface).append(nl);
			}
			sb.append("\tClient Instance:     ").append(clientInstanceId).append(nl);
			sb.append("\tMarshaller:          ").append(marshaller).append(nl);
			sb.append("\tContent-Type:        ").append(contentType).append(nl);
			sb.append("\tURL:                 ").append(url).append(nl);
			sb.append("\tCompress:            ").append(compress).append(nl);
			sb.append("\tRetry Interval:      ").append(retryInterval).append(nl);
			sb.append("\tCall Timeout:        ").append(callTimeout).append(nl);
			sb.append("\tHttpClient Provider: ").append(httpClientProvider).append(nl);
			sb.append("\tMeta Data Provider:  ").append(getMetaDataProvider()).append(nl);
			sb.append("\tFailure Codec:       ").append(getFailureCodec()).append(nl);
			sb.append("\tAuthorization Ctx:   ").append(getAuthorizationContext()).append(nl);
			log.debug(sb.toString());
		}
	}

	private class RpcRequestHttpEntity implements HttpEntity {
		private final GmRpcClientRequestContext requestContext;
		private final ServiceRequest request;
		private Header contentTypeHeader;
		private final Header contentEncoding = compress ? new BasicHeader(HTTP.CONTENT_ENCODING, "gzip") : null;
		private String boundary;
		private final String rpcVersion;

		public RpcRequestHttpEntity(GmRpcClientRequestContext requestContext, String rpcVersion) {
			super();
			this.requestContext = requestContext;
			this.rpcVersion = rpcVersion;
			this.request = requestContext.getServiceRequest();
		}

		public String getBoundary() {
			if (boundary == null) {
				boundary = Multiparts.generateBoundary();
			}

			return boundary;
		}

		@Override
		public Header getContentEncoding() {
			return contentEncoding;
		}

		@Deprecated
		@Override
		public void consumeContent() throws IOException {
			// ignore as it is deprecated
		}

		@Override
		public Header getContentType() {
			if (contentTypeHeader == null) {
				switch (rpcVersion) {
					case "1":
						contentTypeHeader = requestContext.hasInputResources()
								? new BasicHeader(HTTP.CONTENT_TYPE, "multipart/form-data; boundary=" + getBoundary())
								: new BasicHeader(HTTP.CONTENT_TYPE, contentType);
						break;
					default:
						contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, "multipart/chunked");
						break;
				}
			}

			return contentTypeHeader;
		}

		public boolean isMultipart() {
			switch (rpcVersion) {
				case "1":
					return requestContext.hasInputResources();
				default:
					return true;
			}
		}

		@Override
		public boolean isChunked() {
			return true;
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public long getContentLength() {
			return -1;
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writeTo(out);
			byte[] data = out.toByteArray();
			out.close();
			return new ByteArrayInputStream(data);
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			try {
				if (compress) {
					GZIPOutputStream gzipOut = new GZIPOutputStream(outstream);
					writeRequestTo(gzipOut);
					gzipOut.finish();
				} else {
					writeRequestTo(outstream);
				}
			} catch (Exception e) {
				throw new IOException("error while marshalling rpc request", e);
			}
		}

		private FormDataWriter openFormDataWriter(OutputStream out) {
			switch (rpcVersion) {
				case "1":
					return Multiparts.formDataWriter(out, getBoundary());
				default:
					return Multiparts.chunkedFormDataWriter(out);
			}
		}

		private void writeRequestTo(OutputStream outstream) throws Exception {

			try {
				requestContext.summaryLogger().startTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_REQUEST);

				if (isMultipart()) {
					try (FormDataWriter formDataWriter = openFormDataWriter(outstream)) {

						MutablePartHeader partHeader = Multiparts.newPartHeader();
						partHeader.setContentType(contentType);
						partHeader.setName(RpcConstants.RPC_MAPKEY_REQUEST);

						PartWriter requestPartWriter = formDataWriter.openPart(partHeader);
						try (OutputStream requestPartOut = requestPartWriter.outputStream()) {
							marshaller.marshall(requestPartOut, request, GmSerializationOptions.deriveDefaults()
									.set(EntityVisitorOption.class, requestContext.getEntityVisitor()).build());
						}

						for (TransientSource transientSource : requestContext.getTransientSources()) {
							String name = transientSource.getGlobalId();

							try (InputStream in = transientSource.openStream()) {
								MutablePartHeader streamingPartHeader = Multiparts.newPartHeader();
								streamingPartHeader.setContentType("application/octet-stream");
								streamingPartHeader.setName(name);

								PartWriter streamingPartWriter = formDataWriter.openPart(streamingPartHeader);
								try (OutputStream streamingPartOut = streamingPartWriter.outputStream()) {
									IOTools.transferBytes(in, streamingPartOut, IOTools.BUFFER_SUPPLIER_64K);
								}
							}
						}

					}
				} else {
					marshaller.marshall(outstream, request);
				}

			} finally {
				requestContext.summaryLogger().stopTimer(RpcConstants.RPC_LOGSTEP_MARSHALL_REQUEST);
			}

		}

		@Override
		public boolean isStreaming() {
			return true;
		}

	}

	protected String getUrl() {
		return url;
	}

}
