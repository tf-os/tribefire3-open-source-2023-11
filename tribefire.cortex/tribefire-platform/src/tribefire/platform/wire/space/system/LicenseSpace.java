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
package tribefire.platform.wire.space.system;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.model.processing.license.glf.GlfLicenseManager;
import com.braintribe.model.processing.license.glf.LicenseResourceLoader;
import com.braintribe.model.processing.license.glf.processor.LicenseResourceProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;

@Managed
public class LicenseSpace implements WireSpace {

	@Import
	private CortexAccessSpace cortexAccess;

	@Managed
	public LicenseManager manager() {
		GlfLicenseManager bean = new GlfLicenseManager();
		bean.setLicenseLoader(resourceLoader());
		bean.setSessionProvider(cortexAccess.sessionProvider());
		return bean;
	}

	@Managed
	public LicenseResourceProcessor licenseResourceProcessor() {
		LicenseResourceProcessor bean = new LicenseResourceProcessor();
		bean.setCortexSessionSupplier(cortexAccess.sessionProvider());
		return bean;
	}

	@Managed
	public LicenseResourceLoader resourceLoader() {
		try {
			LicenseResourceLoader bean = new LicenseResourceLoader();
			bean.setSessionProvider(cortexAccess.sessionProvider());
			return bean;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to create a " + LicenseResourceLoader.class.getName());
		}
	}

}
