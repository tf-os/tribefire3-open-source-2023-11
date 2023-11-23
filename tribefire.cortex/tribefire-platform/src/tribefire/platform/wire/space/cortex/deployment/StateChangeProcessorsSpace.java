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
package tribefire.platform.wire.space.cortex.deployment;

import com.braintribe.model.processing.deployment.processor.BidiPropertyStateChangeProcessor;
import com.braintribe.model.processing.deployment.processor.MetaDataStateChangeProcessorRule;
import com.braintribe.model.processing.license.glf.processor.LicenseActivatedProcessor;
import com.braintribe.model.processing.license.glf.processor.LicenseUploadedProcessor;
import com.braintribe.model.processing.web.cors.CortexCorsStateChangeProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.model.ModelChangeNotifier;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.servlets.SecurityServletSpace;
import tribefire.platform.wire.space.system.LicenseSpace;

@Managed
public class StateChangeProcessorsSpace implements WireSpace {

	@Import
	private DeploymentSpace deployment;

	@Import
	private LicenseSpace license;

	@Import
	private AuthContextSpace authContext;

	@Import
	private SecurityServletSpace securityServlet;

	@Import
	private RpcSpace rpc;
	
	@Managed
	public LicenseUploadedProcessor licenseUpload() {
		LicenseUploadedProcessor bean = new LicenseUploadedProcessor();
		bean.setUsernameProvider(authContext.currentUser().userNameProvider());
		return bean;
	}

	@Managed
	public LicenseActivatedProcessor licenseActivated() {
		LicenseActivatedProcessor bean = new LicenseActivatedProcessor();
		bean.setLicenseManager(license.manager());
		return bean;
	}

	@Managed
	public BidiPropertyStateChangeProcessor bidiProperty() {
		BidiPropertyStateChangeProcessor bean = new BidiPropertyStateChangeProcessor();
		return bean;
	}

	@Managed 
	public MetaDataStateChangeProcessorRule<?> metadata() {
		MetaDataStateChangeProcessorRule<?> bean = new MetaDataStateChangeProcessorRule<>();
		bean.setDeployRegistry(deployment.registry());
		return bean;
	}

	@Managed
	public CortexCorsStateChangeProcessor cors() {
		CortexCorsStateChangeProcessor bean = new CortexCorsStateChangeProcessor();
		bean.setCortexCorsHandler(securityServlet.cortexCorsHandler());
		return bean;
	}

	@Managed
	public ModelChangeNotifier modelAccessoryNotifier() {
		ModelChangeNotifier bean = new ModelChangeNotifier();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}
}
