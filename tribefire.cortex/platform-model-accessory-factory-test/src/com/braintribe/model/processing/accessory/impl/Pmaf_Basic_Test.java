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
package com.braintribe.model.processing.accessory.impl;

import static com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry.PERSPECTIVE_PERSISTENCE_SESSION;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.removeFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.meta.StreamWith;
import com.braintribe.model.extensiondeployment.meta.UploadWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_1A_CortexModel;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_1B_CustomModel;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_2A_AccessAndServiceDomain;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_3A_ExtensionMetaData;
import com.braintribe.model.processing.accessory.test.cortex.MaInit_4A_EssentialAndNonEssentialMd;
import com.braintribe.model.processing.accessory.test.custom.model.CustomEntity;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.resource.Resource;

/**
 * 
 */
public class Pmaf_Basic_Test extends AbstractPlatformModelAccessoryFactoryTest {

	/**
	 * @see MaInit_1A_CortexModel
	 * @see MaInit_1B_CustomModel
	 * @see MaInit_2A_AccessAndServiceDomain
	 * @see MaInit_3A_ExtensionMetaData
	 * @see MaInit_4A_EssentialAndNonEssentialMd
	 */
	@Test
	public void fullPlatformMaTest() throws Exception {
		loadMafAndMas();

		assertCortexAccess();
		assertCortexAccess_FromDifferentPerspectives();
		assertCortexServiceDomain();
		assertCustomAccess();
		assertCustomAccessServiceDomain();
		assertCustomServiceDomain();
	}

	private void loadMafAndMas() {
		maf = contract.platformModelAccessoryFactory();
		assertThat(maf).isNotNull();

		cortexMa = maf.getForAccess(CORTEX_ACCESS_EXTERNAL_ID);
		smokeTestMa(cortexMa);

		cortexSdMa = maf.getForServiceDomain(CORTEX_ACCESS_EXTERNAL_ID);
		smokeTestMa(cortexSdMa);

		customAccessMa = maf.getForAccess(CUSTOM_ACCESS_EXTERNAL_ID);
		smokeTestMa(customAccessMa);

		customAccessSdMa = maf.getForServiceDomain(CUSTOM_ACCESS_EXTERNAL_ID);
		smokeTestMa(customAccessSdMa);

		customSdMa = maf.getForServiceDomain(CUSTOM_SERVICE_DOMAIN_EXTERNAL_ID);
		smokeTestMa(customSdMa);

		emptySdMa = maf.getForServiceDomain(EMPTY_SERVICE_DOMAIN_EXTERNAL_ID);
		smokeTestMa(emptySdMa);
	}

	private void smokeTestMa(ModelAccessory ma) {
		assertThat(ma).isNotNull();
		assertThat(ma.getModel()).isNotNull();

		assertThat(ma.getOracle()).isNotNull();
		assertThat(ma.getOracle().getGmMetaModel()).isSameAs(ma.getModel()).isNotNull();

		assertThat(ma.getCmdResolver()).isNotNull();
		assertThat(ma.getCmdResolver().getModelOracle()).isSameAs(ma.getOracle());
	}

	private void assertCortexAccess() {
		StreamWith sw = cortexMa.getMetaData().entityType(Resource.T).meta(StreamWith.T).exclusive();
		assertThat(sw).isNotNull();
		assertImmutable(sw);

		BinaryRetrieval binRetrieval = sw.getRetrieval();
		assertThat(binRetrieval).isNotNull();
		assertThat(binRetrieval.getExternalId()).isEqualTo(CSA_BINARY_RETRIEVAL_EXTERNAL_ID);

		GmEntityType gmType = cortexMa.getOracle().findEntityTypeOracle(Resource.T).asGmType();
		assertThat(gmType.getSuperTypes()).isSameAs(gmType.getSuperTypes());
		assertCollectionImmutable(gmType.getSuperTypes());
	}

	/** @see MaInit_4A_EssentialAndNonEssentialMd */
	private void assertCortexAccess_FromDifferentPerspectives() {
		assertContains(cortexMa, Visible.T, true); // essential, directly on access model
		assertContains(cortexMa, Mandatory.T, true); // essential, via access extensions
		assertContains(cortexMa, StreamWith.T, true); // non-essential, directly on access model
		assertContains(cortexMa, UploadWith.T, true); // non-essential, via access extensions

		ModelAccessory cortexMaForSession = maf.forPerspective(PERSPECTIVE_PERSISTENCE_SESSION).getForAccess(CORTEX_ACCESS_EXTERNAL_ID);
		assertContains(cortexMaForSession, Visible.T, true);
		assertContains(cortexMaForSession, Mandatory.T, true);
		assertContains(cortexMaForSession, StreamWith.T, false);
		assertContains(cortexMaForSession, UploadWith.T, false);
	}

	private <T extends MetaData> void assertContains(ModelAccessory cortexMa, EntityType<T> mdType, boolean expected) {
		T md = cortexMa.getMetaData().useCase("perspective").meta(mdType).exclusive();
		if (!expected) {
			assertThat(md).isNull();
			return;
		}

		assertThat(md).isNotNull();

		MetaDataSelector selector = md.getSelector();
		assertThat(selector).isInstanceOf(UseCaseSelector.class);

		UseCaseSelector castedSelector = (UseCaseSelector) selector;
		assertThat(castedSelector.getUseCase()).isEqualTo("perspective");
	}

	private void assertCollectionImmutable(List<GmEntityType> list) {
		assertMethodForbidden(list, l -> l.clear(), "clear");
		assertMethodForbidden(list, l -> l.add(null), "add()");
		assertMethodForbidden(list, l -> l.addAll(asList(null, null)), "add()");
		assertMethodForbidden(list, l -> l.remove(null), "remove()");
		assertMethodForbidden(list, l -> l.retainAll(asList(null, null)), "remove()");
		assertMethodForbidden(list, l -> removeFirst(l), "remove()");
	}

	private void assertMethodForbidden(List<GmEntityType> list, Consumer<List<GmEntityType>> listChanger, String operation) {
		try {
			listChanger.accept(list);

		} catch (RuntimeException e) {
			return;
		}
		fail("Collection is not immutable, operation still doable: " + operation);
	}

	/**
	 * Tests: <br/>
	 * - cortex service domain is enriched - contains the {@link QueryRequest}
	 */
	private void assertCortexServiceDomain() {
		assertModelContainsQueryRequest(cortexSdMa);
	}

	private void assertCustomAccess() {
		Name nameMd = customAccessMa.getCmdResolver().getMetaData().entityType(CustomEntity.T).meta(Name.T).exclusive();
		assertThat(nameMd).isNotNull();
		assertImmutable(nameMd);
	}

	/**
	 * Tests: <br/>
	 * - custom access service domain is enriched - contains the {@link QueryRequest}
	 */
	private void assertCustomAccessServiceDomain() {
		assertModelContainsQueryRequest(customAccessSdMa);
	}

	private void assertCustomServiceDomain() {
		// not sure we have anything to test
	}

	private void assertModelContainsQueryRequest(ModelAccessory ma) {
		EntityTypeOracle o = ma.getOracle().findEntityTypeOracle(QueryRequest.T);
		assertThat(o).isNotNull();
	}

	private void assertImmutable(GenericEntity ge) {
		try {
			ge.setPartition(null);

		} catch (RuntimeException e) {
			return;
		}
		fail("Entity is not immutable: " + ge);
	}

}
