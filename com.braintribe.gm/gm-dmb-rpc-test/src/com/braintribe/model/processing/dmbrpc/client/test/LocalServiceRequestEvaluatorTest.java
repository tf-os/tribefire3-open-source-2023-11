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
package com.braintribe.model.processing.dmbrpc.client.test;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.RpcTestBase;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessorRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.common.eval.AbstractServiceRequestEvaluator;
import com.braintribe.model.service.api.CompositeRequest;

/**
 * <p>
 * Test suite using a {@link AbstractServiceRequestEvaluator} to execute
 * requests directly against a local {@link ServiceProcessor}.
 * 
 * <p>
 * The tests implementation as well as the implementation of the test services are packaged in the {@code GmRpcTestBase}
 * artifact.
 * 
 * <p>
 * Although not MBean-based, these tests can be used to verify the tests inherited from {@link RpcTestBase}
 * 
 */
@Ignore // Will move to ServiceApiCommons
public class LocalServiceRequestEvaluatorTest extends DmbRpcTestBase {

	// ============================= //
	// =========== TESTS =========== //
	// ============================= //

	// == BasicTestServiceProcessor == //

	@Test
	public void testStandardBasicServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, false);
	}

	@Test
	public void testStandardBasicServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, false);
	}

	// == StreamingTestServiceProcessor == //

	@Test
	public void testStandardStreamingServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false);
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false);
	}

	// == UploadTestServiceProcessor == //

	@Test
	public void testStandardUploadServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false);
	}

	@Test
	public void testStandardUploadServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false);
	}

	// == DownloadResourceTestServiceProcessor == //

	@Test
	public void testStandardDownloadResourceServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false);
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false);
	}

	// == DownloadCaptureTestServiceProcessor == //

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false);
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false);
	}


	// == CompositeTestServiceProcessor == //

	@Test
	public void testStandardCompositeServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(CompositeRequest.T, false);
	}

	@Test
	public void testStandardCompositeServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(CompositeRequest.T, false);
	}

	// ======================================== //
	// =========== SEQUENCIAL TESTS =========== //
	// ======================================== //

	// == BasicTestServiceProcessor == //

	@Test
	public void testStandardBasicServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardBasicServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// == StreamingTestServiceProcessor == //

	@Test
	public void testStandardStreamingServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// == UploadTestServiceProcessor == //

	@Test
	public void testStandardUploadServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardUploadServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// == DownloadResourceTestServiceProcessor == //

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// == DownloadCaptureTestServiceProcessor == //

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// == CompositeTestServiceProcessor == //

	@Test
	public void testStandardCompositeServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(CompositeRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardCompositeServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(CompositeRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	// ======================================== //
	// =========== CONCURRENT TESTS =========== //
	// ======================================== //

	// == BasicTestServiceProcessor == //

	@Test
	public void testStandardBasicServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardBasicServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// == StreamingTestServiceProcessor == //

	@Test
	public void testStandardStreamingServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// == UploadTestServiceProcessor == //

	@Test
	public void testStandardUploadServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardUploadServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingUploadServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// == DownloadResourceTestServiceProcessor == //

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// == DownloadCaptureTestServiceProcessor == //

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// == CompositeTestServiceProcessor == //

	@Test
	public void testStandardCompositeServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(CompositeRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(CompositeRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingCompositeServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(CompositeRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(CompositeRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	@Override
	public AbstractServiceRequestEvaluator createService(GmRpcClientConfig clientConfig) {
		// Will move to ServiceApiCommons
//		AbstractServiceRequestEvaluator evaluator = context.beans().serverCommons().serviceRequestEvaluatorSelfAuthenticating();
		return null;
	}

	@Override
	public void destroyService(Object service) {
		// no-op
	}

}
