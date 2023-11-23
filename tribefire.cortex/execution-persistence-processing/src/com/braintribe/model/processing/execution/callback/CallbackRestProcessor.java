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
package com.braintribe.model.processing.execution.callback;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.logging.Logger;
import com.braintribe.model.execution.persistence.HasCallbackSpecification;
import com.braintribe.model.execution.persistence.Job;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.weaving.impl.dispatch.DispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallbackCompletionRequest;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallbackRequest;
import com.braintribe.model.service.api.callback.AsynchronousRequestCallbackStatusRequest;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.lcd.StringTools;

/**
 * Generic REST callback processor that allows invocation of CallbackRequests at remote instances
 */
public class CallbackRestProcessor extends DispatchingServiceProcessor<AsynchronousRequestCallbackRequest, Boolean> implements LifecycleAware {

	private static final Logger logger = Logger.getLogger(CallbackRestProcessor.class);

	private HttpClientProvider httpClientProvider;
	private Marshaller marshaller;
	private CloseableHttpClient httpClient;
	private String contentType = "application/json";

	// private Supplier<PersistenceGmSession> sessionSupplier;
	private Function<String, PersistenceGmSession> sessionFactory;

	private MarshallerRegistry marshallerRegistry;

	private String cryptorSecret;

	@SuppressWarnings("unused")
	public Boolean completionCallbackRequest(ServiceRequestContext context, AsynchronousRequestCallbackCompletionRequest request) throws Exception {

		Job job = (Job) request.getResult();

		if (job instanceof HasCallbackSpecification) {

			request.setResult(cloneAndStripSources(job));

			HasCallbackSpecification hcs = (HasCallbackSpecification) job;

			String url = hcs.getCallbackRestTargetUrl();

			return postCallback(job, url, request, hcs, false);

		} else {
			return Boolean.FALSE;
		}

	}

	@SuppressWarnings("unused")
	public Boolean statusCallbackRequest(ServiceRequestContext context, AsynchronousRequestCallbackStatusRequest request) throws Exception {

		String jobId = request.getCorrelationId();
		Job job = getJob(jobId, request.getDomainId());

		if (job instanceof HasCallbackSpecification) {

			HasCallbackSpecification hcs = (HasCallbackSpecification) job;

			String url = hcs.getCallbackRestStatusTargetUrl();
			if (!StringTools.isBlank(url)) {
				return postCallback(job, url, request, hcs, true);
			}
		}
		return Boolean.FALSE;
	}

	private Job getJob(String jobId, String accessId) {
		// PersistenceGmSession session = sessionSupplier.get();
		PersistenceGmSession session = sessionFactory.apply(accessId);
		EntityQuery query = EntityQueryBuilder.from(Job.T).where().property(Job.id).eq(jobId).done();
		Job job = session.query().entities(query).first();
		return job;
	}

	private boolean postCallback(Job job, String url, AsynchronousRequestCallbackRequest request, HasCallbackSpecification callbackSpec,
			boolean ignoreError) throws Exception {
		String body = null;
		String basicAuthUser = callbackSpec.getCallbackRestBasicAuthUser();
		String basicAuthPass = callbackSpec.getCallbackRestBasicAuthPassword();
		if (!StringTools.isBlank(basicAuthPass)) {
			try {
				basicAuthPass = Cryptor.decrypt(cryptorSecret, null, null, null, basicAuthPass);
			} catch (Exception e) {
				logger.info(() -> "Error while trying to decrypt the basic authentication password.", e);
			}
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			request.setServiceId(callbackSpec.getCallbackRestTargetServiceId());
			request.setCustomData(callbackSpec.getCallbackProcessorCustomData());
			request.setDomainId(callbackSpec.getCallbackRestTargetDomain());

			request.setCorrelationId(job.getId());

			marshaller.marshall(baos, request, GmSerializationOptions.defaultOptions.derive().outputPrettiness(OutputPrettiness.high).build());
			try {
				body = baos.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new Exception("Could not serialize job: " + job, e);
			}

		} catch (Exception e) {
			logger.warn(() -> "Error while processing finished job: " + job, e);
			if (!ignoreError) {
				throw e;
			}
		}
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = getClient();

			HttpPost post = new HttpPost(url);
			post.addHeader(HTTP.CONTENT_TYPE, contentType);
			post.addHeader(HTTP.CONTENT_ENCODING, "UTF-8");
			post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

			if (!StringTools.isBlank(basicAuthUser) && !StringTools.isBlank(basicAuthPass)) {
				if (logger.isDebugEnabled())
					logger.debug("Using basic authentication with user " + basicAuthUser + " and password "
							+ StringTools.simpleObfuscatePassword(basicAuthPass));

				HttpClientContext context = HttpClientContext.create();
				String host = post.getURI().getHost();

				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(host, AuthScope.ANY_PORT),
						new UsernamePasswordCredentials(basicAuthUser, basicAuthPass));
				context.setCredentialsProvider(credentialsProvider);

				response = client.execute(post, context);
			} else {

				response = client.execute(post);
			}

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				String responseContent = HttpTools.toString(response);
				String message = "Got a non-200 response from " + url + ": " + responseContent;
				if (!ignoreError) {
					throw new Exception(message);
				} else {
					logger.debug(() -> message);
				}
			}
			ByteArrayOutputStream responseBaos = new ByteArrayOutputStream();
			try (InputStream is = new ResponseEntityInputStream(response)) {
				IOTools.pump(is, responseBaos);
			} catch (Exception e) {
				logger.error("Error while downloading response from " + url, e);
			}
			if (logger.isDebugEnabled())
				logger.debug("Received from " + url + ": " + responseBaos.toString("UTF-8"));

			job.setNotificationTimestamp(new Date());

		} catch (Exception e) {
			String message = "Could not callback " + url + " for job " + job.getId() + ": " + e.getMessage();
			if (!ignoreError) {
				logger.warn(() -> message, e);
				throw e;
			} else {
				logger.debug(() -> message, e);
			}
		} finally {
			HttpTools.consumeResponse(url, response);
			if (response != null) {
				try {
					response.close();
				} catch (Exception e) {
					logger.debug(() -> "Error while trying to close HTTP response.", e);
				}
			}
		}

		return true;
	}

	private Job cloneAndStripSources(Job conversionJob) {
		return Job.T.clone(new StandardCloningContext() {
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {

				if (ResourceSource.T.isAssignableFrom(property.getType())) {
					return false;
				}
				return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
			}
		}, conversionJob, StrategyOnCriterionMatch.skip);
	}

	protected CloseableHttpClient getClient() throws Exception {
		if (this.httpClient == null) {
			this.httpClient = this.httpClientProvider.provideHttpClient();
		}
		return this.httpClient;
	}

	@Required
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}
	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	// @Required
	// public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
	// this.sessionSupplier = sessionSupplier;
	// }
	@Required
	public void setSessionFactory(Function<String, PersistenceGmSession> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Configurable
	public void setContentType(String contentType) {
		if (!StringTools.isBlank(contentType)) {
			this.contentType = contentType;
		}
	}
	@Required
	public void setCryptorSecret(String cryptorSecret) {
		this.cryptorSecret = cryptorSecret;
	}

	@Override
	public void postConstruct() {
		marshaller = marshallerRegistry.getMarshaller(contentType);
	}
	@Override
	public void preDestroy() {
		IOTools.closeCloseable(httpClient, logger);
	}

}
