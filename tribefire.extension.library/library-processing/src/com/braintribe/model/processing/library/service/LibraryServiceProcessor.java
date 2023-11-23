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
package com.braintribe.model.processing.library.service;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.library.service.LibraryBaseRequest;
import com.braintribe.model.library.service.LibraryBaseResult;
import com.braintribe.model.library.service.dependencies.GetDependencies;
import com.braintribe.model.library.service.documentation.CreateSpdxSbom;
import com.braintribe.model.library.service.documentation.DocumentLibraries;
import com.braintribe.model.library.service.license.CheckLicenseAvailability;
import com.braintribe.model.library.service.license.CreateLicenseImportSpreadsheet;
import com.braintribe.model.library.service.license.ExportLicenseSpreadsheet;
import com.braintribe.model.library.service.license.ImportSpreadsheet;
import com.braintribe.model.library.service.vulnerabilities.CheckVulnerabilities;
import com.braintribe.model.library.service.vulnerabilities.UpdateNvdMirror;
import com.braintribe.model.library.service.vulnerabilities.VulnerabilityDatabaseMaintenance;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.library.service.expert.DependencyExpert;
import com.braintribe.model.processing.library.service.expert.DocumentationExpert;
import com.braintribe.model.processing.library.service.expert.LicenseExpert;
import com.braintribe.model.processing.library.service.expert.VulnerabilitiesExpert;

public class LibraryServiceProcessor implements AccessRequestProcessor<LibraryBaseRequest, LibraryBaseResult>, InitializationAware {

	private final static Logger logger = Logger.getLogger(LibraryServiceProcessor.class);

	private LicenseExpert licenseExpert;
	private DependencyExpert dependencyExpert;
	private VulnerabilitiesExpert vulnerabilitiesExpert;
	private DocumentationExpert documentationExpert;

	private AccessRequestProcessor<LibraryBaseRequest, LibraryBaseResult> delegate;

	@Override
	public LibraryBaseResult process(AccessRequestContext<LibraryBaseRequest> context) {
		return delegate.process(context);
	}

	// com.microsoft.sqlserver.jdbc.SQLServerDriver
	// jdbc:sqlserver://db-vm-sql2008.braintribe:1433;databaseName=INF_Licenses;sendStringParametersAsUnicode=false;integratedSecurity=false;encrypt=false;trustServerCertificate=true

	@Configurable
	@Required
	public void setLicenseExpert(LicenseExpert licenseExpert) {
		this.licenseExpert = licenseExpert;
	}
	@Required
	@Configurable
	public void setDependencyExpert(DependencyExpert dependencyExpert) {
		this.dependencyExpert = dependencyExpert;
	}
	@Required
	@Configurable
	public void setVulnerabilitiesExpert(VulnerabilitiesExpert vulnerabilitiesExpert) {
		this.vulnerabilitiesExpert = vulnerabilitiesExpert;
	}
	@Required
	@Configurable
	public void setDocumentationExpert(DocumentationExpert documentationExpert) {
		this.documentationExpert = documentationExpert;
	}

	@Override
	public void postConstruct() {
		delegate = AccessRequestProcessors.dispatcher(dispatching -> {
			dispatching.register(GetDependencies.T, dependencyExpert::getDependencies);

			dispatching.register(DocumentLibraries.T, documentationExpert::documentLibraries);
			dispatching.register(CreateSpdxSbom.T, documentationExpert::createSpdxSbom);

			dispatching.register(CheckLicenseAvailability.T, licenseExpert::checkLicenseAvailability);
			dispatching.register(CreateLicenseImportSpreadsheet.T, licenseExpert::createLicenseImportSpreadsheet);
			dispatching.register(ImportSpreadsheet.T, licenseExpert::importSpreadsheet);
			dispatching.register(ExportLicenseSpreadsheet.T, licenseExpert::exportLicenseSpreadsheet);

			dispatching.register(CheckVulnerabilities.T, vulnerabilitiesExpert::checkVulnerabilities);
			dispatching.register(VulnerabilityDatabaseMaintenance.T, vulnerabilitiesExpert::vulnerabilityDatabaseMaintenance);
			dispatching.register(UpdateNvdMirror.T, vulnerabilitiesExpert::updateNvdMirror);
		});
	}

}
