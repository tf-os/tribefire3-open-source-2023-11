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
package tribefire.extension.library.wire.space;

import java.io.File;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.logging.Logger;
import com.braintribe.model.library.deployment.job.UpdateNvdMirrorScheduledJob;
import com.braintribe.model.library.deployment.service.WkHtmlToPdf;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.library.job.UpdateNvdMirrorScheduledJobImpl;
import com.braintribe.model.processing.library.service.LibraryServiceProcessor;
import com.braintribe.model.processing.library.service.expert.DependencyExpert;
import com.braintribe.model.processing.library.service.expert.DocumentationExpert;
import com.braintribe.model.processing.library.service.expert.LicenseExpert;
import com.braintribe.model.processing.library.service.expert.VulnerabilitiesExpert;
import com.braintribe.model.processing.library.service.libdoc.LibraryDocumentationGenerator;
import com.braintribe.model.processing.library.service.owasp.OwaspDependecyCheck;
import com.braintribe.model.processing.library.service.spdx.SpdxSupport;
import com.braintribe.model.processing.library.service.util.PdfTools;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.StrictSslSocketFactoryProvider;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.SystemToolsContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.WorkerContract;

@Managed
public class LibraryDeployablesSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(LibraryDeployablesSpace.class);

	@Import
	private SystemUserRelatedContract systemUserRelated;

	@Import
	private SystemToolsContract systemTools;

	@Import
	private WorkerContract worker;

	@Import
	private BuildDependencyResolutionContract buildDependencyResolution;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Import
	private ModuleReflectionContract moduleReflection;

	@Managed
	public LibraryServiceProcessor libraryProcessor(ExpertContext<com.braintribe.model.library.deployment.service.LibraryService> context) {

		com.braintribe.model.library.deployment.service.LibraryService deployable = context.getDeployable();

		LibraryServiceProcessor bean = new LibraryServiceProcessor();
		bean.setLicenseExpert(licenseExpert(deployable));
		bean.setDependencyExpert(dependencyExpert(deployable));
		bean.setVulnerabilitiesExpert(vulnerabilitiesExpert(deployable));
		bean.setDocumentationExpert(documentationExpert(deployable));

		return bean;
	}

	@Managed
	private LicenseExpert licenseExpert(com.braintribe.model.library.deployment.service.LibraryService deployable) {
		LicenseExpert bean = new LicenseExpert();
		bean.setSessionFactory(systemUserRelated.sessionFactory());
		bean.setDependencyExpert(dependencyExpert(deployable));

		bean.setRepositoryUsername(deployable.getRepositoryUsername());
		bean.setRepositoryPassword(deployable.getRepositoryPassword());
		bean.setRepositoryUrl(deployable.getRepositoryUrl());
		bean.setClientProvider(clientProvider());

		return bean;
	}

	@Managed
	private DependencyExpert dependencyExpert(com.braintribe.model.library.deployment.service.LibraryService deployable) {
		DependencyExpert bean = new DependencyExpert();
		bean.setRepositoryBasePath(deployable.getRepositoryBasePath());
		bean.setProfile(deployable.getProfile());

		bean.setRepositoryUsername(deployable.getRepositoryUsername());
		bean.setRepositoryPassword(deployable.getRepositoryPassword());
		bean.setRepositoryUrl(deployable.getRepositoryUrl());
		bean.setRavenhurstUrl(deployable.getRavenhurstUrl());
		
		return bean;
	}

	@Managed
	private VulnerabilitiesExpert vulnerabilitiesExpert(com.braintribe.model.library.deployment.service.LibraryService deployable) {
		VulnerabilitiesExpert bean = new VulnerabilitiesExpert();
		bean.setOwaspDependecyCheck(owaspDependecyCheck(deployable));
		bean.setDependencyExpert(dependencyExpert(deployable));
		bean.setNvdMirrorBasePath(deployable.getNvdMirrorBasePath());
		bean.setModuleClassloader(moduleReflection.moduleClassLoader());
		return bean;
	}

	@Managed
	private DocumentationExpert documentationExpert(com.braintribe.model.library.deployment.service.LibraryService deployable) {
		DocumentationExpert bean = new DocumentationExpert();
		bean.setSessionFactory(systemUserRelated.sessionFactory());
		bean.setDependencyExpert(dependencyExpert(deployable));
		bean.setDocumentationGenerator(documentationGenerator(deployable));
		bean.setSpdxSupport(spdxSupport());
		return bean;
	}

	@Managed
	private SpdxSupport spdxSupport() {
		SpdxSupport bean = new SpdxSupport();
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());
		return bean;
	}

	public OwaspDependecyCheck owaspDependecyCheck(com.braintribe.model.library.deployment.service.LibraryService deployable) {

		String repositoryBasePath = deployable.getRepositoryBasePath();

		OwaspDependecyCheck bean = new OwaspDependecyCheck();
		if (!StringTools.isEmpty(repositoryBasePath)) {
			File repo = new File(repositoryBasePath);
			if (repo.exists() && repo.isDirectory()) {
				bean.setRepositoryBasePath(repo);
			} else {
				logger.error("Could not verify the repository base path " + repositoryBasePath);
			}
		}
		bean.setPdfTools(pdfTools());

		bean.setNvdMirrorBasePath(deployable.getNvdMirrorBasePath());
		return bean;
	}

	@Managed
	public LibraryDocumentationGenerator documentationGenerator(com.braintribe.model.library.deployment.service.LibraryService deployable) {

		WkHtmlToPdf wkhtmltopdf = deployable.getWkHtmlToPdf();

		LibraryDocumentationGenerator bean = new LibraryDocumentationGenerator();
		PdfTools pdfTools = pdfTools();
		if (wkhtmltopdf != null && !StringTools.isEmpty(wkhtmltopdf.getPath())) {
			String path = wkhtmltopdf.getPath();
			File wkhtmltopdfFile = new File(path);
			if (wkhtmltopdfFile.exists()) {
				pdfTools.setWkhtmltopdf(wkhtmltopdf);
			} else {
				logger.error("Could not verify the wkhtmltopdf path " + path);
			}
		}
		bean.setPdfTools(pdfTools);
		bean.setExecutor(worker.threadPool());
		return bean;
	}

	@Managed
	public PdfTools pdfTools() {
		PdfTools bean = new PdfTools();
		bean.setCommandExecution(systemTools.commandExecution());
		return bean;
	}

	@Managed
	private HttpClientProvider clientProvider() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSslSocketFactoryProvider(sslSocketFactoryProvider());
		return bean;
	}
	@Managed
	private SslSocketFactoryProvider sslSocketFactoryProvider() {
		SslSocketFactoryProvider bean = TribefireRuntime.getAcceptSslCertificates() ? new EasySslSocketFactoryProvider()
				: new StrictSslSocketFactoryProvider();

		return bean;
	}

	@Managed
	public UpdateNvdMirrorScheduledJobImpl updateNvdMirrorScheduledJob(ExpertContext<UpdateNvdMirrorScheduledJob> context) {
		UpdateNvdMirrorScheduledJobImpl bean = new UpdateNvdMirrorScheduledJobImpl();
		bean.setSystemServiceRequestEvaluator(systemUserRelated.evaluator());
		return bean;
	}
}
