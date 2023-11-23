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
package com.braintribe.model.access.impl.etcd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.integration.etcd.supplier.ClientSupplier;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.Holder;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

@Category(SpecialEnvironment.class)
public class EtcdAccessTest {

	protected EtcdAccess accessSessions;
	protected BasicPersistenceGmSession sessionSessions;

	protected static List<String> endpointUrls = new ArrayList<>();
	protected static String username = null;
	protected static String password = null;

	@BeforeClass
	public static void beforeClass() {
		endpointUrls.add("http://localhost:2379");
	}

	@Test
	public void testLargeQuantities() throws Exception {

		EtcdProcessing processing = new EtcdProcessing(new ClientSupplier(endpointUrls, username, password));

		int iterations = 1000;
		int valueSize = 10000;
		int ttl = 60;

		String keyPrefix = UUID.randomUUID().toString();
		byte[] value = new byte[valueSize];
		new Random().nextBytes(value);

		Instant insertStart = NanoClock.INSTANCE.instant();
		for (int i = 0; i < iterations; ++i) {
			processing.put(keyPrefix + "/" + i, value, ttl);
		}
		System.out.println("Inserting " + iterations + " elements (size " + valueSize + ") with prefix " + keyPrefix + " took "
				+ StringTools.prettyPrintDuration(insertStart, true, null));

		Instant getStart = NanoClock.INSTANCE.instant();
		List<String> keys = processing.getAllKeysWithPrefix(keyPrefix);
		Map<String, String> map = processing.getAllEntries(keys);
		System.out.println("Getting took " + StringTools.prettyPrintDuration(getStart, true, null));

		Collection<String> values = map.values();
		assertThat(values.size()).isEqualTo(iterations);
	}

	@Test
	public void testSingleSession() throws Exception {

		accessSessions = createAccess("sessions-test", "com.braintribe.gm", "user-session-model");
		sessionSessions = new BasicPersistenceGmSession(accessSessions);

		String sessionId = UUID.randomUUID().toString();

		EntityQuery query = EntityQueryBuilder.from(UserSession.T).where().property(UserSession.sessionId).eq(sessionId).done();
		List<UserSession> list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isEmpty();

		UserSession us = sessionSessions.create(UserSession.T);
		us.setCreationDate(new Date());
		us.setSessionId(sessionId);

		User user = sessionSessions.create(User.T);
		user.setName("john");
		us.setUser(user);

		sessionSessions.commit();

		list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isNotEmpty();
		assertThat(list.size()).isEqualTo(1);

		UserSession controlSession = list.get(0);
		assertThat(controlSession.getSessionId()).isEqualTo(sessionId);
		assertThat(controlSession.getUser()).isNotNull();
		assertThat(controlSession.getUser().getName()).isNotNull();

		assertThat(controlSession.getUser().getName()).isEqualTo("john");
	}

	@Test
	public void testMultipleSessions() throws Exception {

		accessSessions = createAccess("sessions-test", "com.braintribe.gm", "user-session-model");
		sessionSessions = new BasicPersistenceGmSession(accessSessions);

		int count = 10;
		String sessionId = UUID.randomUUID().toString();

		for (int i = 0; i < count; ++i) {
			UserSession us = sessionSessions.create(UserSession.T);
			us.setCreationDate(new Date());
			us.setSessionId(sessionId + "-" + i);

			User user = sessionSessions.create(User.T);
			user.setName("john" + i);
			us.setUser(user);

		}
		sessionSessions.commit();

		EntityQuery query = EntityQueryBuilder.from(UserSession.T).where().property(UserSession.sessionId).like(sessionId + "*").done();
		List<UserSession> list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isNotEmpty();
		assertThat(list.size()).isEqualTo(count);

		UserSession controlSession = list.get(0);
		assertThat(controlSession.getSessionId()).startsWith(sessionId);
		assertThat(controlSession.getUser()).isNotNull();
		assertThat(controlSession.getUser().getName()).isNotNull();

		assertThat(controlSession.getUser().getName()).startsWith("john");
	}

