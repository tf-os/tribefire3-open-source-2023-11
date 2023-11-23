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
package tribefire.platform.impl.multicast;

import static com.braintribe.wire.api.util.Sets.set;
import static java.util.Collections.emptySet;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.ADDRESSEE_B;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.ALL_INSTANCES;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.DEFAULT_TIMEOUT;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.INSTANCE_A1;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.INSTANCE_B1;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.INSTANCE_B2;
import static tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract.REQUEST_TIMEOUT;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.StandardRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.testing.category.Slow;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.platform.impl.multicast.wire.MulticastTestWireModule;
import tribefire.platform.impl.multicast.wire.contract.MulticastProcessorContract;
import tribefire.platform.impl.topology.CartridgeLiveInstances;

/**
 * {@link MulticastProcessor} tests.
 * 
 */
public class MulticastProcessorTest {

	private WireContext<MulticastProcessorContract> context;

	@Before
	public void init() {
		context = Wire.context(MulticastTestWireModule.INSTANCE);
	}

	@After
	public void destroy() {
		context.shutdown();
	}

	@Test
	public void testSynchronousSingleResponse() {

		// No addressee is given, one response is returned

		liveInstances(INSTANCE_A1);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousSingleAddresseeResponse() {

		// One addressee is given, one response is returned

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest(INSTANCE_A1);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousMultipleResponses() {

		// No addressee is given, multiple responses are returned

		liveInstances(ALL_INSTANCES);

		respondAs(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, ALL_INSTANCES);

	}

	@Test
	public void testSynchronousMultipleAddresseesResponses() {

		// One node or app adressee is given, multiple responses are returned

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_B1, INSTANCE_B2);

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_B1, INSTANCE_B2);

	}

