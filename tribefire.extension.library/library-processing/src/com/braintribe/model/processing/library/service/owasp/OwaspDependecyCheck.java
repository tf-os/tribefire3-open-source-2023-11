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
package com.braintribe.model.processing.library.service.owasp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.owasp.dependencycheck.Engine;
import org.owasp.dependencycheck.dependency.CvssV3;
import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.dependency.Vulnerability;
import org.owasp.dependencycheck.exception.ExceptionCollection;
import org.owasp.dependencycheck.utils.Settings;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.library.service.vulnerabilities.CheckVulnerabilities;
import com.braintribe.model.library.service.vulnerabilities.Severity;
import com.braintribe.model.library.service.vulnerabilities.Vulnerabilities;
import com.braintribe.model.library.service.vulnerabilities.VulnerableLibrary;
import com.braintribe.model.processing.library.service.util.PdfTools;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;

public class OwaspDependecyCheck {

	private final static Logger logger = Logger.getLogger(OwaspDependecyCheck.class);

	protected File repositoryBasePath;
	private PdfTools pdfTools;
	protected ReentrantLock databaseLock = new ReentrantLock();
	protected File dataDirectory = null;

	protected String nvdMirrorBasePath;

	public File vulnerabilityCheck(CheckVulnerabilities request, Vulnerabilities result, Map<Resource, Artifact> resourceMap) throws Exception {

		Map<String, String> pathToArtifactMap = new HashMap<>();
		File[] files = getLibraryFiles(resourceMap.keySet());

		String title = request.getTitle() != null ? request.getTitle() : "Default";
		String targetFormat = request.getReportFormat().name();
		if (targetFormat.equals("PDF")) {
			targetFormat = "HTML";
		}
		File targetFile = null;
		try {
			targetFile = File.createTempFile("report", "." + targetFormat.toLowerCase());
			targetFile.delete();
		} catch (IOException ioe) {
			throw new Exception("Could not create a temporary file for the report.", ioe);
		}
		Dependency[] dependencies = null;
		try {
			dependencies = vulnerabilityCheck(files, title, targetFile, targetFormat);
		} catch (Exception e) {
			result.getLibrariesNotChecked().addAll(pathToArtifactMap.values());
			throw new Exception("Failed to check for vulnerabilities.", e);
		}

		TreeSet<String> cleanLibrariesSet = new TreeSet<>();
		HashSet<String> vulnerableLibrariesSet = new HashSet<>();
		if (dependencies != null) {
			for (Dependency dep : dependencies) {
				String filePath = dep.getFilePath();
				String artifact = pathToArtifactMap.get(filePath);

				if (artifact == null) {
					int idx = filePath.indexOf(".jar/");
					if (idx != -1) {
						String rawJarPath = filePath.substring(0, idx + 4);
						artifact = pathToArtifactMap.get(rawJarPath);
					}
				}

				Set<Vulnerability> vulnerabilities = dep.getVulnerabilities();
				if (vulnerabilities == null || vulnerabilities.isEmpty()) {
					if (artifact != null && !vulnerableLibrariesSet.contains(artifact)) {
						cleanLibrariesSet.add(artifact);
					}
				} else {
					for (Vulnerability vulnerability : vulnerabilities) {

						if (artifact != null) {
							vulnerableLibrariesSet.add(artifact);
							cleanLibrariesSet.remove(artifact);
						}

						VulnerableLibrary vl = VulnerableLibrary.T.create();
						vl.setLibraryId(artifact);
						vl.setName(vulnerability.getName());
						vl.setUrl("http://web.nvd.nist.gov/view/vuln/detail?vulnId=" + vulnerability.getName());
						CvssV3 cvssV3 = vulnerability.getCvssV3();
						Severity severity = Severity.Medium;
						if (cvssV3 != null) {
							float cvssScore = cvssV3.getBaseScore();
							vl.setCvssScore(cvssScore);
							if (cvssScore < 4f) {
								severity = Severity.Low;
							} else if (cvssScore >= 7f) {
								severity = Severity.High;
							} else {
								severity = Severity.Medium;
							}
						}
						vl.setSeverity(severity);
						vl.setDescription(vulnerability.getDescription());
						result.getVulnerableLibraries().add(vl);
					}
				}
			}
		} else {
			result.getLibrariesNotChecked().addAll(pathToArtifactMap.values());
		}

		result.getCleanLibraries().addAll(cleanLibrariesSet);

		return targetFile;
	}

