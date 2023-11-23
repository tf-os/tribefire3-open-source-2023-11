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
package com.braintribe.model.processing.rpc.test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.stream.StreamProviders;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.commons.DataSize;
import com.braintribe.model.processing.rpc.test.commons.StreamingTools;
import com.braintribe.model.processing.rpc.test.commons.StreamingTools.RandomDataStore;
import com.braintribe.model.processing.rpc.test.commons.TestContext;
import com.braintribe.model.processing.rpc.test.commons.TestContextBuilder;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestService;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestServiceRequest;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestServiceResponse;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestService;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestServiceRequest;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestServiceRequestEncrypted;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestServiceResponse;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorResponse;
import com.braintribe.model.processing.rpc.test.service.processor.failure.FailureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessorResponse;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessorResponse;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessorResponse;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessorResponse;
import com.braintribe.model.processing.rpc.test.wire.contract.RpcTestContract;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * <p>
 * Base class for RPC request tests against services defined in
 * {@code com.braintribe.model.processing.rpc.test.service.*}.
 * 
 */
public abstract class RpcTestBase {

	private static final Logger log = Logger.getLogger(RpcTestBase.class);

	protected static int THREAD_POOL_SIZE = 5;
	protected static int MAX_CONCURRENT_TESTS = 5;
	protected static long CONCURRENT_TESTS_TIMEOUT = 5;
	protected static int MAX_SEQUENTIAL_TESTS = 3;
	protected static long ASYNC_TESTS_TIMEOUT = 10;

	// protected static TimeUnit CONCURRENT_TESTS_TIMEOUT_UNIT = TimeUnit.MINUTES;
	// protected static TimeUnit ASYNC_TESTS_TIMEOUT_UNIT = TimeUnit.MINUTES;
	// @Rule
	// public Timeout globalTimeout = Timeout.seconds(30L);

	// START_DEBUG

	protected static TimeUnit CONCURRENT_TESTS_TIMEOUT_UNIT = TimeUnit.HOURS;
	protected static TimeUnit ASYNC_TESTS_TIMEOUT_UNIT = TimeUnit.HOURS;

	@Rule
	public Timeout globalTimeout = Timeout.seconds(30000L);

	// END_DEBUG

	// protected static TimeUnit CONCURRENT_TESTS_TIMEOUT_UNIT = TimeUnit.HOURS;
	// protected static TimeUnit ASYNC_TESTS_TIMEOUT_UNIT = TimeUnit.HOURS;

	// ================================= //
	// ======== IMPL. SPECIFIC ========= //
	// ================================= //

	public abstract <S> S createService(GmRpcClientConfig clientConfig);

	public abstract <S> void destroyService(S service);

	public abstract RpcTestContract rpcTestBeans();

	// ================================================= //
	// ========= GENERIC TESTS - IFACE BASED =========== //
	// ================================================= //

	protected void testBasicServiceRequest(boolean encryptedResponse, boolean forceReAuthorization) throws Exception {
		testBasicServiceRequest(encryptedResponse, forceReAuthorization, false, 1);
	}

	protected void testBasicServiceRequest(boolean encryptedResponse, boolean forceReAuthorization, boolean multiThreaded, int numTests)
			throws Exception {

		GmRpcClientConfig clientConfig = null;
		if (forceReAuthorization) {
			clientConfig = rpcTestBeans().basicReauthorizable();
		} else {
			clientConfig = rpcTestBeans().basic();
		}

		BasicTestService testService = createService(clientConfig);

		try {
			if (!multiThreaded) {
				while (numTests-- > 0) {
					testBasicServiceRequest(testService, encryptedResponse, forceReAuthorization, multiThreaded);
				}
			} else {
				testBasicServiceRequestConcurrently(testService, encryptedResponse, forceReAuthorization, numTests);
			}
		} finally {
			destroyService(testService);
		}

	}

	protected void testBasicServiceRequestConcurrently(final BasicTestService testService, final boolean encryptedResponse,
			final boolean forceReAuthorization, int threads) throws Exception {

		Set<TestCaller> callers = new HashSet<>();

		for (int i = 0; i < threads; i++) {
			callers.add(new TestCaller() {
				@Override
				public void test() throws Throwable {
					testBasicServiceRequest(testService, encryptedResponse, forceReAuthorization, true);
				}
			});
		}

		testConcurrently(callers, THREAD_POOL_SIZE);

	}

