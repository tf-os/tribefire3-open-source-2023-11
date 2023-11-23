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
package com.braintribe.model.processing.mqrpc.client.test;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.mqrpc.client.BasicGmMqRpcClientConfig;
import com.braintribe.model.processing.mqrpc.client.GmMqRpcEvaluator;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorRequest;

/**
 * <p>
 * Test suite using the DDSA evaluator {@link com.braintribe.model.processing.mqrpc.client.GmMqRpcEvaluator} to execute
 * requests against the DDSA server {@link com.braintribe.model.processing.mqrpc.server.GmMqRpcServer}.
 * 
 * <p>
 * The tests implementation as well as the implementation of the test services are packaged in the {@code GmRpcTestBase}
 * artifact.
 * 
 */
public class EvaluatorClientTest extends MqRpcTestBase {

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

	// @Test
	// public void testEncryptedBasicServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedBasicServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	@Test
	public void testReAuthorizingBasicServiceProcessorRequest() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, true);
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, true);
	}
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// // == StreamingTestServiceProcessor == //
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// // == UploadTestServiceProcessor == //
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// // == DownloadResourceTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// // == DownloadCaptureTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestNotifyingCache() throws Exception {
	// with(DownloadCaptureTestServiceProcessorRequest.T)
	// .notifyEagerResponse()
	// .test();
	// }
	//
	// // == CompositeTestServiceProcessor == //
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, false);
	// }
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, false);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, false);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequest() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, true);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequestAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, true);
	// }
	//
	// // ======================================== //
	// // =========== SEQUENCIAL TESTS =========== //
	// // ======================================== //
	//
	// // == BasicTestServiceProcessor == //
	//
	@Test
	public void testStandardBasicServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}

	@Test
	public void testStandardBasicServiceProcessorRequestSequentialAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	}
	//
	// @Test
	// public void testEncryptedBasicServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedBasicServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestSequential() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	}
	//
	// @Test
	// public void testReAuthorizingBasicServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // == StreamingTestServiceProcessor == //
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, false, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // == UploadTestServiceProcessor == //
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // == DownloadResourceTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, false, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, false, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, true, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, true, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // == DownloadCaptureTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true, false,
	// MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // == CompositeTestServiceProcessor == //
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, false, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequestSequential() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequestSequentialAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, true, false, MAX_SEQUENTIAL_TESTS);
	// }
	//
	// // ======================================== //
	// // =========== CONCURRENT TESTS =========== //
	// // ======================================== //
	//
	// // == BasicTestServiceProcessor == //
	//
	@Test
	public void testStandardBasicServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testStandardBasicServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	}
	//
	// @Test
	// public void testEncryptedBasicServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedBasicServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	@Test
	public void testReAuthorizingBasicServiceProcessorRequestConcurrent() throws Exception {
		testServiceProcessorRequest(BasicTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}

	@Test
	public void testReAuthorizingBasicServiceProcessorRequestConcurrentAsync() throws Exception {
		testServiceProcessorRequestAsync(BasicTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	}
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(BasicTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedBasicServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(BasicTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// // == StreamingTestServiceProcessor == //
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testStandardStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(StreamingTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedStreamingServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(StreamingTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// // == UploadTestServiceProcessor == //
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testStandardUploadServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedUploadServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingUploadServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(UploadTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedUploadServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(UploadTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// // == DownloadResourceTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testStandardDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, false, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, false, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadResourceTestServiceProcessorRequestEncrypted.T, true, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadResourceServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadResourceTestServiceProcessorRequestEncrypted.T, true, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// // == DownloadCaptureTestServiceProcessor == //
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testStandardDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, false, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedDownloadCaptureServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(DownloadCaptureTestServiceProcessorRequestEncrypted.T, true, true,
	// MAX_CONCURRENT_TESTS);
	// }
	//
	// // == CompositeTestServiceProcessor == //
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testStandardCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testEncryptedCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, false, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(CompositeRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequest.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequestConcurrent() throws Exception {
	// testServiceProcessorRequest(CompositeRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }
	//
	// @Test
	// public void testReAuthorizingEncryptedCompositeServiceProcessorRequestConcurrentAsync() throws Exception {
	// testServiceProcessorRequestAsync(CompositeRequestEncrypted.T, true, true, MAX_CONCURRENT_TESTS);
	// }

	// ============================================ //
	// =========== ERROR HANDLING TESTS =========== //
	// ============================================ //

	public void testAuthorizationFailure() {
		testUnauthorizedEvaluatorRequest();
	}

	@Test(expected = InterruptedException.class)
	public void testException() throws Throwable {
		try {
			testFailedEvaluatorRequest(InterruptedException.class);
		} catch (RuntimeException e) {
			e.printStackTrace();
			Assert.assertNotNull(e.getClass().getName() + " has no cause", e.getCause());
			boolean unexpectedCause = (e.getCause() instanceof RuntimeException || e.getCause() instanceof Error);
			Assert.assertFalse(e.getClass().getName() + " is wrapping an unchecked exception: " + e.getCause(), unexpectedCause);
			throw e.getCause();
		} catch (Exception e) {
			Assert.fail("Unexpected exception: " + e);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRuntimeException() throws Exception {
		testFailedEvaluatorRequest(IllegalArgumentException.class);
	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	@Override
	public GmMqRpcEvaluator createService(GmRpcClientConfig clientConfig) {
		BasicGmMqRpcClientConfig config = (BasicGmMqRpcClientConfig) clientConfig;
		GmMqRpcEvaluator evaluator = new GmMqRpcEvaluator();
		evaluator.setConfig(config);
		return evaluator;
	}

	@Override
	public <S> void destroyService(S service) {
		if (service instanceof GmMqRpcEvaluator) {
			((GmMqRpcEvaluator) service).close();
		}
	}

}
