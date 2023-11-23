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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.GenericProcessingRequest;
import com.braintribe.model.service.api.HasServiceRequest;
import com.braintribe.model.service.api.HasServiceRequests;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Tests the {@link ServiceRequest} evaluation via {@link BasicPersistenceGmSession}.
 */
public class BasicPersistenceGmSessionEvaluationTest {

	private static String sessionAccessId = "test-access-id";
	private static String explicitAccessId = "custom-access-id";

	Smood smood;
	BasicPersistenceGmSession session;

	@Before
	public void setup() throws Exception {
		smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(MetaModelTools.provideRawModel(Person.T, Flag.T));
		smood.setAccessId(sessionAccessId);

		session = new BasicPersistenceGmSession(smood);
		session.setAccessId(sessionAccessId);
		session.setRequestEvaluator(new TestEvaluator());

		preparePersons();
	}

	@Test
	public void testNonAccessRequest() throws Exception {
		PersonRequest request = PersonRequest.T.create();
		request.eval(session).get();
		assertThat(request.getDomainId()).isNull();

		request.setDomainId(explicitAccessId);
		request.eval(session).get();
		assertThat(request.getDomainId()).isEqualTo(explicitAccessId);
	}

	@Test
	public void testAccessRequest() throws Exception {
		PersonAccessRequest request = PersonAccessRequest.T.create();
		request.eval(session).get();
		assertThat(request.getDomainId()).isEqualTo(sessionAccessId);

		request.setDomainId(explicitAccessId);
		request.eval(session).get();
		assertThat(request.getDomainId()).isEqualTo(explicitAccessId);
	}

	@Test
	public void testAccessDataRequest() throws Exception {
		PersonAccessDataRequest request = PersonAccessDataRequest.T.create();
		request.eval(session).get();
		assertThat(request.getDomainId()).isEqualTo(sessionAccessId);

		request.setDomainId(explicitAccessId);
		request.eval(session).get();
		assertThat(request.getDomainId()).isEqualTo(explicitAccessId);
	}

	/* Generic processing cases */

	@Test
	public void testGenericNested() throws Exception {
		GenericWrapper wrapper = GenericWrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		wrapper.setServiceRequest(request);

		wrapper.eval(session).get();

		assertThat(request.getDomainId()).isEqualTo(sessionAccessId);
	}

	@Test
	public void testGenericMultiNested() throws Exception {
		GenericWrapper gpr = GenericWrapper.T.create();
		GenericWrapper gpr1 = GenericWrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		gpr1.setServiceRequest(request);
		gpr.setServiceRequest(gpr1);
		gpr.eval(session).get();

		assertThat(request.getDomainId()).isEqualTo(sessionAccessId);
	}

	@Test
	public void testGenericComposite() throws Exception {
		GenericComposite gpr = GenericComposite.T.create();
		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();

		gpr.getRequests().add(request1);
		gpr.getRequests().add(request2);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isEqualTo(sessionAccessId);
		assertThat(request2.getDomainId()).isEqualTo(sessionAccessId);
	}

	@Test
	public void testGenericCompositeNested() throws Exception {
		GenericComposite gpr = GenericComposite.T.create();
		GenericWrapper gpr1 = GenericWrapper.T.create();
		GenericWrapper gpr2 = GenericWrapper.T.create();
		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();

		gpr1.setServiceRequest(request1);
		gpr2.setServiceRequest(request2);

		gpr.getRequests().add(gpr1);
		gpr.getRequests().add(gpr2);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isEqualTo(sessionAccessId);
		assertThat(request2.getDomainId()).isEqualTo(sessionAccessId);
	}

	@Test
	public void testGenericNestedComposite() throws Exception {
		GenericWrapper gpr = GenericWrapper.T.create();

		GenericComposite gpr1 = GenericComposite.T.create();
		GenericComposite gpr2 = GenericComposite.T.create();

		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();
		PersonAccessRequest request3 = PersonAccessRequest.T.create();

		gpr1.getRequests().add(request1);
		gpr1.getRequests().add(gpr2);
		gpr2.getRequests().add(request2);
		gpr2.getRequests().add(request3);

		gpr.setServiceRequest(gpr1);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isEqualTo(sessionAccessId);
		assertThat(request2.getDomainId()).isEqualTo(sessionAccessId);
		assertThat(request3.getDomainId()).isEqualTo(sessionAccessId);
	}

	/* Nesting cases, involving non-GPRs */

	@Test
	public void testNested() throws Exception {
		Wrapper wrapper = Wrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		wrapper.setServiceRequest(request);

		wrapper.eval(session).get();

		assertThat(request.getDomainId()).isNull();
	}

	@Test
	public void testMultiNested() throws Exception {
		Wrapper wrapper = Wrapper.T.create();
		Wrapper gpr1 = Wrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		gpr1.setServiceRequest(request);
		wrapper.setServiceRequest(gpr1);
		wrapper.eval(session).get();

		assertThat(request.getDomainId()).isNull();
	}

