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
package com.braintribe.model.processing.webrpc.client.test;

import org.junit.Test;

import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessorRequest;
import com.braintribe.model.processing.webrpc.client.BasicGmWebRpcClientConfig;
import com.braintribe.model.processing.webrpc.client.GmWebRpcEvaluator;

/**
 * <p>
 * Test suite using the DDSA evaluator {@link com.braintribe.model.processing.webrpc.client.GmWebRpcEvaluator} to
 * execute requests against the DDSA server {@link com.braintribe.model.processing.webrpc.server.GmWebRpcServer}.
 * 
 * <p>
 * Evaluators in this suite are always configured with a response consumer, which may be eagerly called by the target
 * processor or not.
 * 
 * <p>
 * The tests implementation as well as the implementation of the test services are packaged in the {@code GmRpcTestBase}
 * artifact.
 * 
 */
public class EvaluatorClientWithResponseConsumerTest extends WebRpcTestBase {

	// ============================= //
	// =========== TESTS =========== //
	// ============================= //

	// == BasicTestServiceProcessor == //

	// @formatter:off
	@Test
	public void testStandardBasicServiceProcessorRequest() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.test();
	}

	@Test
	public void testStandardBasicServiceProcessorRequestAsync() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequest() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestAsync() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testStandardBasicServiceProcessorRequestWithConsumer() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.test();
	}

	@Test
	public void testStandardBasicServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestWithConsumer() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(BasicTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.forceReAuthorization()
			.test();
	}

	// @formatter:on


	// == StreamingTestServiceProcessor == //

	// @formatter:off
	@Test
	public void testStandardStreamingServiceProcessorRequest() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.test();
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestAsync() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequest() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequestAsync() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestWithConsumer() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.test();
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequestWithConsumer() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(StreamingTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.forceReAuthorization()
			.test();
	}

	// @formatter:on


	// == UploadTestServiceProcessor == //

	// @formatter:off
	@Test
	public void testStandardUploadServiceProcessorRequest() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.test();
	}

	@Test
	public void testStandardUploadServiceProcessorRequestAsync() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequest() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequestAsync() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testStandardUploadServiceProcessorRequestWithConsumer() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.test();
	}

	@Test
	public void testStandardUploadServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequestWithConsumer() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(UploadTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.forceReAuthorization()
			.test();
	}

	// @formatter:on


	// == DownloadResourceTestServiceProcessor == //

	// @formatter:off
	@Test
	public void testStandardDownloadResourceServiceProcessorRequest() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.test();
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestAsync() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequest() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequestAsync() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestWithConsumer() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.test();
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequestWithConsumer() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(DownloadResourceTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.forceReAuthorization()
			.test();
	}

	// @formatter:on


	// == DownloadCaptureTestServiceProcessor == //

	// @formatter:off
	@Test
	public void testStandardDownloadCaptureServiceProcessorRequest() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.test();
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestAsync() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequest() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequestAsync() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.notifyEagerResponse()
			.async()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestWithConsumer() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.test();
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequestWithConsumer() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.forceReAuthorization()
			.test();
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequestWithConsumerAsync() throws Exception {
		with(DownloadCaptureTestServiceProcessorRequest.T)
			.addResponseConsumer()
			.async()
			.forceReAuthorization()
			.test();
	}

	// @formatter:on


	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	@Override
	public GmWebRpcEvaluator createService(GmRpcClientConfig clientConfig) {
		BasicGmWebRpcClientConfig config = (BasicGmWebRpcClientConfig)clientConfig;
		config.setUrl(rpcUrl.toString());
		GmWebRpcEvaluator evaluator = new GmWebRpcEvaluator();
		evaluator.setConfig(config);
		return evaluator;
	}

	@Override
	public <S> void destroyService(S service) {
		// no-op
	}

}
