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
package tribefire.platform.wire.space.cortex.accesses;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.cortex.aspect.CryptoAspect;
import com.braintribe.model.cortex.aspect.FulltextAspect;
import com.braintribe.model.cortex.aspect.IdGeneratorAspect;
import com.braintribe.model.cortex.aspect.SecurityAspect;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.cortex.processorrules.BidiPropertyStateChangeProcessorRule;
import com.braintribe.model.cortex.processorrules.MetaDataStateChangeProcessorRule;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class DefaultDeployablesSpace implements WireSpace {

	public static final String CRYPTO_ASPECT_EXTERNAL_ID = "aspect.crypto.default";
	public static final String FULLTEXT_ASPECT_EXTERNAL_ID = "aspect.fulltext.default";
	public static final String ID_GENERATOR_ASPECT_EXTERNAL_ID = "aspect.idGenerator.default";
	public static final String QUERY_ASPECT_EXTERNAL_ID = "aspect.query.default";
	public static final String SECURITY_ASPECT_EXTERNAL_ID = "aspect.security.default";
	public static final String STATE_PROCESSING_ASEPCT_EXTERNAL_ID = "aspect.stateProcessing.default";

	@Managed
	public List<Deployable> defaultDeployables() {
		List<Deployable> bean = new ArrayList<>();
		bean.addAll(defaultAspects());
		bean.addAll(defaultStateChangeProcessorRules());

		// I want this to be in cortex, but not to be part of defaultAspects.
		bean.add(cryptoAspect());

		return bean;
	}

	@Managed
	public List<AccessAspect> defaultAspects() {
		return list( //
				fulltextAspect(), //
				idGeneratorAspect(), //
				securityAspect(), //
				stateProcessingAspect() //
				);
	}

	@Managed
	private AccessAspect cryptoAspect() {
		CryptoAspect bean = CryptoAspect.T.create();
		bean.setExternalId(CRYPTO_ASPECT_EXTERNAL_ID);
		bean.setName("Default Crypto Aspect");
		return bean;
	}

	@Managed
	private AccessAspect fulltextAspect() {
		FulltextAspect bean = FulltextAspect.T.create();
		bean.setExternalId(FULLTEXT_ASPECT_EXTERNAL_ID);
		bean.setName("Default Fulltext Aspect");
		return bean;
	}
	
	@Managed
	private AccessAspect idGeneratorAspect() {
		IdGeneratorAspect bean = IdGeneratorAspect.T.create();
		bean.setExternalId(ID_GENERATOR_ASPECT_EXTERNAL_ID);
		bean.setName("Default IdGenerator Aspect");
		return bean;
	}

	@Managed
	private AccessAspect securityAspect() {
		SecurityAspect bean = SecurityAspect.T.create();
		bean.setExternalId(SECURITY_ASPECT_EXTERNAL_ID);
		bean.setName("Default Security Aspect");
		return bean;
	}

	@Managed
	private StateProcessingAspect stateProcessingAspect() {
		StateProcessingAspect bean = StateProcessingAspect.T.create();
		bean.setExternalId(STATE_PROCESSING_ASEPCT_EXTERNAL_ID);
		bean.setName("Default StateProcessingAspect ");

		List<StateChangeProcessorRule> processorRules = new ArrayList<>();
		processorRules.addAll(defaultStateChangeProcessorRules());
		bean.setProcessors(processorRules);
		return bean;
	}

	private List<StateChangeProcessorRule> defaultStateChangeProcessorRules() {
		return list(bidiPropertyProcessorRule(), metaDataProcessorRule());
	}

	@Managed
	private BidiPropertyStateChangeProcessorRule bidiPropertyProcessorRule() {
		BidiPropertyStateChangeProcessorRule bean = BidiPropertyStateChangeProcessorRule.T.create();
		bean.setExternalId("processorRule.bidiProperty.default");
		bean.setName("Default BidiProperty ProcessorRule");
		return bean;
	}

	@Managed
	private MetaDataStateChangeProcessorRule metaDataProcessorRule() {
		MetaDataStateChangeProcessorRule bean = MetaDataStateChangeProcessorRule.T.create();
		bean.setExternalId("processorRule.metaData.default");
		bean.setName("Default MetaData ProcessorRule");
		return bean;
	}

}