	@Test
	public void testMultipleSessionsSingleUser() throws Exception {

		accessSessions = createAccess("sessions-test", "com.braintribe.gm", "user-session-model");
		sessionSessions = new BasicPersistenceGmSession(accessSessions);

		int count = 10;
		String sessionId = UUID.randomUUID().toString();

		User user = sessionSessions.create(User.T);
		user.setName("john");

		for (int i = 0; i < count; ++i) {
			UserSession us = sessionSessions.create(UserSession.T);
			us.setCreationDate(new Date());
			us.setSessionId(sessionId + "-" + i);
			us.setUser(user);

		}
		sessionSessions.commit();

		EntityQuery query = EntityQueryBuilder.from(UserSession.T).where().property(UserSession.sessionId).like(sessionId + "*").done();
		List<UserSession> list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isNotEmpty();
		assertThat(list.size()).isEqualTo(count);

		UserSession controlSession = list.get(0);
		assertThat(controlSession.getSessionId()).startsWith(sessionId);
		assertThat(controlSession.getUser()).isNotNull();
		assertThat(controlSession.getUser().getName()).isNotNull();

	}

	@Test
	public void testSingleSessionUpdate() throws Exception {

		accessSessions = createAccess("sessions-test", "com.braintribe.gm", "user-session-model");
		sessionSessions = new BasicPersistenceGmSession(accessSessions);

		String sessionId = UUID.randomUUID().toString();
		Date initialCreationDate = new Date();
		String initialCreationDateAsString = DateTools.encode(initialCreationDate, DateTools.ISO8601_DATE_WITH_MS_FORMAT);

		EntityQuery query = EntityQueryBuilder.from(UserSession.T).where().property(UserSession.sessionId).eq(sessionId).done();
		List<UserSession> list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isEmpty();

		UserSession us = sessionSessions.create(UserSession.T);
		us.setCreationDate(initialCreationDate);
		us.setLastAccessedDate(initialCreationDate);
		us.setSessionId(sessionId);

		User user = sessionSessions.create(User.T);
		user.setName("john");
		us.setUser(user);

		sessionSessions.commit();

		list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isNotEmpty();
		assertThat(list.size()).isEqualTo(1);

		UserSession controlSession = list.get(0);
		assertThat(controlSession.getSessionId()).isEqualTo(sessionId);
		assertThat(controlSession.getUser()).isNotNull();
		assertThat(controlSession.getUser().getName()).isNotNull();

		String checkDate = DateTools.encode(controlSession.getLastAccessedDate(), DateTools.ISO8601_DATE_WITH_MS_FORMAT);

		assertThat(checkDate).isEqualTo(initialCreationDateAsString);

		Thread.sleep(2_000L);

		Date updateDate = new Date();
		String updateCreationDateAsString = DateTools.encode(updateDate, DateTools.ISO8601_DATE_WITH_MS_FORMAT);

		controlSession.setLastAccessedDate(updateDate);

		sessionSessions.commit();

		list = sessionSessions.query().entities(query).list();

		assertThat(list).isNotNull();
		assertThat(list).isNotEmpty();
		assertThat(list.size()).isEqualTo(1);

		controlSession = list.get(0);

		checkDate = DateTools.encode(controlSession.getLastAccessedDate(), DateTools.ISO8601_DATE_WITH_MS_FORMAT);

		assertThat(checkDate).isEqualTo(updateCreationDateAsString);
	}

	@Ignore
	protected static EtcdAccess createAccess(String index, String modelPackage, String modelArtifact) throws Exception {

		try {
			Model model = GMF.getTypeReflection().getModel(modelPackage + ":" + modelArtifact);
			GmMetaModel metaModel = model.getMetaModel();

			BasicModelAccessory bma = new BasicModelAccessory() {
				@Override
				protected GmMetaModel loadModel() {
					return metaModel;
				}
				@Override
				protected boolean adoptLoadedModel() {
					return false;
				}
			};
			bma.setCortexSessionProvider(() -> null);
			bma.setModelName(metaModel.getName());
			bma.setSessionProvider(() -> bma);

			EtcdAccess access = new EtcdAccess();
			access.setAccessId("etcd." + index);
			access.setModelName(modelArtifact);

			BasicPersistenceGmSession session = new BasicPersistenceGmSession(access);
			session.setModelAccessory(bma);

			Holder<PersistenceGmSession> sessionHolder = new Holder<PersistenceGmSession>();
			sessionHolder.accept(session);
			access.setSessionProvider(sessionHolder);
			access.setClientSupplier(new ClientSupplier(endpointUrls, null, null));
			access.setMarshaller(new JsonStreamMarshaller());

			access.setMetaModelProvider(() -> metaModel);
			access.setProject(index);

			return access;
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
}
