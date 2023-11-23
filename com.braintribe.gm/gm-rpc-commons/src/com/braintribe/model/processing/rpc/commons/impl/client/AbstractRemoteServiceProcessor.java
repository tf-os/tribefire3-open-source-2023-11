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
package com.braintribe.model.processing.rpc.commons.impl.client;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.options.GmDeserializationContextBuilder;
import com.braintribe.codec.marshaller.api.options.attributes.DecodingLenienceOption;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.StopWatch;

public abstract class AbstractRemoteServiceProcessor implements ReasonedServiceProcessor<ServiceRequest, Object> {
	private Codec<Throwable, Failure> failureCodec = FailureCodec.INSTANCE;

	protected InstanceId clientInstanceId;
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected static ThreadRenamer threadRenamer;

	protected AbstractRemoteServiceProcessor() {
	}

	@Configurable
	public void setClientInstanceId(InstanceId clientInstanceId) {
		this.clientInstanceId = clientInstanceId;
	}

	public void setFailureCodec(Codec<Throwable, Failure> failureCodec) {
		Objects.requireNonNull(failureCodec, "failureCodec cannot be set to null");
		this.failureCodec = failureCodec;
	}

	protected abstract ServiceResult sendRequest(GmRpcClientRequestContext requestContext);

	protected abstract Logger logger();

	@Override
	public Maybe<Object> processReasoned(ServiceRequestContext requestContext, ServiceRequest request) {
		ServiceRequestSummaryLogger summaryLogger = requestContext.summaryLogger();
		StopWatch stopWatch = new StopWatch();
		stopWatch.intermediate(Thread.currentThread().getName());

		GmRpcClientRequestContext clientRequestContext = new GmRpcClientRequestContext(request, request.type().getTypeSignature(), logger(), false);
		clientRequestContext.setReasoned(true);
		clientRequestContext.setAttributeContext(requestContext);

		ServiceResult response = null;
		Logger log = logger();

		try {
			transferSessionId(requestContext, request);
			response = sendRequest(clientRequestContext);

			stopWatch.intermediate("Send Request");

			Objects.requireNonNull(response, "Unexpected null rpc response");

			switch (response.resultType()) {
				case success:
					return Maybe.complete(response.asResponse().getResult());

				case unsatisfied:
					return response.asUnsatisfied().toMaby();

				case failure: {
					Throwable exception = decodeFailure(response.asFailure());
					stopWatch.intermediate("Decode Failure");
					throw Exceptions.unchecked(exception, "Received Failure from remote request: " + request);
				}

				default:
					throw new IllegalStateException("Unexpected result type: " + response.resultType());
			}

		} finally {
			summaryLogger.log(this, request);
			summaryLogger.logOneLine(RpcConstants.RPC_LOGSTEP_FINAL, request);

			log.trace(() -> "RpcEvalContext.invoke: " + stopWatch);
		}

	}

	private void transferSessionId(ServiceRequestContext requestContext, ServiceRequest request) {
		if (request.supportsAuthentication()) {
			AuthorizableRequest authorizableRequest = (AuthorizableRequest) request;
			if (authorizableRequest.getSessionId() == null) {
				requestContext.findAttribute(UserSessionAspect.class).map(UserSession::getSessionId).ifPresent(authorizableRequest::setSessionId);
			}
		}
	}

	protected Throwable decodeFailure(Failure failure) {
		return failureCodec.decode(failure);
	}

	protected ServiceResult unmarshallRpcResponse(InputStream in, Marshaller marshaller, List<TransientSource> resources) throws MarshallException {
		return unmarshallRpcResponse(AttributeContexts.peek(), in, marshaller, resources);
	}

	protected ServiceResult unmarshallRpcResponse(AttributeContext attributeContext, InputStream in, Marshaller marshaller,
			List<TransientSource> resources) throws MarshallException {

		 GmDeserializationContextBuilder builder = GmDeserializationOptions.deriveDefaults() //
				.set(EntityVisitorOption.class, entity -> {
					if (entity instanceof TransientSource)
						resources.add((TransientSource) entity);
				});

		 DecodingLenience lenience = attributeContext.findOrNull(DecodingLenienceOption.class);
		 if (lenience != null)
			 builder.set(DecodingLenienceOption.class, lenience);
		 
			ServiceResult response = (ServiceResult) marshaller.unmarshall(in, builder.build());

		return response;
	}

	protected ThreadRenamer threadRenamer() {
		if (threadRenamer == null) {
			boolean enabled = Boolean.valueOf(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_THREAD_RENAMING, "true"));
			threadRenamer = new ThreadRenamer(enabled);
		}
		return threadRenamer;
	}

}
