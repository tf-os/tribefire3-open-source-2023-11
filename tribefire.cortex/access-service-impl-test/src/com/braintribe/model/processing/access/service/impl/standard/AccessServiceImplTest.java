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
package com.braintribe.model.processing.access.service.impl.standard;

import static com.braintribe.testing.tools.gm.GmTestTools.newSmoodAccessMemoryOnly;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessServiceException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.license.License;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;
import com.braintribe.model.processing.access.service.impl.standard.OriginAwareAccessRegistrationInfo.Origin;
import com.braintribe.model.processing.license.LicenseManagerRegistry;
import com.braintribe.model.processing.license.glf.GlfLicenseManager;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.testing.tools.gm.session.TestModelAccessoryFactory;

/**
 * Tests for {@link AccessServiceImpl}
 * 
 * 
 */
public class AccessServiceImplTest {

	private AccessServiceImpl impl;
	private IncrementalAccess cortexAccess;

	private static final String FIRST_ACCESS_ID = "access.documentum";
	private static final String SECOND_ACCESS_ID = "access.sharepoint";

	private IncrementalAccess firstAccess;
	private IncrementalAccess secondAccess;
	private Set<AccessRegistrationInfo> hardwiredAccessRegistrations;
	private Set<IncrementalAccess> hardwiredAccesses;

	@Before
	public void setUp() throws Exception {
		Logger.setLoggerImpl(DummyLogger.class);

		impl = new AccessServiceImpl();
		hardwiredAccessRegistrations = newSet();
		hardwiredAccesses = newSet();

		firstAccess = mockHardwiredAccess(FIRST_ACCESS_ID);
		secondAccess = mockHardwiredAccess(SECOND_ACCESS_ID);

		impl.setHardwiredAccessRegistrations(hardwiredAccessRegistrations);
		impl.setInternalCortexSessionSupplier(() -> new BasicPersistenceGmSession(this.cortexAccess));

		GlfLicenseManager manager = new GlfLicenseManager() {
			// @formatter:off
			@Override public void reloadLicense() { /*NOOP*/ }
			@Override public License getLicense() { return null; }
			@Override public void checkLicense() { /*NOOP*/ }
			// @formatter:on
		};
		impl.setLicenseManager(manager);

		LicenseManagerRegistry.setRegisteredLicenseManager(manager);
	}

	private IncrementalAccess mockHardwiredAccess(String accessId) {
		IncrementalAccess mockAccess = mock(IncrementalAccess.class);

		AccessRegistrationInfo regInfo = new AccessRegistrationInfo();
		regInfo.setAccess(mockAccess);
		regInfo.setAccessId(accessId);

		hardwiredAccessRegistrations.add(regInfo);
		hardwiredAccesses.add(mockAccess);

		return mockAccess;
	}

	@Test
	public void testGetAccessDelegate() throws Exception {
		IncrementalAccess actual = impl.getAccessDelegate(FIRST_ACCESS_ID);
		assertThat(actual).isEqualTo(firstAccess);
	}

	@Test(expected = AccessServiceException.class)
	public void testGetNonRegisteredAccessDelegateThrowsException() throws Exception {
		impl.getAccessDelegate("access.microsoft.access");
	}

	@Test
	public void testGetMetaModel() throws Exception {
		GmMetaModel metaModel = GmMetaModel.T.create();

		when(firstAccess.getMetaModel()).thenReturn(metaModel);

		GmMetaModel actual = impl.getMetaModel(FIRST_ACCESS_ID);
		assertThat(actual).isEqualTo(metaModel);
	}

	@Test
	public void testQueryEntities() throws Exception {
		EntityQuery request = EntityQuery.T.create();
		EntityQueryResult result = EntityQueryResult.T.create();

		when(firstAccess.queryEntities(request)).thenReturn(result);

		EntityQueryResult actual = impl.queryEntities(FIRST_ACCESS_ID, request);
		assertThat(actual).isEqualTo(result);
	}

