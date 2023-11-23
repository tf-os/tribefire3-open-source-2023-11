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
package com.braintribe.model.access.smood.distributed.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.zip.CompressingMarshallerDecorator;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.distributed.DistributedSmoodAccess;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.user.Identity;
import com.braintribe.model.user.User;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class SmoodDbAccessTests extends TestBase {

	@Test
	public void queryTestWithInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session, new String[][] { {"Stefan", "Prieler"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void queryTestWithoutInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithoutInitialData());
			testUtilities.checkUserList(session, new String[][] {});

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// Was failing when I extracted it. Will fix only if this impl is needed
	@Test
	@Category(KnownIssue.class)
	public void FAILS_queryPolymorphicQueryWithInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			int preExistentUsers = 1;
			int usersToCreate = 2;

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());

			List<User> userResults = session.query().entities(EntityQueryBuilder.from(User.class).done()).list();

			Assert.assertEquals(preExistentUsers, userResults.size());

			List<Identity> identityResults = session.query().entities(EntityQueryBuilder.from(Identity.class).done()).list();

			Assert.assertEquals(preExistentUsers, identityResults.size());

			userResults = session.query().select(new SelectQueryBuilder().from(User.class, "u").done()).list();

			Assert.assertEquals(preExistentUsers, userResults.size());

			identityResults = session.query().select(new SelectQueryBuilder().from(Identity.class, "i").done()).list();

			Assert.assertEquals(preExistentUsers, identityResults.size());

			for (int i = 1; i <= usersToCreate; i++) {

				User testUser = session.create(User.T);
				testUser.setId("test"+i+".id");
				testUser.setName("test"+i+".name");
				testUser.setPassword("test"+i+".password");

				session.commit();

				userResults = session.query().entities(EntityQueryBuilder.from(User.class).done()).list();

				Assert.assertEquals(i+preExistentUsers, userResults.size());

				identityResults = session.query().entities(EntityQueryBuilder.from(Identity.class).done()).list();

				Assert.assertEquals(i+preExistentUsers, identityResults.size());

				userResults = session.query().select(new SelectQueryBuilder().from(User.class, "u").done()).list();

				Assert.assertEquals(i+preExistentUsers, userResults.size());

				identityResults = session.query().select(new SelectQueryBuilder().from(Identity.class, "i").done()).list();

				Assert.assertEquals(i+preExistentUsers, identityResults.size());

			}

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// Was failing when I extracted it. Will fix only if this impl is needed
	@Test
	@Category(KnownIssue.class)
	public void FAILS_queryPolymorphicQueryWithoutInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			int preExistentUsers = 0;
			int usersToCreate = 2;

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithoutInitialData());

			List<User> userResults = session.query().entities(EntityQueryBuilder.from(User.class).done()).list();

			Assert.assertEquals(preExistentUsers, userResults.size());

			List<Identity> identityResults = session.query().entities(EntityQueryBuilder.from(Identity.class).done()).list();

			Assert.assertEquals(preExistentUsers, identityResults.size());

			userResults = session.query().select(new SelectQueryBuilder().from(User.class, "u").done()).list();

			Assert.assertEquals(preExistentUsers, userResults.size());

			identityResults = session.query().select(new SelectQueryBuilder().from(Identity.class, "i").done()).list();

			Assert.assertEquals(preExistentUsers, identityResults.size());

			for (int i = 1; i <= usersToCreate; i++) {

				User testUser = session.create(User.T);
				testUser.setId("test"+i+".id");
				testUser.setName("test"+i+".name");
				testUser.setPassword("test"+i+".password");

				session.commit();

				userResults = session.query().entities(EntityQueryBuilder.from(User.class).done()).list();

				Assert.assertEquals(i+preExistentUsers, userResults.size());

				identityResults = session.query().entities(EntityQueryBuilder.from(Identity.class).done()).list();

				Assert.assertEquals(i+preExistentUsers, identityResults.size());

				userResults = session.query().select(new SelectQueryBuilder().from(User.class, "u").done()).list();

				Assert.assertEquals(i+preExistentUsers, userResults.size());

				identityResults = session.query().select(new SelectQueryBuilder().from(Identity.class, "i").done()).list();

				Assert.assertEquals(i+preExistentUsers, identityResults.size());

			}

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void updateTestWithInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());
			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] { {"Stefan", "Prieler"}, {"Peter", "Brandner"} });

			PersistenceGmSession session3 = testUtilities.openSession(configuration.accessWithoutInitialData());
			testUtilities.checkUserList(session3, new String[][] { {"Stefan", "Prieler"}, {"Peter", "Brandner"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void updateTestWithoutInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithoutInitialData());
			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] { {"Peter", "Brandner"} });

			PersistenceGmSession session3 = testUtilities.openSession(configuration.accessWithoutInitialData());
			testUtilities.checkUserList(session3, new String[][] { {"Peter", "Brandner"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void deleteTestWithInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());
			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			User userPrieler = testUtilities.getUser(session2, "Stefan", "Prieler");
			session2.deleteEntity(userPrieler);
			session2.commit();

			PersistenceGmSession session3 = testUtilities.openSession(configuration.accessWithoutInitialData());
			testUtilities.checkUserList(session3, new String[][] { {"Peter", "Brandner"} });

			PersistenceGmSession session4 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session4, new String[][] { {"Peter", "Brandner"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void maxManipulationBuffersZeroDumps() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(3);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(0);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			testUtilities.checkUserCount(session, 2);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 1);

			user = session.create(User.T);
			user.setFirstName("Gunther");
			user.setLastName("Schenk");
			session.commit();

			testUtilities.checkUserCount(session, 3);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 2);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Bohnsack");
			session.commit();

			testUtilities.checkUserCount(session, 4);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 3);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Kern");
			session.commit();

			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] {
				{"Peter", "Brandner"}, 
				{"Stefan", "Prieler"}, 
				{"Gunther", "Schenk"}, 
				{"Rainer", "Bohnsack"}, 
				{"Rainer", "Kern"} });


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void maxManipulationBuffersOneDump() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(3);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(1);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			testUtilities.checkUserCount(session, 2);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 1);

			user = session.create(User.T);
			user.setFirstName("Gunther");
			user.setLastName("Schenk");
			session.commit();

			testUtilities.checkUserCount(session, 3);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 2);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Bohnsack");
			session.commit();

			testUtilities.checkUserCount(session, 4);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 3);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Kern");
			session.commit();

			testUtilities.checkDumpCount(accessId, 2);
			testUtilities.checkManipulationBuffers(accessId, 0);

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] {
				{"Peter", "Brandner"}, 
				{"Stefan", "Prieler"}, 
				{"Gunther", "Schenk"}, 
				{"Rainer", "Bohnsack"}, 
				{"Rainer", "Kern"} });


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void noManipulationBuffersTwoDumps() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(0);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(1);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			testUtilities.checkUserCount(session, 2);
			testUtilities.checkDumpCount(accessId, 2);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Gunther");
			user.setLastName("Schenk");
			session.commit();

			testUtilities.checkUserCount(session, 3);
			testUtilities.checkDumpCount(accessId, 2);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Bohnsack");
			session.commit();

			testUtilities.checkUserCount(session, 4);
			testUtilities.checkDumpCount(accessId, 2);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Kern");
			session.commit();

			testUtilities.checkUserCount(session, 5);
			testUtilities.checkDumpCount(accessId, 2);
			testUtilities.checkManipulationBuffers(accessId, 0);

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] {
				{"Peter", "Brandner"}, 
				{"Stefan", "Prieler"}, 
				{"Gunther", "Schenk"}, 
				{"Rainer", "Bohnsack"}, 
				{"Rainer", "Kern"} });


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void noManipulationBuffersOneDump() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(0);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(0);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			testUtilities.checkUserCount(session, 2);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Gunther");
			user.setLastName("Schenk");
			session.commit();

			testUtilities.checkUserCount(session, 3);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Bohnsack");
			session.commit();

			testUtilities.checkUserCount(session, 4);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);

			user = session.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Kern");
			session.commit();

			testUtilities.checkUserCount(session, 5);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] {
				{"Peter", "Brandner"}, 
				{"Stefan", "Prieler"}, 
				{"Gunther", "Schenk"}, 
				{"Rainer", "Bohnsack"}, 
				{"Rainer", "Kern"} });


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void manipulationBufferSmallSizeRestriction() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(-1);
			smoodDbAccess.setMaxManipulationBufferSize(100);
			smoodDbAccess.setKeepNOldDumps(0);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			User user = session.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session.commit();

			testUtilities.checkUserCount(session, 2);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 1);

			user = session.create(User.T);
			user.setFirstName("Gunther");
			user.setLastName("Schenk");
			session.commit();

			testUtilities.checkUserCount(session, 3);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(accessId, 0);


			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, new String[][] {
				{"Peter", "Brandner"}, 
				{"Stefan", "Prieler"}, 
				{"Gunther", "Schenk"}, 
			});


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void manipulationBufferSizeRestriction() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithoutInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(-1);
			int threshold = 3000;
			smoodDbAccess.setMaxManipulationBufferSize(threshold);
			smoodDbAccess.setKeepNOldDumps(0);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			boolean thresholdReached = false;
			int counter = 1;
			int maxTries = 1000;
			List<String[]> namesAdded = new ArrayList<String[]>();
			
			while (counter < maxTries) {
				
				User user = session.create(User.T);
				user.setFirstName("Peter");
				user.setLastName("Brandner "+counter);
				session.commit();
				namesAdded.add(new String[] {"Peter", "Brandner "+counter});
				
				int currentSize = testUtilities.getCurrentBufferSize(accessId);
				int bufferedManipulationsSize = testUtilities.getBufferedManipulationsSize(accessId);
				System.out.println("Current dump size: "+currentSize+", buffered manipulations size: "+bufferedManipulationsSize);

				testUtilities.checkUserCount(session, counter);

				if (thresholdReached) {
					assertThat(currentSize).isGreaterThan(0);
					assertThat(bufferedManipulationsSize).isEqualTo(0);
					break;
				} else if (bufferedManipulationsSize > threshold) {
					thresholdReached = true;
				}
				
				testUtilities.checkDumpCount(accessId, 1);
				testUtilities.checkManipulationBuffers(accessId, counter);
				
				counter++;
			}
			
			if (counter == maxTries) {
				throw new AssertionError("Could not reach threshold.");
			}
			
			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session2, namesAdded.toArray(new String[namesAdded.size()][]));


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void multipleQueryTestWithInitialData() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());
			for (int i=0; i<100; ++i) {
				testUtilities.checkUserList(session, new String[][] { {"Stefan", "Prieler"} });
			}

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	@Category(Slow.class)
	public void multipleQueryTestWithBigData() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithoutInitialData();
			PersistenceGmSession session = testUtilities.openSession(access);

			long startInsert = System.currentTimeMillis();

			int insertCount = 5000;
			int queryCount = 1000;

			User user = null;
			for (int i=0; i<insertCount; ++i) {
				user = session.create(User.T);
				user.setFirstName("Peter "+i);
				user.setLastName("Brandner "+i);
				if ((i % 100) == 0) {
					session.commit();	
				}
			}
			session.commit();

			long stopInsert = System.currentTimeMillis();
			System.out.println("Inserting "+insertCount+" users took "+(stopInsert-startInsert)+" ms");


			Random rnd = new Random();

			long startQuery = System.currentTimeMillis();

			for (int i=0; i<queryCount; ++i) {
				int index = rnd.nextInt(insertCount);
				testUtilities.getUser(session, "Peter "+index, "Brandner "+index);
			}

			long stopQuery = System.currentTimeMillis();
			System.out.println("Querying "+queryCount+" users took "+(stopQuery-startQuery)+" ms");

			if (access instanceof DistributedSmoodAccess) {
				DistributedSmoodAccess dsa = (DistributedSmoodAccess) access;
				System.out.println(dsa.getStatistics());
			}

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void queryTestWithReload() throws Exception {

		try {
			testUtilities.clearTables();

			PersistenceGmSession session = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session, new String[][] { {"Stefan", "Prieler"} });

			session = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserList(session, new String[][] { {"Stefan", "Prieler"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testQueryProperties() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			PersistenceGmSession session = testUtilities.openSession(access);

			SelectQuery query = new SelectQueryBuilder()
					.from(User.class, "u")
					.tc()
					.disjunction()
					.pattern().entity(User.class).property(User.lastName).close()
					.close()
					.done();
			List<Object> objectList = null;
			try {
				objectList = session.query().select(query).list();
			} catch (Exception e) {
				throw new AssertionError("Error while seraching for Users.", e);
			}

			if (objectList != null) {
				for (Object o : objectList) {
					User user = (User) o;

					GmSession attachedSession = user.detach();
					String directAccessLastName = user.getLastName();
					Assert.assertNull(directAccessLastName);
					user.attach(attachedSession);

					String lastName = user.getLastName();
					Assert.assertNotNull(lastName);
				}
			} else {
				throw new AssertionError("Could not get a user list.");
			}		

			if (access instanceof DistributedSmoodAccess) {
				DistributedSmoodAccess dsa = (DistributedSmoodAccess) access;
				System.out.println(dsa.getStatistics());
			}


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void noLimitsOnSmoodDumpsMinimalBuffers() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(0);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(-1);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			for (int i=0; i<100; ++i) {

				User user = session.create(User.T);
				user.setFirstName("Peter");
				user.setLastName("Brandner");
				session.commit();

				testUtilities.checkUserCount(session, i+2);
				testUtilities.checkDumpCount(accessId, i+2);
				testUtilities.checkManipulationBuffers(accessId, 0);

			}


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void noLimitsOnSmoodDumpsMinimalBuffersWithReload() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(0);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(-1);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			for (int i=0; i<100; ++i) {

				User user = session.create(User.T);
				user.setFirstName("Peter");
				user.setLastName("Brandner");
				session.commit();

				testUtilities.checkUserCount(session, i+2);
				testUtilities.checkDumpCount(accessId, i+2);
				testUtilities.checkManipulationBuffers(accessId, 0);

			}

			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserCount(session2, 101);


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Test
	public void multipleBuffersWithReload() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access = configuration.accessWithInitialData();
			String accessId = access.getAccessId();

			DistributedSmoodAccess smoodDbAccess = (DistributedSmoodAccess) access;
			smoodDbAccess.setMaxManipulationBuffers(-1);
			smoodDbAccess.setMaxManipulationBufferSize(-1);
			smoodDbAccess.setKeepNOldDumps(-1);

			testUtilities.checkDumpCount(accessId, 0);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			PersistenceGmSession session = testUtilities.openSession(access);

			testUtilities.checkUserCount(session, 1);
			testUtilities.checkDumpCount(accessId, 1);
			testUtilities.checkManipulationBuffers(smoodDbAccess.getAccessId(), 0);

			for (int i=0; i<100; ++i) {

				User user = session.create(User.T);
				user.setId("id"+i);
				user.setName("name"+i);
				user.setGlobalId("gid"+i);
				user.setFirstName("Peter "+i);
				user.setLastName("Brandner "+i);
				session.commit();

				testUtilities.checkUserCount(session, i+2);
				testUtilities.checkDumpCount(accessId, 1);
				testUtilities.checkManipulationBuffers(accessId, i+1);

				PersistenceGmSession tempSession = testUtilities.openSession(configuration.accessWithInitialData());
				testUtilities.checkUserCount(tempSession, i+2);
			}

			testUtilities.checkUserCount(session, 101);
			session.commit();
			
			PersistenceGmSession session2 = testUtilities.openSession(configuration.accessWithInitialData());
			testUtilities.checkUserCount(session2, 101);


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void multipleIdentifierPrefixes() throws Exception {

		try {
			testUtilities.clearTables();


			IncrementalAccess access1 = configuration.accessWithoutInitDataPrefix1();
			Assert.assertEquals("testAccess", access1.getAccessId());

			PersistenceGmSession session1 = testUtilities.openSession(access1);

			User user = session1.create(User.T);
			user.setFirstName("Rainer");
			user.setLastName("Bohnsack");
			session1.commit();



			IncrementalAccess access2 = configuration.accessWithoutInitDataPrefix2();
			Assert.assertEquals("testAccess", access2.getAccessId());
			Assert.assertEquals(access1.getAccessId(), access2.getAccessId());

			PersistenceGmSession session2 = testUtilities.openSession(access2);

			user = session2.create(User.T);
			user.setFirstName("Peter");
			user.setLastName("Brandner");
			session2.commit();


			testUtilities.checkUserCount(session1, 1);
			testUtilities.checkUserCount(session2, 1);


		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCompressingMarshaller() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access1 = configuration.accessWithoutInitialData();
			DistributedSmoodAccess dsa1 = (DistributedSmoodAccess) access1;
			dsa1.setMaxManipulationBuffers(0);
			PersistenceGmSession session1 = testUtilities.openSession(access1);

			User user1 = session1.create(User.T);
			user1.setFirstName("Peter");
			user1.setLastName("Brandner");
			session1.commit();

			String uncompressedEncodedData = testUtilities.getEncodedDataOfCurrentBuffer(access1.getAccessId());
			Assert.assertTrue(uncompressedEncodedData, uncompressedEncodedData.contains("xml"));

			testUtilities.clearTables();

			IncrementalAccess access2 = configuration.accessWithoutInitialData();
			DistributedSmoodAccess dsa2 = (DistributedSmoodAccess) access2;
			dsa2.setMaxManipulationBuffers(0);
			CharacterMarshaller rawMarshaller = dsa2.getXmlMarshaller();
			CompressingMarshallerDecorator compressingMarshaller = new CompressingMarshallerDecorator();
			compressingMarshaller.setEmbeddedMarshaller(rawMarshaller);
			dsa2.setXmlMarshaller(compressingMarshaller);
			PersistenceGmSession session2 = testUtilities.openSession(access2);

			User user2 = session2.create(User.T);
			user2.setFirstName("Peter");
			user2.setLastName("Brandner");
			session2.commit();

			String compressedEncodedData = testUtilities.getEncodedDataOfCurrentBuffer(access2.getAccessId());
			Assert.assertFalse(compressedEncodedData, compressedEncodedData.contains("xml"));

			PersistenceGmSession checkSession = testUtilities.openSession(access2);
			testUtilities.checkUserList(checkSession, new String[][] { {"Peter", "Brandner"} });

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testCompressingMarshallerSizeEfficiency() throws Exception {

		try {
			testUtilities.clearTables();

			IncrementalAccess access1 = configuration.accessWithoutInitialData();
			DistributedSmoodAccess dsa1 = (DistributedSmoodAccess) access1;
			dsa1.setMaxManipulationBuffers(0);
			PersistenceGmSession session1 = testUtilities.openSession(access1);

			for (int i=0; i<100; ++i) {
				User user1 = session1.create(User.T);
				user1.setFirstName("Peter");
				user1.setLastName("Brandner");
			}
			long uncompressedCommitStart = System.nanoTime();
			session1.commit();
			long uncompressedCommitStop = System.nanoTime();

			int uncompressedSize = testUtilities.getCurrentBufferSize(access1.getAccessId());
			testUtilities.checkUserCount(session1, 100);

			testUtilities.clearTables();

			IncrementalAccess access2 = configuration.accessWithoutInitialData();
			DistributedSmoodAccess dsa2 = (DistributedSmoodAccess) access2;
			dsa2.setMaxManipulationBuffers(0);
			CharacterMarshaller rawMarshaller2 = dsa2.getXmlMarshaller();
			CompressingMarshallerDecorator compressingMarshaller2 = new CompressingMarshallerDecorator();
			compressingMarshaller2.setEmbeddedMarshaller(rawMarshaller2);
			dsa2.setXmlMarshaller(compressingMarshaller2);
			PersistenceGmSession session2 = testUtilities.openSession(access2);

			for (int i=0; i<100; ++i) {
				User user2 = session2.create(User.T);
				user2.setFirstName("Peter");
				user2.setLastName("Brandner");
			}
			long compressedCommitStart = System.nanoTime();
			session2.commit();
			long compressedCommitStop = System.nanoTime();

			int compressedSize = testUtilities.getCurrentBufferSize(access2.getAccessId());
			
			assertThat(uncompressedSize).isGreaterThan(compressedSize);

			testUtilities.checkUserCount(session2, 100);
			
			System.out.println("Uncompressed commit time: "+(uncompressedCommitStop-uncompressedCommitStart)+" ns");
			System.out.println("Compressed commit time:   "+(compressedCommitStop-compressedCommitStart)+" ns");

		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