	@Test
	public void testMultiNestedWithGenericLevel() throws Exception {
		Wrapper wrapper = Wrapper.T.create();
		GenericWrapper gpr1 = GenericWrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		gpr1.setServiceRequest(request);
		wrapper.setServiceRequest(gpr1);
		wrapper.eval(session).get();

		assertThat(request.getDomainId()).isNull();
	}

	@Test
	public void testMultiNestedWithGenericTopLevel() throws Exception {
		GenericWrapper wrapper = GenericWrapper.T.create();
		Wrapper nestedWrapper = Wrapper.T.create();
		PersonAccessRequest request = PersonAccessRequest.T.create();

		nestedWrapper.setServiceRequest(request);
		wrapper.setServiceRequest(nestedWrapper);
		wrapper.eval(session).get();

		assertThat(request.getDomainId()).isNull();
	}

	@Test
	public void testComposite() throws Exception {
		Composite gpr = Composite.T.create();
		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();

		gpr.getRequests().add(request1);
		gpr.getRequests().add(request2);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isNull();
		assertThat(request2.getDomainId()).isNull();
	}

	@Test
	public void testCompositeNested() throws Exception {
		Composite gpr = Composite.T.create();
		GenericWrapper gpr1 = GenericWrapper.T.create();
		GenericWrapper gpr2 = GenericWrapper.T.create();
		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();

		gpr1.setServiceRequest(request1);
		gpr2.setServiceRequest(request2);

		gpr.getRequests().add(gpr1);
		gpr.getRequests().add(gpr2);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isNull();
		assertThat(request2.getDomainId()).isNull();
	}

	@Test
	public void tesNestedComposite() throws Exception {
		Wrapper gpr = Wrapper.T.create();

		GenericComposite gpr1 = GenericComposite.T.create();
		GenericComposite gpr2 = GenericComposite.T.create();

		PersonAccessRequest request1 = PersonAccessRequest.T.create();
		PersonAccessRequest request2 = PersonAccessRequest.T.create();
		PersonAccessRequest request3 = PersonAccessRequest.T.create();

		gpr1.getRequests().add(request1);
		gpr1.getRequests().add(gpr2);
		gpr2.getRequests().add(request2);
		gpr2.getRequests().add(request3);

		gpr.setServiceRequest(gpr1);

		gpr.eval(session).get();

		assertThat(request1.getDomainId()).isNull();
		assertThat(request2.getDomainId()).isNull();
		assertThat(request3.getDomainId()).isNull();
	}

	// including non-generic

	private Person getP1() throws Exception {
		return session.query().entity(Person.T, 1L).require();
	}

	private void preparePersons() throws GmSessionException {
		Person p1 = newPerson("p1");
		Person p2 = newPerson("p2");
		Person p3 = newPerson("p3");
		p1.setFriendSet(asSet(p2, p3));
		session.commit();
	}

	private Person newPerson(String name) {
		Person p = session.create(Person.T);
		p.setName(name);
		return p;
	}

	/* Test evaluator */

	public class TestEvaluator implements Evaluator<ServiceRequest> {

		@Override
		public <T> EvalContext<T> eval(ServiceRequest evaluable) {
			return new EvalContext<T>() {

				@Override
				public T get() throws EvalException {
					try {
						Person result = getP1();
						result.detach();
						return (T) result;

					} catch (RuntimeException | Error e) {
						throw e;
					} catch (Exception e) {
						throw new EvalException(e);
					}
				}

				@Override
				public void get(AsyncCallback<? super T> callback) {
					callback.onSuccess(get());
				}

				@Override
				public <U, A extends EvalContextAspect<? super U>> EvalContext<T> with(Class<A> aspect, U value) {
					throw new UnsupportedOperationException("Not yet relevant for tests");
				}

			};
		}

	}

	/* Test requests */

	public static interface PersonRequest extends DomainRequest {
		EntityType<PersonRequest> T = EntityTypes.T(PersonRequest.class);

		Person getPerson();
		void setPerson(Person person);
	}

	public static interface PersonAccessRequest extends PersonRequest, AccessRequest {
		EntityType<PersonAccessRequest> T = EntityTypes.T(PersonAccessRequest.class);
	}

	public static interface PersonAccessDataRequest extends PersonRequest, AccessDataRequest {
		EntityType<PersonAccessDataRequest> T = EntityTypes.T(PersonAccessDataRequest.class);
	}

	public static interface GenericWrapper extends HasServiceRequest, GenericProcessingRequest {
		EntityType<GenericWrapper> T = EntityTypes.T(GenericWrapper.class);
	}

	public static interface GenericComposite extends HasServiceRequests, GenericProcessingRequest {
		EntityType<GenericComposite> T = EntityTypes.T(GenericComposite.class);
	}

	public static interface Wrapper extends HasServiceRequest, ServiceRequest {
		EntityType<Wrapper> T = EntityTypes.T(Wrapper.class);
	}

	public static interface Composite extends HasServiceRequests, ServiceRequest {
		EntityType<Composite> T = EntityTypes.T(Composite.class);
	}

}