	@Test
	public void testQueryProperty() throws Exception {
		PropertyQuery request = PropertyQuery.T.create();
		PropertyQueryResult result = PropertyQueryResult.T.create();

		when(secondAccess.queryProperty(request)).thenReturn(result);

		PropertyQueryResult actual = impl.queryProperty(SECOND_ACCESS_ID, request);
		assertThat(actual).isEqualTo(result);
	}

	@Test
	public void testApplyManipulation() throws Exception {
		ManipulationRequest request = prepareManipulationRequest();
		ManipulationResponse result = ManipulationResponse.T.create();

		when(firstAccess.applyManipulation(request)).thenReturn(result);

		ManipulationResponse actual = impl.applyManipulation(FIRST_ACCESS_ID, request);
		assertThat(actual).isEqualTo(result);
	}

	private ManipulationRequest prepareManipulationRequest() {
		Manipulation manipulation = DeleteManipulation.T.create();
		ManipulationRequest request = ManipulationRequest.T.create();
		request.setManipulation(manipulation);
		return request;
	}

	@Test
	public void testGetReferences() throws Exception {
		ReferencesRequest referencesRequest = ReferencesRequest.T.create();

		PersistentEntityReference reference = PersistentEntityReference.T.create();
		referencesRequest.setReference(reference);

		ReferencesResponse result = ReferencesResponse.T.create();
		when(secondAccess.getReferences(referencesRequest)).thenReturn(result);
		ReferencesResponse actual = impl.getReferences(SECOND_ACCESS_ID, referencesRequest);

		assertThat(actual).isEqualTo(result);
	}

