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
package com.braintribe.model.processing.library.service.expert;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.library.service.LibraryBaseResult;
import com.braintribe.model.library.service.vulnerabilities.CheckVulnerabilities;
import com.braintribe.model.library.service.vulnerabilities.UpdateNvdMirror;
import com.braintribe.model.library.service.vulnerabilities.UpdatedNvdMirrorReport;
import com.braintribe.model.library.service.vulnerabilities.Vulnerabilities;
import com.braintribe.model.library.service.vulnerabilities.VulnerabilityDatabaseMaintenance;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.library.service.owasp.NistDataMirror;
import com.braintribe.model.processing.library.service.owasp.OwaspDependecyCheck;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.stream.DeleteOnCloseFileInputStream;

public class VulnerabilitiesExpert {

	private final static Logger logger = Logger.getLogger(VulnerabilitiesExpert.class);

	private OwaspDependecyCheck owaspDependecyCheck;
	private DependencyExpert dependencyExpert;

	private String nvdMirrorBasePath;
	private boolean nvdMirrorInitialized = false;

	private ClassLoader moduleClassloader;

	public LibraryBaseResult vulnerabilityDatabaseMaintenance(AccessRequestContext<VulnerabilityDatabaseMaintenance> context) {
		initialNvdMirrorInit();

		LibraryBaseResult result = LibraryBaseResult.T.create();

		VulnerabilityDatabaseMaintenance request = context.getRequest();
		StringBuilder sb = new StringBuilder();

		try {

			Boolean purgeDatabase = request.getPurgeDatabase();
			if (purgeDatabase != null && purgeDatabase.booleanValue()) {
				owaspDependecyCheck.purgeDatabase();
				sb.append("Purged the vulnerability database.\n");
			}
			Boolean update = request.getUpdateDatabase();
			if (update != null && update.booleanValue()) {
				sb.append("Updated the vulnerability database.\n");
				owaspDependecyCheck.updateDatabase();
			}

			result.setSuccess(true);
		} catch (Exception e) {
			String message = "Could not process DocumentLibraries request";
			logger.error(message, e);
			result.setSuccess(false);
			sb.append(message);
			sb.append('\n');
			sb.append(e.getMessage());
		}
		result.setMessage(sb.toString().trim());

		return result;

	}

	public Vulnerabilities checkVulnerabilities(AccessRequestContext<CheckVulnerabilities> context) {
		initialNvdMirrorInit();

		CheckVulnerabilities request = context.getRequest();

		Vulnerabilities result = Vulnerabilities.T.create();

		try {

			Map<Resource, Artifact> resourceMap = new HashMap<>();

			Map<String, DependencyResolutionResult> map = dependencyExpert.resolveArtifactAndDependencies(request);
			map.values().forEach(artifactInformation -> {
				//@formatter:off
				for (Artifact artifact : artifactInformation.getDependencies()) {
					artifact.getParts().values().stream()
						.filter(p -> p.getType().equals("jar"))
						.filter(p -> p.getRepositoryOrigin().equals("third-party"))
						.map(Part::getResource)
						.forEach(r -> resourceMap.put(r, artifact));
				}
				//@formatter:on				
			});

			Thread currentThread = Thread.currentThread();
			ClassLoader contextClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(moduleClassloader);
			final File finalResult;
			try {
				finalResult = owaspDependecyCheck.vulnerabilityCheck(request, result, resourceMap);
			} finally {
				currentThread.setContextClassLoader(contextClassLoader);
			}

			Resource callResource = Resource.createTransient(() -> new DeleteOnCloseFileInputStream(finalResult));

			String mimetype;
			String extension = FileTools.getExtension(finalResult.getName());
			switch (extension) {
				case "html":
					mimetype = "application/html";
					break;
				case "csv":
					mimetype = "text/csv";
					break;
				case "json":
					mimetype = "application/json";
					break;
				default:
					mimetype = ".bin";
					break;
			}

			callResource.setName("report." + extension);
			callResource.setMimeType(mimetype);
			callResource.setFileSize(finalResult.length());
			callResource.setCreated(new Date());

			result.setReport(callResource);

			result.setSuccess(true);

		} catch (Exception e) {
			logger.error("Could not process DocumentLibraries request", e);
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}

		return result;
	}

	private void initialNvdMirrorInit() {
		if (nvdMirrorInitialized) {
			return;
		}
		File folder = new File(nvdMirrorBasePath);
		boolean initialize = false;
		if (!folder.exists()) {
			initialize = true;
		} else {
			File[] files = folder.listFiles();
			if (files == null || files.length == 0) {
				initialize = true;
			}
		}
		if (initialize) {
			folder.mkdirs();
			updateNvdMirror(null);
		}
		nvdMirrorInitialized = true;
	}

	public UpdatedNvdMirrorReport updateNvdMirror(@SuppressWarnings("unused") AccessRequestContext<UpdateNvdMirror> context) {

		UpdatedNvdMirrorReport report = UpdatedNvdMirrorReport.T.create();

		NistDataMirror nvd = new NistDataMirror(nvdMirrorBasePath);
		try {
			boolean updated = nvd.mirror("1.1");
			report.setUpdated(updated);

		} catch (Exception e) {
			logger.debug(() -> "Error while trying to update the NVD Mirror at " + nvdMirrorBasePath, e);
			report.setUpdated(false);
			report.setErrorMessage(e.getMessage());
		}

		return report;
	}

	@Configurable
	@Required
	public void setOwaspDependecyCheck(OwaspDependecyCheck owaspDependecyCheck) {
		this.owaspDependecyCheck = owaspDependecyCheck;
	}
	@Required
	@Configurable
	public void setDependencyExpert(DependencyExpert dependencyExpert) {
		this.dependencyExpert = dependencyExpert;
	}
	@Required
	@Configurable
	public void setNvdMirrorBasePath(String nvdMirrorBasePath) {
		this.nvdMirrorBasePath = nvdMirrorBasePath;
	}
	@Required
	@Configurable
	public void setModuleClassloader(ClassLoader moduleClassloader) {
		this.moduleClassloader = moduleClassloader;
	}

}