	@Test
	public void testSynchronousSingleDelayedResponse() {

		// No addressee is given, one delayed response is returned

		liveInstances(INSTANCE_A1);

		respondDelayedAs(DEFAULT_TIMEOUT / 2, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousSingleAddresseeDelayedResponse() {

		// One addressee is given, one delayed response is returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 2, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(INSTANCE_A1);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousMultipleDelayedResponses() {

		// No addressee is given, multiple delayed responses are returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 2, ALL_INSTANCES); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, ALL_INSTANCES);

	}

	@Test
	public void testSynchronousMultipleAddresseesDelayedResponses() {

		// One node or app adressee is given, multiple delayed responses are returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 2, INSTANCE_B1, INSTANCE_B2); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_B1, INSTANCE_B2);

	}

	@Test
	@Category(Slow.class)
	public void testSynchronousSingleDelayedWithKeepAliveResponse() {

		// No addressee is given, one delayed response is returned after a keep alive signal

		liveInstances(INSTANCE_A1);

		respondDelayedAs(DEFAULT_TIMEOUT / 3 * 2, true, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	@Category(Slow.class)
	public void testSynchronousSingleAddresseeDelayedWithKeepAliveResponse() {

		// One addressee is given, one delayed response is returned after a keep alive signal

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 3 * 2, true, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(INSTANCE_A1);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	@Category(Slow.class)
	public void testSynchronousMultipleDelayedWithKeepAliveResponses() {

		// No addressee is given, multiple delayed responses are returned after keep alive signals

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 3 * 2, true, ALL_INSTANCES); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, ALL_INSTANCES);

	}

	@Test
	public void testSynchronousMultipleAddresseesDelayedWithKeepAliveResponses() {

		// One node or app adressee is given, multiple delayed responses are returned after keep alive signals

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 3 * 2, true, INSTANCE_B1, INSTANCE_B2); // Delay within the configured
																					// timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_B1, INSTANCE_B2);

	}

	@Test
	@Category(Slow.class)
	public void testSynchronousNoResponse() {

		// No addressee is given, no response is returned within the timeout limit

		liveInstances(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest();

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(ALL_INSTANCES));

	}

	@Test
	public void testSynchronousPartialNoResponse() {

		// No addressee is given, partial no response is returned within the timeout limit

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(DEFAULT_TIMEOUT / 2, INSTANCE_B1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, set(INSTANCE_B1), set(INSTANCE_B2));

	}

	@Test
	public void testAsynchronous() {

		// No addressee is given, async so no response is awaited for.

		liveInstances(ALL_INSTANCES);

		respondAs(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest(true);

		MulticastResponse response = evaluate(request);

		Assert.assertNull("Async MulticastRequest should have returned null MulticastResponse", response);

	}

	@Test
	public void testAddressedAsynchronous() {

		// Addressee is given, async so no response is awaited for.

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest(INSTANCE_A1, true, null);

		MulticastResponse response = evaluate(request);

		Assert.assertNull("Async MulticastRequest should have returned null MulticastResponse", response);

	}

	@Test
	public void testSynchronousSingleResponseWithTimeout() {

		// No addressee is given, one response is returned

		liveInstances(INSTANCE_A1);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousSingleAddresseeResponseWithTimeout() {

		// One addressee is given, one response is returned

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest(INSTANCE_A1, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousMultipleResponsesWithTimeout() {

		// No addressee is given, multiple responses are returned

		liveInstances(ALL_INSTANCES);

		respondAs(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, ALL_INSTANCES);

	}

	@Test
	public void testSynchronousMultipleAddresseesResponsesWithTimeout() {

		// One node or app adressee is given, multiple responses are returned

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_B1, INSTANCE_B2);

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_B1, INSTANCE_B2);

	}

	@Test
	public void testSynchronousSingleDelayedResponseWithTimeout() {

		// No addressee is given, one delayed response is returned

		liveInstances(INSTANCE_A1);

		respondDelayedAs(REQUEST_TIMEOUT / 2, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousSingleAddresseeDelayedResponseWithTimeout() {

		// One addressee is given, one delayed response is returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 2, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(INSTANCE_A1, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_A1);

	}

	@Test
	public void testSynchronousMultipleDelayedResponsesWithTimeout() {

		// No addressee is given, multiple delayed responses are returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 2, ALL_INSTANCES); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, ALL_INSTANCES);

	}

	@Test
	public void testSynchronousMultipleAddresseesDelayedResponsesWithTimeout() {

		// One node or app adressee is given, multiple delayed responses are returned

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 2, INSTANCE_B1, INSTANCE_B2); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, INSTANCE_B1, INSTANCE_B2);

	}

	@Test
	@Category(Slow.class)
	public void testSynchronousSingleDelayedWithKeepAliveResponseWithTimeout() {

		// No addressee is given, one delayed response is returned after a keep alive signal

		liveInstances(INSTANCE_A1);

		respondDelayedAs(REQUEST_TIMEOUT / 3 * 2, true, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(INSTANCE_A1));

	}

	@Test
	public void testSynchronousSingleAddresseeDelayedWithKeepAliveResponseWithTimeout() {

		// One addressee is given, one delayed response is returned after a keep alive signal

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 3 * 2, true, INSTANCE_A1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(INSTANCE_A1, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(INSTANCE_A1));

	}

	@Test
	public void testSynchronousMultipleDelayedWithKeepAliveResponsesWithTimeout() {

		// No addressee is given, multiple delayed responses are returned after keep alive signals

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 3 * 2, true, ALL_INSTANCES); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(ALL_INSTANCES));

	}

	@Test
	public void testSynchronousMultipleAddresseesDelayedWithKeepAliveResponsesWithTimeout() {

		// One node or app adressee is given, multiple delayed responses are returned after keep alive signals

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 3 * 2, true, INSTANCE_B1, INSTANCE_B2); // Delay within the configured
																					// timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(INSTANCE_B1, INSTANCE_B2));

	}

	@Test
	public void testSynchronousNoResponseWithTimeout() {

		// No addressee is given, no response is returned within the timeout limit

		liveInstances(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest(REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, emptySet(), set(ALL_INSTANCES));

	}

	@Test
	public void testSynchronousPartialNoResponseWithTimeout() {

		// No addressee is given, partial no response is returned within the timeout limit

		liveInstances(ALL_INSTANCES);

		respondDelayedAs(REQUEST_TIMEOUT / 2, INSTANCE_B1); // Delay within the configured timeout

		MulticastRequest request = createMulticastRequest(ADDRESSEE_B, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		assertResponse(request, response, set(INSTANCE_B1), set(INSTANCE_B2));

	}

	@Test
	public void testAsynchronousWithTimeout() {

		// No addressee is given, async so no response is awaited for.

		liveInstances(ALL_INSTANCES);

		respondAs(ALL_INSTANCES);

		MulticastRequest request = createMulticastRequest(null, true, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		Assert.assertNull("Async MulticastRequest should have returned null MulticastResponse", response);

	}

	@Test
	public void testAddressedAsynchronousWithTimeout() {

		// Addressee is given, async so no response is awaited for.

		liveInstances(ALL_INSTANCES);

		respondAs(INSTANCE_A1);

		MulticastRequest request = createMulticastRequest(INSTANCE_A1, true, REQUEST_TIMEOUT);

		MulticastResponse response = evaluate(request);

		Assert.assertNull("Async MulticastRequest should have returned null MulticastResponse", response);

	}

	private void assertResponse(MulticastRequest multicastRequest, MulticastResponse multicastResponse, InstanceId... ids) {
		assertResponse(multicastRequest, multicastResponse, set(ids), emptySet());
	}

	private void assertResponse(MulticastRequest multicastRequest, MulticastResponse multicastResponse, Set<InstanceId> successInstances,
			Set<InstanceId> failureInstances) {

		Assert.assertNotNull(multicastResponse);

		Set<String> successful = successInstances.stream().map(InstanceId::toString).collect(Collectors.toSet());
		Set<String> failed = failureInstances.stream().map(InstanceId::toString).collect(Collectors.toSet());

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();

		Assert.assertEquals("Unexpected number of responses", successInstances.size() + failureInstances.size(), responses.size());

		TestRequest payload = (TestRequest) multicastRequest.getServiceRequest();

		for (Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {

			String instanceId = entry.getKey().toString();
			ServiceResult result = entry.getValue();

			if (successful.remove(instanceId)) {
				ResponseEnvelope response = result.asResponse();
				Assert.assertNotNull("Unexpected ServiceResult for instance " + instanceId + ": " + result, response);
				TestRequest responsePayload = (TestRequest) response.getResult();
				Assert.assertEquals(payload.getGlobalId(), responsePayload.getGlobalId());
			} else if (failed.remove(instanceId)) {
				Failure failure = result.asFailure();
				Assert.assertNotNull("Unexpected ServiceResult for instance " + instanceId + ": " + result, failure);
			} else {
				Assert.fail("Instance id " + instanceId + " was not among expected succeful nor failed ids");
			}

		}

	}

	private MulticastResponse evaluate(MulticastRequest request) {
		return request.eval(context.contract().evaluator()).get();
	}

	private MulticastRequest createMulticastRequest() {
		return createMulticastRequest(null, null, null);
	}

	private MulticastRequest createMulticastRequest(InstanceId addressee) {
		return createMulticastRequest(addressee, null, null);
	}

	private MulticastRequest createMulticastRequest(Boolean async) {
		return createMulticastRequest(null, async, null);
	}

	private MulticastRequest createMulticastRequest(Long timeout) {
		return createMulticastRequest(null, null, timeout);
	}

	private MulticastRequest createMulticastRequest(InstanceId addressee, Long timeout) {
		return createMulticastRequest(addressee, null, timeout);
	}

	private MulticastRequest createMulticastRequest(InstanceId addressee, Boolean async, Long timeout) {

		TestRequest payload = TestRequest.T.create();
		payload.setGlobalId(UUID.randomUUID().toString());

		MulticastRequest request = MulticastRequest.T.create();

		if (addressee != null)
			request.setAddressee(addressee);

		if (async != null)
			request.setAsynchronous(async);

		if (timeout != null)
			request.setTimeout(timeout);

		request.setServiceRequest(payload);

		return request;

	}

	private void respondAs(InstanceId... ids) {
		for (InstanceId id : ids) {
			context.contract().consumer(id);
			// Just build the consumers, no further config needed.
		}
	}

	private void respondDelayedAs(long delay, InstanceId... ids) {
		respondDelayedAs(delay, false, ids);
	}

	private void respondDelayedAs(long delay, boolean keepAlive, InstanceId... ids) {
		for (InstanceId id : ids) {
			TestMulticastConsumer consumer = context.contract().consumer(id);
			consumer.setDelay(delay);
			consumer.setReplyAfterKeepAlive(keepAlive);
		}
	}

	private void liveInstances(InstanceId... ids) {

		CartridgeLiveInstances liveInstances = context.contract().liveInstances();

		for (InstanceId id : ids) {
			liveInstances.accept(id);
		}

	}

	public static interface TestRequest extends StandardRequest {

		EntityType<TestRequest> T = EntityTypes.T(TestRequest.class);

	}

}