	protected void testBasicServiceRequest(BasicTestService testService, boolean encryptedResponse, boolean forceReAuthorization,
			boolean multiThreaded) throws Exception {

		BasicTestServiceRequest request = BasicTestServiceRequest.T.create();
		request.setRequestId(UUID.randomUUID().toString());
		request.setRequestDate(new Date());

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		BasicTestServiceResponse response = null;
		if (encryptedResponse) {
			response = testService.testEncrypted(request);
		} else {
			response = testService.test(request);
		}

		Assert.assertNotNull(response);
		Assert.assertEquals(request.getRequestId(), response.getRequestId());
		Assert.assertNotNull(response.getResponseDate());

		if (!multiThreaded && forceReAuthorization) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected void testStreamingServiceRequest(boolean encryptedResponse, boolean forceReAuthorization) throws Exception {
		testStreamingServiceRequest(encryptedResponse, forceReAuthorization, false, 1);
	}

	protected void testStreamingServiceRequest(boolean encryptedResponse, boolean forceReAuthorization, boolean multiThreaded, int numTests)
			throws Exception {

		GmRpcClientConfig clientConfig = null;
		if (forceReAuthorization) {
			clientConfig = rpcTestBeans().streamingReauthorizable();
		} else {
			clientConfig = rpcTestBeans().streaming();
		}

		StreamingTestService testService = createService(clientConfig);

		try {
			if (!multiThreaded) {
				while (numTests-- > 0) {
					testStreamingServiceRequest(testService, encryptedResponse, forceReAuthorization, multiThreaded);
				}
			} else {
				testStreamingServiceRequestConcurrently(testService, encryptedResponse, forceReAuthorization, numTests);
			}
		} finally {
			destroyService(testService);
		}

	}

	protected void testStreamingServiceRequestConcurrently(final StreamingTestService testService, final boolean encryptedResponse,
			final boolean forceReAuthorization, int threads) throws Exception {

		Set<TestCaller> callers = new HashSet<>();

		for (int i = 0; i < threads; i++) {
			callers.add(new TestCaller() {
				@Override
				public void test() throws Throwable {
					testStreamingServiceRequest(testService, encryptedResponse, forceReAuthorization, true);
				}
			});
		}

		testConcurrently(callers, THREAD_POOL_SIZE);

	}

	protected void testStreamingServiceRequest(StreamingTestService testService, boolean encryptedResponse, boolean forceReAuthorization,
			boolean multiThreaded) throws Exception {

		StreamingTestServiceRequest request = null;
		if (encryptedResponse) {
			request = StreamingTestServiceRequestEncrypted.T.create();
		} else {
			request = StreamingTestServiceRequest.T.create();
		}

		request.setRequestDate(new Date());

		Resource resource1 = StreamingTools.createResource();
		Resource resource2 = StreamingTools.createResource();

		request.setResource1(resource1);
		request.setResource2(resource2);

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();

		request.setCapture1(callStreamCapture(out1));
		request.setCapture2(callStreamCapture(out2));

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		StreamingTestServiceResponse response = null;
		if (encryptedResponse) {
			response = testService.testEncrypted(request);
		} else {
			response = testService.test(request);
		}

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getResponseDate());

		StreamingTools.checkOutput(out1, response.getCapture1Md5());
		StreamingTools.checkOutput(out2, response.getCapture2Md5());

		StreamingTools.checkResource(response.getResource1());
		StreamingTools.checkResource(response.getResource2());

		if (!multiThreaded && forceReAuthorization) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	// ===================================================================== //
	// ========= GENERIC PARAMETERIZED TESTS - DENOTATION DRIVEN =========== //
	// ===================================================================== //

	public <T extends ServiceRequest> Tester<T> with(EntityType<T> requestType) {
		Tester<T> tester = new RpcTestBase.Tester<>(requestType);
		return tester;
	}

	public class Tester<T extends ServiceRequest> {

		EntityType<T> requestType;
		boolean forceReAuthorization;
		boolean async;
		boolean multiThreaded;
		int numTests = 1;
		boolean addResponseConsumer;
		boolean notifyEagerResponse;

		public Tester(EntityType<T> requestType) {
			this.requestType = requestType;
		}

		public Tester<T> forceReAuthorization() {
			this.forceReAuthorization = true;
			return this;
		}

		public Tester<T> async() {
			this.async = true;
			return this;
		}

		public Tester<T> multiThreaded() {
			this.multiThreaded = true;
			return this;
		}

		public Tester<T> runs(int numTests) {
			this.numTests = numTests;
			return this;
		}

		public Tester<T> notifyEagerResponse() {
			this.notifyEagerResponse = true;
			return this;
		}

		public Tester<T> addResponseConsumer() {
			this.addResponseConsumer = true;
			return this;
		}

		public void test() throws Exception {
			testServiceProcessorRequest(requestType, forceReAuthorization, async, multiThreaded, numTests, notifyEagerResponse, addResponseConsumer);
		}

	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(EntityType<T> requestType, boolean forceReAuthorization) throws Exception {
		testServiceProcessorRequest(requestType, forceReAuthorization, false, false, 1, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(TestContext context, EntityType<T> requestType,
			boolean forceReAuthorization) throws Exception {
		testServiceProcessorRequest(context, requestType, forceReAuthorization, false, false, 1, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequestAsync(EntityType<T> requestType, boolean forceReAuthorization)
			throws Exception {
		testServiceProcessorRequest(requestType, forceReAuthorization, true, false, 1, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(EntityType<T> requestType, boolean forceReAuthorization, boolean async)
			throws Exception {
		testServiceProcessorRequest(requestType, forceReAuthorization, async, false, 1, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(EntityType<T> requestType, boolean forceReAuthorization,
			boolean multiThreaded, int numTests) throws Exception {
		testServiceProcessorRequest(requestType, forceReAuthorization, false, multiThreaded, numTests, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequestAsync(EntityType<T> requestType, boolean forceReAuthorization,
			boolean multiThreaded, int numTests) throws Exception {
		testServiceProcessorRequest(requestType, forceReAuthorization, true, multiThreaded, numTests, false, false);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(EntityType<T> requestType, boolean forceReAuthorization, boolean async,
			boolean multiThreaded, int numTests, boolean notifyEagerResponse, boolean addConsumer) throws Exception {
		TestContext context = TestContextBuilder.create();
		testServiceProcessorRequest(context, requestType, forceReAuthorization, async, multiThreaded, numTests, notifyEagerResponse, addConsumer);
	}

	protected <T extends ServiceRequest> void testServiceProcessorRequest(TestContext context, EntityType<T> requestType,
			boolean forceReAuthorization, boolean async, boolean multiThreaded, int numTests, boolean notifyEagerResponse, boolean addConsumer)
			throws Exception {

		GmRpcClientConfig config = null;
		if (forceReAuthorization) {
			config = rpcTestBeans().denotationDrivenReauthorizable();
		} else {
			config = rpcTestBeans().denotationDriven();
		}

		Evaluator<ServiceRequest> evaluator = createService(config);

		try {
			if (!multiThreaded) {
				while (numTests-- > 0) {
					testServiceProcessorRequest(context, requestType, evaluator, forceReAuthorization, async, multiThreaded, notifyEagerResponse,
							addConsumer);
				}
			} else {
				testServiceProcessorRequestConcurrently(requestType, evaluator, forceReAuthorization, async, numTests, notifyEagerResponse,
						addConsumer);
			}
		} finally {
			destroyService(evaluator);
		}

	}

	private <T extends ServiceRequest> void testServiceProcessorRequestConcurrently(final EntityType<T> requestType,
			final Evaluator<ServiceRequest> testService, final boolean forceReAuthorization, final boolean async, int threads,
			boolean notifyEagerResponse, boolean addConsumer) throws Exception {

		Set<TestCaller> callers = new HashSet<>();

		for (int i = 0; i < threads; i++) {
			callers.add(new TestCaller() {
				@Override
				public void test() throws Throwable {
					testServiceProcessorRequest(requestType, testService, forceReAuthorization, async, true, notifyEagerResponse, addConsumer);
				}
			});
		}

		testConcurrently(callers, THREAD_POOL_SIZE);

	}

	private <T extends ServiceRequest> void testServiceProcessorRequest(EntityType<T> requestType, Evaluator<ServiceRequest> testService,
			boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse, boolean addConsumer) throws Exception {
		TestContext context = TestContextBuilder.create();
		testServiceProcessorRequest(context, requestType, testService, forceReAuthorization, async, multiThreaded, notifyEagerResponse, addConsumer);
	}

	@SuppressWarnings("unchecked")
	private <T extends ServiceRequest> void testServiceProcessorRequest(TestContext context, EntityType<T> requestType,
			Evaluator<ServiceRequest> testService, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		if (BasicTestServiceProcessorRequest.T.isAssignableFrom(requestType)) {
			testBasicServiceProcessorRequest((EntityType<? extends BasicTestServiceProcessorRequest>) requestType, testService, forceReAuthorization,
					async, multiThreaded, notifyEagerResponse, addConsumer);
		} else if (StreamingTestServiceProcessorRequest.T.isAssignableFrom(requestType)) {
			testStreamingServiceProcessorRequest((EntityType<? extends StreamingTestServiceProcessorRequest>) requestType, testService,
					forceReAuthorization, async, multiThreaded, notifyEagerResponse, addConsumer);
		} else if (UploadTestServiceProcessorRequest.T.isAssignableFrom(requestType)) {
			testUploadServiceProcessorRequest(context, (EntityType<? extends UploadTestServiceProcessorRequest>) requestType, testService,
					forceReAuthorization, async, multiThreaded, notifyEagerResponse, addConsumer);
		} else if (DownloadResourceTestServiceProcessorRequest.T.isAssignableFrom(requestType)) {
			testDownloadResourceServiceProcessorRequest((EntityType<? extends DownloadResourceTestServiceProcessorRequest>) requestType, testService,
					forceReAuthorization, async, multiThreaded, notifyEagerResponse, addConsumer);
		} else if (DownloadCaptureTestServiceProcessorRequest.T.isAssignableFrom(requestType)) {
			testDownloadCaptureServiceProcessorRequest((EntityType<? extends DownloadCaptureTestServiceProcessorRequest>) requestType, testService,
					forceReAuthorization, async, multiThreaded, notifyEagerResponse, addConsumer);
		} else if (CompositeRequest.T.isAssignableFrom(requestType)) {
			testCompositeServiceProcessorRequest((EntityType<? extends CompositeRequest>) requestType, testService, forceReAuthorization, async,
					multiThreaded);
		} else {
			throw new RuntimeException("Unsupported request type: " + requestType);
		}

	}

	protected <T extends BasicTestServiceProcessorRequest> void testBasicServiceProcessorRequest(EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		BasicTestServiceProcessorRequest request = requestType.create();
		request.setRequestId(UUID.randomUUID().toString());
		request.setRequestDate(new Date());
		request.setRespondEagerly(notifyEagerResponse);

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<BasicTestServiceProcessorResponse> responseContext = evaluator.eval(request);

		ResponseConsumer eagerResponseConsumer = null;

		if (notifyEagerResponse || addConsumer) {
			eagerResponseConsumer = new ResponseConsumer(notifyEagerResponse);
			responseContext.with(ResponseConsumerAspect.class, eagerResponseConsumer);
		}

		BasicTestServiceProcessorResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			response = responseContext.get();
		}

		Assert.assertNotNull("The response shouldn't have been null", response);

		if (notifyEagerResponse || addConsumer) {
			BasicTestServiceProcessorResponse eagerResponse = eagerResponseConsumer.getEagerResponse();
			Assert.assertNotNull("The eager response consumer should have received a non-null response", eagerResponse);
			Assert.assertEquals("The response returned by the get() method should have been the same as the one notified to the eager consumer",
					eagerResponse, response);
			Assert.assertEquals(notifyEagerResponse, eagerResponse.getEager());
			response = eagerResponse;
		}

		Assert.assertEquals(request.getRequestId(), response.getRequestId());
		Assert.assertNotNull(response.getResponseDate());

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected <T extends StreamingTestServiceProcessorRequest> void testStreamingServiceProcessorRequest(EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		StreamingTestServiceProcessorRequest request = requestType.create();

		request.setRequestDate(new Date());
		request.setRespondEagerly(notifyEagerResponse);

		Resource resource1 = StreamingTools.createResource();
		Resource resource2 = StreamingTools.createResource();

		request.setResource1(resource1);
		request.setResource2(resource2);

		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();

		request.setCapture1(callStreamCapture(out1));
		request.setCapture2(callStreamCapture(out2));

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<StreamingTestServiceProcessorResponse> responseContext = evaluator.eval(request);

		ResponseConsumer eagerResponseConsumer = null;

		if (notifyEagerResponse || addConsumer) {
			eagerResponseConsumer = new ResponseConsumer(notifyEagerResponse, out1, out2);
			responseContext.with(ResponseConsumerAspect.class, eagerResponseConsumer);
		}

		StreamingTestServiceProcessorResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			response = responseContext.get();
		}

		Assert.assertNotNull("The response shouldn't have been null", response);

		if (notifyEagerResponse || addConsumer) {
			StreamingTestServiceProcessorResponse eagerResponse = eagerResponseConsumer.getEagerResponse();
			Assert.assertNotNull("The eager response consumer should have received a non-null response", eagerResponse);
			Assert.assertEquals("The response returned by the get() method should have been the same as the one notified to the eager consumer",
					eagerResponse, response);
			Assert.assertEquals(notifyEagerResponse, eagerResponse.getEager());
			response = eagerResponse;
		}

		assertStreamingTestServiceProcessorResponse(response, out1, out2);

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected <T extends CompositeRequest> void testCompositeServiceProcessorRequest(EntityType<T> requestType, Evaluator<ServiceRequest> evaluator,
			boolean forceReAuthorization, boolean async, boolean multiThreaded) throws Exception {

		CompositeRequest request = requestType.create();
		request.setParallelize(false);

		StreamingTestServiceProcessorRequestInfo[] streamingRequestInfos = new StreamingTestServiceProcessorRequestInfo[2];

		for (int i = 0; i < 2; i++) {
			streamingRequestInfos[i] = new StreamingTestServiceProcessorRequestInfo();
			request.getRequests().add(streamingRequestInfos[i].request);
		}

		for (int i = 0; i < 2; i++) {
			BasicTestServiceProcessorRequest basicRequest = BasicTestServiceProcessorRequest.T.create();
			basicRequest.setRequestId(UUID.randomUUID().toString());
			basicRequest.setRequestDate(new Date());
			request.getRequests().add(basicRequest);
		}

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<CompositeResponse> responseContext = evaluator.eval(request);
		CompositeResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			response = responseContext.get();
		}

		Assert.assertNotNull("The CompositeResponse shouldn't have been null", response);
		Assert.assertNotNull("CompositeResponse.getResults() shouldn't have been null", response.getResults());
		Assert.assertEquals("Unexpected CompositeResponse.getResults() size", 4, response.getResults().size());

		Assert.assertEquals(request.getRequests().size(), response.getResults().size());

		for (int i = 0; i < request.getRequests().size(); i++) {

			ServiceRequest serviceRequest = request.getRequests().get(i);

			ServiceResult result = response.getResults().get(i);
			Assert.assertTrue(result instanceof ResponseEnvelope);
			ResponseEnvelope standardResult = (ResponseEnvelope) result;
			Assert.assertNotNull(standardResult.getResult());

			if (serviceRequest instanceof StreamingTestServiceProcessorRequest) {
				Assert.assertTrue(standardResult.getResult() instanceof StreamingTestServiceProcessorResponse);
				assertStreamingTestServiceProcessorResponse((StreamingTestServiceProcessorResponse) standardResult.getResult(),
						streamingRequestInfos[i].capture1, streamingRequestInfos[i].capture2);
			} else if (serviceRequest instanceof BasicTestServiceProcessorRequest) {
				Assert.assertTrue(standardResult.getResult() instanceof BasicTestServiceProcessorResponse);
				BasicTestServiceProcessorResponse basicResponse = (BasicTestServiceProcessorResponse) standardResult.getResult();
				BasicTestServiceProcessorRequest basicRequest = (BasicTestServiceProcessorRequest) serviceRequest;
				Assert.assertNotNull(basicResponse);
				Assert.assertEquals(basicRequest.getRequestId(), basicResponse.getRequestId());
				Assert.assertNotNull(basicResponse.getResponseDate());
			} else {
				Assert.fail("Unexpected request type");
			}

		}

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected <T extends DownloadCaptureTestServiceProcessorRequest> void testDownloadCaptureServiceProcessorRequest(EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		String resourceId = RandomDataStore.getExistingId();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		DownloadCaptureTestServiceProcessorRequest request = requestType.create();
		request.setResourceId(resourceId);
		request.setCapture(callStreamCapture(output));
		request.setRespondEagerly(notifyEagerResponse);

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<DownloadCaptureTestServiceProcessorResponse> responseContext = evaluator.eval(request);

		ResponseConsumer eagerResponseConsumer = null;

		if (notifyEagerResponse || addConsumer) {
			eagerResponseConsumer = new ResponseConsumer(notifyEagerResponse, output);
			responseContext.with(ResponseConsumerAspect.class, eagerResponseConsumer);
		}

		DownloadCaptureTestServiceProcessorResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			response = responseContext.get();
		}

		Assert.assertNotNull("The response shouldn't have been null", response);

		if (notifyEagerResponse || addConsumer) {
			DownloadCaptureTestServiceProcessorResponse eagerResponse = eagerResponseConsumer.getEagerResponse();
			Assert.assertNotNull("The eager response consumer should have received a non-null response", eagerResponse);
			Assert.assertEquals("The response returned by the get() method should have been the same as the one notified to the eager consumer",
					eagerResponse, response);
			response = eagerResponse;
		}

		StreamingTools.checkOutput(output, resourceId);

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected <T extends DownloadResourceTestServiceProcessorRequest> void testDownloadResourceServiceProcessorRequest(EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		String resourceId = RandomDataStore.getExistingId();

		DownloadResourceTestServiceProcessorRequest request = requestType.create();
		request.setResourceId(resourceId);
		request.setRespondEagerly(notifyEagerResponse);

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<DownloadResourceTestServiceProcessorResponse> responseContext = evaluator.eval(request);

		ResponseConsumer eagerResponseConsumer = null;

		if (notifyEagerResponse || addConsumer) {
			eagerResponseConsumer = new ResponseConsumer(notifyEagerResponse);
			responseContext.with(ResponseConsumerAspect.class, eagerResponseConsumer);
		}

		DownloadResourceTestServiceProcessorResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			response = responseContext.get();
		}

		Assert.assertNotNull("The response shouldn't have been null", response);

		if (notifyEagerResponse || addConsumer) {
			DownloadResourceTestServiceProcessorResponse eagerResponse = eagerResponseConsumer.getEagerResponse();
			Assert.assertNotNull("The eager response consumer should have received a non-null response", eagerResponse);
			Assert.assertEquals("The response returned by the get() method should have been the same as the one notified to the eager consumer",
					eagerResponse, response);
			response = eagerResponse;
		}

		StreamingTools.checkResource(response.getResource());

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	protected <T extends UploadTestServiceProcessorRequest> void testUploadServiceProcessorRequest(EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {
		testUploadServiceProcessorRequest(TestContextBuilder.create(), requestType, evaluator, forceReAuthorization, async, multiThreaded,
				notifyEagerResponse, addConsumer);
	}

	protected <T extends UploadTestServiceProcessorRequest> void testUploadServiceProcessorRequest(TestContext context, EntityType<T> requestType,
			Evaluator<ServiceRequest> evaluator, boolean forceReAuthorization, boolean async, boolean multiThreaded, boolean notifyEagerResponse,
			boolean addConsumer) throws Exception {

		Integer dataSize = context.get(DataSize.class);

		Resource uploadResource = dataSize != null ? StreamingTools.createResource(dataSize) : StreamingTools.createResource();

		UploadTestServiceProcessorRequest request = requestType.create();
		request.setResource(uploadResource);
		request.setRespondEagerly(notifyEagerResponse);

		if (forceReAuthorization) {
			invalidateCurrentUserSession();
		}

		EvalContext<UploadTestServiceProcessorResponse> responseContext = evaluator.eval(request);

		ResponseConsumer eagerResponseConsumer = null;

		if (notifyEagerResponse || addConsumer) {
			eagerResponseConsumer = new ResponseConsumer(notifyEagerResponse);
			responseContext.with(ResponseConsumerAspect.class, eagerResponseConsumer);
		}

		UploadTestServiceProcessorResponse response = null;
		if (async) {
			BlockingAsyncCallback callback = new BlockingAsyncCallback();
			responseContext.get(callback);
			response = callback.get();
		} else {
			long s = System.nanoTime();
			response = responseContext.get();
			long e = System.nanoTime();
			double d = (e - s) / 1_000_000d;
			System.out.println("call took: " + d + "ms");
		}

		Assert.assertNotNull("The response shouldn't have been null", response);

		if (notifyEagerResponse || addConsumer) {
			UploadTestServiceProcessorResponse eagerResponse = eagerResponseConsumer.getEagerResponse();
			Assert.assertNotNull("The eager response consumer should have received a non-null response", eagerResponse);
			Assert.assertEquals("The response returned by the get() method should have been the same as the one notified to the eager consumer",
					eagerResponse, response);
			response = eagerResponse;
		}

		Assert.assertNotNull(response.getResourceId());
		Assert.assertEquals(uploadResource.getMd5(), response.getResourceId());

		if (forceReAuthorization && !multiThreaded && !async) {
			Assert.assertFalse(getNotifiedAuthorizationFailures().isEmpty());
		}

	}

	// ===================================================================== //
	// ========= ERROR HANDLING TESTS - DENOTATION DRIVEN ================== //
	// ===================================================================== //

	protected void testFailedEvaluatorRequest(Class<? extends Throwable> exceptionType) throws Exception {

		Evaluator<ServiceRequest> evaluator = null;
		try {
			evaluator = createService(rpcTestBeans().denotationDriven());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		FailureTestServiceProcessorRequest request = FailureTestServiceProcessorRequest.T.create();
		request.setExceptionType(exceptionType.getName());

		request.eval(evaluator).get();

		Assert.fail(request + " evaluation should have failed.");

	}

	protected void testUnauthorizedEvaluatorRequest() {

		Evaluator<ServiceRequest> evaluator = null;
		try {
			evaluator = createService(rpcTestBeans().denotationDriven());
			invalidateCurrentUserSession();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		FailureTestServiceProcessorRequest request = FailureTestServiceProcessorRequest.T.create();
		try {
			Maybe<?> maybe = request.eval(evaluator).getReasoned();

			if (maybe.isUnsatisfied()) {
				// Resets the user session provider state for the sake of the subsequent calls.
				resetUserSession(new ReasonException(maybe.whyUnsatisfied()));

				if (!maybe.isUnsatisfiedBy(AuthenticationFailure.T))
					Assertions.fail("Unexpected reason received: " + maybe.whyUnsatisfied().stringify());

			} else {
				Assertions.fail("Unexpectedly executed authorized request");
			}
		} catch (RuntimeException e) {
			resetUserSession(e); // Resets the user session provider state for the sake of the subsequent calls.
			throw e;
		}

	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	private UserSession invalidateCurrentUserSession() {

		UserSession userSession = rpcTestBeans().currentUserSessionInvalidator().get();

		return userSession;

	}

	private void resetUserSession(Throwable userSessionResetTrigger) {

		rpcTestBeans().authorizationFailureConsumer().accept(userSessionResetTrigger);

	}

	protected Set<Throwable> getNotifiedAuthorizationFailures() throws Exception {

		Set<Throwable> failures = rpcTestBeans().currentAuthorizationFailures();

		return failures;

	}

	protected void testConcurrently(Set<TestCaller> tests, int threadPoolSize) throws Exception {

		ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize, new NamedPoolThreadFactory(RpcTestBase.class.getSimpleName()));

		try {
			List<Future<Throwable>> results = executorService.invokeAll(tests, CONCURRENT_TESTS_TIMEOUT, CONCURRENT_TESTS_TIMEOUT_UNIT);

			List<Throwable> errors = new ArrayList<Throwable>();

			for (Future<Throwable> result : results) {
				Throwable error = null;

				try {
					error = result.get();
				} catch (CancellationException e) {
					System.out.println("Test cancelled as it didn't complete after " + CONCURRENT_TESTS_TIMEOUT + " "
							+ CONCURRENT_TESTS_TIMEOUT_UNIT.toString().toLowerCase());
					continue;
				}

				if (error != null) {
					errors.add(error);
				}
			}

			if (errors.isEmpty()) {
				System.out.println(tests.size() + " concurrent tests completed successfully.");
			} else {
				AssertionError error = new AssertionError("From " + tests.size() + " concurrent tests, " + errors.size() + " failed.");
				for (Throwable cause : errors) {
					error.addSuppressed(cause);
				}
				throw error;
			}
		} finally {
			executorService.shutdownNow();
		}

	}

	public abstract class TestCaller implements Callable<Throwable> {

		public abstract void test() throws Throwable;

		@Override
		public Throwable call() throws Exception {
			try {
				test();
				return null;
			} catch (Throwable e) {
				return e;
			}
		}

	}

	/**
	 * <p>
	 * A {@link ThreadFactory} allowing a custom name for the generated Thread(s).
	 */
	static class NamedPoolThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		NamedPoolThreadFactory(String poolName) {
			group = Thread.currentThread().getThreadGroup();
			namePrefix = poolName + "-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (!t.isDaemon()) {
				t.setDaemon(true);
			}
			t.setName(namePrefix + threadNumber.getAndIncrement());

			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}

	}

	protected class BlockingAsyncCallback implements AsyncCallback<Object> {

		private final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);

		@Override
		public void onSuccess(Object future) {
			try {
				queue.put(future);
			} catch (InterruptedException e) {
				log.warn("BlockingAsyncCallback.onSuccess() interrupted on put()", e);
			}
		}

		@Override
		public void onFailure(Throwable t) {
			t.printStackTrace();
			try {
				queue.put(t);
			} catch (InterruptedException e) {
				log.warn("BlockingAsyncCallback.onFailure() interrupted on put()", e);
			}
		}

		public <S> S get() throws Exception {
			return get(ASYNC_TESTS_TIMEOUT, ASYNC_TESTS_TIMEOUT_UNIT);
		}

		public <S> S get(long timeout, TimeUnit unit) throws Exception {
			Object r = queue.poll(timeout, unit);
			if (r == null) {
				log.warn("No response was received by the BlockingAsyncCallback after " + timeout + " " + unit);
			} else if (r instanceof Throwable) {
				Throwable t = (Throwable) r;
				throw Exceptions.unchecked(t);
			}
			return (S) r;
		}

	}

	protected static class StreamingTestServiceProcessorRequestInfo {

		StreamingTestServiceProcessorRequest request;
		ByteArrayOutputStream capture1;
		ByteArrayOutputStream capture2;

		public StreamingTestServiceProcessorRequestInfo() throws Exception {

			request = StreamingTestServiceProcessorRequest.T.create();

			request.setRequestDate(new Date());

			Resource resource1 = StreamingTools.createResource();
			Resource resource2 = StreamingTools.createResource();

			request.setResource1(resource1);
			request.setResource2(resource2);

			capture1 = new ByteArrayOutputStream();
			capture2 = new ByteArrayOutputStream();

			request.setCapture1(RpcTestBase.callStreamCapture(capture1));
			request.setCapture2(RpcTestBase.callStreamCapture(capture2));

		}

	}

	protected void assertStreamingTestServiceProcessorResponse(StreamingTestServiceProcessorResponse response, ByteArrayOutputStream capture1,
			ByteArrayOutputStream capture2) throws Exception {

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getResponseDate());

		StreamingTools.checkOutput(capture1, response.getCapture1Md5());
		StreamingTools.checkOutput(capture2, response.getCapture2Md5());

		StreamingTools.checkResource(response.getResource1());
		StreamingTools.checkResource(response.getResource2());

	}

	protected class ResponseConsumer implements Consumer<Object> {

		boolean eager;
		ByteArrayOutputStream[] captures;

		public ResponseConsumer(boolean eager, ByteArrayOutputStream... captures) {
			this.eager = eager;
			this.captures = captures;
		}

		private Object eagerResponse;

		@Override
		public void accept(Object eagerResponse) {
			this.eagerResponse = eagerResponse;
			log.debug("Client consumed the eager response: " + eagerResponse);

			if (captures != null) {
				for (ByteArrayOutputStream capture : captures) {
					byte[] data = capture.toByteArray();
					if (eager) {
						Assert.assertTrue(
								"There are already " + data.length + " bytes writen to the capture by the time the response was eagerly notified.",
								data.length == 0);
						log.debug("No data yet written to capture stream " + Integer.toHexString(capture.hashCode())
								+ " when response was eagerly consumed.");
					} else {
						Assert.assertFalse("No bytes writen to the expected capture for a request which didn't eagerly notify the response.",
								data.length == 0);
						log.debug("There are " + data.length + " bytes writen stream " + Integer.toHexString(capture.hashCode())
								+ " when response was received by the registered consumer.");
					}
				}
			}

		}

		public <T> T getEagerResponse() {
			return (T) eagerResponse;
		}

	}

	private static CallStreamCapture callStreamCapture(ByteArrayOutputStream out1) {
		CallStreamCapture result = CallStreamCapture.T.create();
		result.setGlobalId(UUID.randomUUID().toString());
		result.setOutputStreamProvider(StreamProviders.from(out1));

		return result;
	}

}