	@Test
	public void testGetAccessIds() throws Exception {
		Set<String> actual = impl.getAccessIds();
		assertThat(actual).containsOnly(FIRST_ACCESS_ID, SECOND_ACCESS_ID);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetAccessIdsReturnsUnmodifieable() throws Exception {
		Set<String> actual = impl.getAccessIds();
		actual.add("breaking the law");
	}

	@Test
	public void testQuery() throws Exception {
		SelectQuery query = SelectQuery.T.create();
		SelectQueryResult result = SelectQueryResult.T.create();
		when(secondAccess.query(query)).thenReturn(result);

		SelectQueryResult actual = impl.query(SECOND_ACCESS_ID, query);

		assertThat(actual).isEqualTo(result);
	}

	@Test
	public void testRegisterAccess() throws Exception {
		String newAccessId = "access.hibernate";
		IncrementalAccess newAccess = registerMockAccessWithId(newAccessId);
		IncrementalAccess actual = impl.getAccessDelegate(newAccessId);

		assertThat(actual).isEqualTo(newAccess);
	}

	private IncrementalAccess registerMockAccessWithId(String newAccessId) {
		IncrementalAccess newAccess = mock(IncrementalAccess.class);

		AccessRegistrationInfo registrationInfo = new AccessRegistrationInfo();
		registrationInfo.setAccessId(newAccessId);
		registrationInfo.setAccess(newAccess);
		impl.registerAccess(registrationInfo);
		return newAccess;
	}

	@Test(expected = AccessServiceException.class)
	public void testUnRegisterAccess() throws Exception {
		impl.unregisterAccess(SECOND_ACCESS_ID);
		impl.getAccessDelegate(SECOND_ACCESS_ID);
	}

	@Test
	public void testConfiguredAccessAwareness() throws Exception {
		Origin actualOrigin = impl.getAccessRegistry().get(FIRST_ACCESS_ID).getOrigin();
		Assertions.assertThat(actualOrigin).isEqualTo(Origin.CONFIGURATION);
	}

	@Test
	public void testRegisteredAccessAwareness() throws Exception {
		String registeredAccessId = "access.salesforce";
		registerMockAccessWithId(registeredAccessId);
		Origin actualOrigin = impl.getAccessRegistry().get(registeredAccessId).getOrigin();
		Assertions.assertThat(actualOrigin).isEqualTo(Origin.REGISTRATION);
	}

	@Test
	public void testGetModelEnvironment() throws Exception {
		// prepare entities
		final String dataModelName = "dataModel";
		final String seriveModelName = "serviceModel";
		final String workbenchModelName = "workbenchModel";

		final String accessId = "access.dynamic.test";
		final String workbenchAccessId = "access.wb";

		cortexAccess = newSmoodAccessMemoryOnly("cortex", null);
		IncrementalAccess access = newSmoodAccessMemoryOnly(accessId, null);
		IncrementalAccess workbenchAccess = newSmoodAccessMemoryOnly(workbenchAccessId, null);

		// prepare
		AccessRegistrationInfo registrationInfo = new AccessRegistrationInfo();
		registrationInfo.setAccess(access);
		registrationInfo.setAccessId(accessId);
		registrationInfo.setModelName(dataModelName);
		registrationInfo.setWorkbenchModelName(workbenchModelName);
		registrationInfo.setModelAccessId("cortex");
		registrationInfo.setWorkbenchAccessId(workbenchAccessId);

		AccessRegistrationInfo metaModelRegInfo = new AccessRegistrationInfo();
		metaModelRegInfo.setAccess(cortexAccess);
		metaModelRegInfo.setAccessId("cortex");

		AccessRegistrationInfo workbenchRegInfo = new AccessRegistrationInfo();
		workbenchRegInfo.setAccess(workbenchAccess);
		workbenchRegInfo.setAccessId(workbenchAccessId);

		TestModelAccessoryFactory maf = new TestModelAccessoryFactory();
		maf.registerAccessModelAccessory(accessId, model(dataModelName));
		maf.registerServiceModelAccessory(accessId, model(seriveModelName));

		impl.setUserModelAccessoryFactory(maf);
		impl.setSystemModelAccessoryFactory(maf);
		impl.registerAccess(metaModelRegInfo);
		impl.registerAccess(registrationInfo);
		impl.registerAccess(workbenchRegInfo);

		PersistenceGmSession metaModelSession = new BasicPersistenceGmSession(cortexAccess);
		GmMetaModel workbenchMetaModel = metaModelSession.create(GmMetaModel.T);
		workbenchMetaModel.setName(workbenchModelName);
		metaModelSession.commit();

		ModelEnvironment me = impl.getModelEnvironment(accessId);

		assertThat(me.getDataAccessId()).isEqualTo(accessId);
		assertThat(me.getMetaModelAccessId()).isEqualTo("cortex");
		assertThat(me.getDataModel().getName()).isEqualTo(dataModelName);
		assertThat(me.getServiceModel().getName()).isEqualTo(seriveModelName);
		assertThat(me.getWorkbenchModel().getName()).isEqualTo(workbenchModelName);
		assertThat(me.getWorkbenchModelAccessId()).isEqualTo(workbenchAccessId);

		assertDataServiceModelDependOnSameRootModel(me);
	}

	private GmMetaModel model(String name) {
		GmMetaModel rootModel = GenericEntity.T.getModel().getMetaModel();
		rootModel = rootModel.clone(new StandardCloningContext());
		rootModel.setId("rootModel");

		GmMetaModel model = GmMetaModel.T.create();
		model.setName(name);
		model.getDependencies().add(rootModel);

		return model;
	}

	private void assertDataServiceModelDependOnSameRootModel(ModelEnvironment me) {
		String rootModelName = GenericEntity.T.getModel().name();

		GmMetaModel dataRootModel = first(me.getDataModel().getDependencies());
		GmMetaModel serviceRootModel = first(me.getServiceModel().getDependencies());

		assertThat(dataRootModel.getName()).isEqualTo(rootModelName);
		assertThat(serviceRootModel.getName()).isEqualTo(rootModelName);

		assertThat(dataRootModel).isSameAs(serviceRootModel);
	}

}
