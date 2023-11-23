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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.AbstractDelegatingAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.session.api.persistence.CommitListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.testing.tools.gm.access.TransientNonIncrementalAccess;

public class BasicPersistenceGmSessionInductionTest {

	BasicPersistenceGmSession session;
	private SmoodAccess plainAccess;

	@Before
	public void setup() throws Exception {

		NonIncrementalAccess niAccess = new TransientNonIncrementalAccess(MetaModelTools.provideRawModel(Person.T));

		plainAccess = new SmoodAccess();
		plainAccess.setDataDelegate(niAccess);
		plainAccess.setAccessId("access.myPersons");
		plainAccess.setReadWriteLock(EmptyReadWriteLock.INSTANCE);

		IncrementalAccess access = new AbstractDelegatingAccess() {

			@Override
			protected IncrementalAccess getDelegate() {
				return plainAccess;
			}

			@Override
			public synchronized ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
				try {
					final ManipulationResponse response = getDelegate().applyManipulation(manipulationRequest);

					EntityReference reference = null;
					for (Manipulation m : manipulationRequest.getManipulation().inline()) {
						if (m instanceof InstantiationManipulation) {
							InstantiationManipulation im = (InstantiationManipulation) m;
							EntityReference candidate = (EntityReference) im.getEntity();
							if (candidate.getTypeSignature().equalsIgnoreCase(Person.class.getName())) {
								reference = candidate;
								break;
							}
						}
					}

					CompoundManipulation newInducedManipulation = CompoundManipulation.T.create();
					newInducedManipulation.setCompoundManipulationList(new ArrayList<Manipulation>());

					if (response.getInducedManipulation() != null)
						newInducedManipulation.getCompoundManipulationList().add(response.getInducedManipulation());

					response.setInducedManipulation(newInducedManipulation);

					if (reference != null) {
						for (Manipulation m : response.getInducedManipulation().inline()) {
							if (m instanceof ChangeValueManipulation) {
								ChangeValueManipulation cvm = (ChangeValueManipulation) m;
								EntityProperty owner = (EntityProperty) cvm.getOwner();
								if (EntRefHashingComparator.INSTANCE.compare(reference, owner.getReference())
										&& owner.getPropertyName().equals("id")) {

									PersistenceGmSession plainSession = openPlainSession();
									Person johnDoe = plainSession.query().entity(Person.T, cvm.getNewValue()).find();

									if (!johnDoe.getName().equals("Assi Doe"))
										continue;

									Person johnDoesBestFriend = plainSession.create(Person.T);
									johnDoesBestFriend.setName("Best Friend");

									johnDoe.setBestFriend(johnDoesBestFriend);

									plainSession.listeners().add(new CommitListener() {
										@Override
										public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
											// NOOP
										}

										@Override
										public void onAfterCommit(PersistenceGmSession session, Manipulation manipulation,
												Manipulation inducedManipluation) {

											if (manipulation != null) {
												newInducedManipulation.getCompoundManipulationList().add(manipulation);
											}
											if (inducedManipluation != null) {
												newInducedManipulation.getCompoundManipulationList().add(inducedManipluation);
											}
										}
									});

									plainSession.commit();
								}
							}
						}
					}

					return response;
				} catch (GmSessionException e) {
					throw new ModelAccessException(e);
				}

			}
		};

		session = new BasicPersistenceGmSession(access);

	}

	private PersistenceGmSession openPlainSession() {
		BasicPersistenceGmSession plainSession = new BasicPersistenceGmSession(plainAccess);
		return plainSession;
	}

	@Test
	public void testIdInduction() throws Exception {
		Person p = session.create(Person.T);
		p.setName("John Doe");

		session.commit();

		assertThat(p.<Object> getId()).isNotNull();
	}

	@Test
	public void testAccessSideAssignmentInduction() throws Exception {
		Person p = session.create(Person.T);
		p.setName("Assi Doe");

		session.commit();

		assertThat(p.getBestFriend()).isNotNull();
		assertThat(p.getBestFriend().getName()).isEqualTo("Best Friend");
	}

}
