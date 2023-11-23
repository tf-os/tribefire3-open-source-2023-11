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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.model.access.security.SecurityAspect;
import com.braintribe.model.access.security.manipulation.experts.EntityDeletionExpert;
import com.braintribe.model.access.security.manipulation.experts.EntityInstantiationDisabledExpert;
import com.braintribe.model.access.security.manipulation.experts.MandatoryPropertyExpert;
import com.braintribe.model.access.security.manipulation.experts.PropertyModifiableExpert;
import com.braintribe.model.access.security.manipulation.experts.UniqueKeyPropertyExpert;
import com.braintribe.model.processing.aop.fulltext.ReplaceFulltextComparisons;
import com.braintribe.model.processing.aspect.crypto.CryptoAspect;
import com.braintribe.model.processing.deployment.utils.AccessLookupModelProvider;
import com.braintribe.model.processing.idgenerator.GlobalIdGeneratorAspect;
import com.braintribe.model.processing.idgenerator.IdGeneratorAspect;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.common.CryptoSpace;
import tribefire.platform.wire.space.cortex.accesses.PlatformSetupAccessSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;

@Managed
public class AccessAspectsSpace implements WireSpace, ReflectBeansForDeployment {

	@Import
	private AccessServiceSpace accessService;

	@Import
	private AuthContextSpace authContext;

	@Import
	private CryptoSpace crypto;

	@Import
	private DeploymentSpace deployment;
	
	@Import
	private GmSessionsSpace gmSessions;

	@Import
	private PlatformSetupAccessSpace platformSetupAccess;
	
	@Managed
	public SecurityAspect security() {
		SecurityAspect bean = new SecurityAspect();
		bean.setTrustedRoles(set("tf-internal"));
		// @formatter:off
		bean.setManipulationSecurityExperts(
				set(
					new UniqueKeyPropertyExpert(),
					new MandatoryPropertyExpert(),
					new EntityInstantiationDisabledExpert(),
					new EntityDeletionExpert(),
					new PropertyModifiableExpert()
				)
			);
		// @formatter:on
		return bean;
	}

	@Managed
	public ReplaceFulltextComparisons fulltext() {
		ReplaceFulltextComparisons bean = new ReplaceFulltextComparisons();
		return bean;
	}

	@Managed
	public AccessLookupModelProvider lookupModelProvider() {
		AccessLookupModelProvider bean = new AccessLookupModelProvider();
		bean.setAccessService(accessService.service());
		bean.setAccessIdentificationLookup(accessService.service());
		return bean;
	}

	@Managed
	public GlobalIdGeneratorAspect globalIdGenerator() {
		GlobalIdGeneratorAspect bean = new GlobalIdGeneratorAspect();
		return bean;
	}

	@Managed
	public IdGeneratorAspect idGenerator() {
		IdGeneratorAspect bean = new IdGeneratorAspect();
		bean.setDeployRegistry(deployment.registry());
		return bean;
	}

	@Managed
	public CryptoAspect crypto() {
		CryptoAspect bean = new CryptoAspect();
		bean.setCryptorProvider(crypto.cryptorProvider());
		bean.setCacheCryptorsPerContext(true);
		return bean;
	}

}