	private File[] getLibraryFiles(Collection<Resource> resources) {

		List<File> files = resources.stream().map(r -> {
			FileResource fileResource = (FileResource) r;
			String path = fileResource.getPath();
			File f = new File(path);
			if (f.exists()) {
				return f;
			} else {
				return null;
			}
		}).filter(f -> f != null).collect(Collectors.toList());

		return files.toArray(new File[files.size()]);
	}

	private Settings ensureDatabase() throws Exception {
		File dir = getDataDirectory();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		ClassLoader classLoader = OwaspDependecyCheck.class.getClassLoader();
		Properties props = new Properties();
		try (InputStream resourceAsStream = classLoader.getResourceAsStream("dependencycheck.properties")) {
			props.load(resourceAsStream);
		} catch (Exception e) {
			logger.error("Could not load dependencycheck.properties with classloader " + classLoader, e);
		}
		Settings settings = new Settings(props);
		OwaspSettings.populateSettings(settings, dir, nvdMirrorBasePath);

		return settings;
	}

	public void purgeDatabase() throws Exception {
		File dir = getDataDirectory();
		databaseLock.lock();
		try {
			if (dir != null && dir.exists()) {
				try {
					FileTools.deleteDirectoryRecursively(dir);
				} catch (IOException e) {
					throw new Exception("Could not delete the database directory " + dir.getAbsolutePath(), e);
				}
			}
		} finally {
			databaseLock.unlock();
		}

	}

	public void updateDatabase() throws Exception {
		Settings settings = null;

		databaseLock.lock();
		try {
			settings = ensureDatabase();

			try (Engine engine = new Engine(settings)) {
				engine.doUpdates();
			}
		} finally {
			OwaspSettings.clearSettings(settings);
			databaseLock.unlock();
		}
	}

	private Dependency[] vulnerabilityCheck(File[] files, String title, File reportTargetPath, String reportFormat) throws Exception {

		Settings settings = null;

		databaseLock.lock();
		try {
			settings = ensureDatabase();

			try (Engine engine = new Engine(OwaspDependecyCheck.class.getClassLoader(), settings)) {
				engine.openDatabase(false, false);
				// engine.openDatabase();
				engine.scan(files);
				try {
					engine.analyzeDependencies();
				} catch (Exception e) {
					logger.info(() -> "Error while trying to analyze the libraries.", e);
				}
				final ExceptionCollection exceptions = callExecuteAnalysis(engine);
				engine.writeReports(title, reportTargetPath, reportFormat, exceptions);

				Dependency[] dependencies = engine.getDependencies();
				return dependencies;
			}
		} finally {
			OwaspSettings.clearSettings(settings);
			databaseLock.unlock();
		}
	}

	private ExceptionCollection callExecuteAnalysis(final Engine engine) throws Exception {
		ExceptionCollection exceptions = null;
		try {
			engine.analyzeDependencies();
		} catch (ExceptionCollection ex) {
			exceptions = ex;
		}
		return exceptions;
	}

	public void setRepositoryBasePath(File repositoryBasePath) {
		this.repositoryBasePath = repositoryBasePath;
	}

	@Required
	@Configurable
	public void setPdfTools(PdfTools pdfTools) {
		this.pdfTools = pdfTools;
	}

	public File getDataDirectory() {
		if (dataDirectory == null || !dataDirectory.exists()) {
			dataDirectory = new File(FileTools.getTempDir(), "owasp-data");
			dataDirectory.mkdirs();
		}
		return dataDirectory;
	}
	@Configurable
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}
	@Configurable
	public void setNvdMirrorBasePath(String nvdMirrorBasePath) {
		this.nvdMirrorBasePath = nvdMirrorBasePath;
	}

}
