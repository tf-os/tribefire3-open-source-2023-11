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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.library.service.ArtifactReferences;
import com.braintribe.model.library.service.license.CheckLicenseAvailability;
import com.braintribe.model.library.service.license.CreateLicenseImportSpreadsheet;
import com.braintribe.model.library.service.license.ExportLicenseSpreadsheet;
import com.braintribe.model.library.service.license.ImportSpreadsheet;
import com.braintribe.model.library.service.license.LicenseAvailability;
import com.braintribe.model.library.service.license.LicenseExportSpreadsheet;
import com.braintribe.model.library.service.license.LicenseImportSpreadsheet;
import com.braintribe.model.library.service.license.SpreadsheetImportReport;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.library.LibraryConstants;
import com.braintribe.model.processing.library.service.LibrariesMissingException;
import com.braintribe.model.processing.library.service.spreadsheet.SpreadsheetSupport;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.MemoryInputStreamProviders;

public class LicenseExpert {

	private final static Logger logger = Logger.getLogger(LicenseExpert.class);

	private static final DateTimeFormatter fileDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withLocale(Locale.US);

	private PersistenceGmSessionFactory sessionFactory;
	private DependencyExpert dependencyExpert;

	private HttpClientProvider clientProvider;
	private CloseableHttpClient client = null;

	private String repositoryUsername;
	private String repositoryPassword;
	private String repositoryUrl;
    
	public LicenseAvailability checkLicenseAvailability(AccessRequestContext<CheckLicenseAvailability> context) {

		CheckLicenseAvailability request = context.getRequest();
		LicenseAvailability la = LicenseAvailability.T.create();
		la.setSuccess(true);

		PersistenceGmSession librarySession = sessionFactory.newSession(LibraryConstants.LIBRARY_ACCESS_ID);
		TreeSet<String> librariesFound = new TreeSet<>();
		try {

			List<String> dependencyList = resolveArtifactReferences(request);

			dependencyExpert.getLibraries(request, librarySession, dependencyList, librariesFound, null);

		} catch (LibrariesMissingException lme) {

			TreeSet<String> librariesMissing = lme.getLibrariesMissing();
			List<String> list = new ArrayList<String>(librariesMissing);
			la.setLicensesMissing(list);

		} catch (Exception e) {

			logger.error("Error while trying to check the availability of licenses for", e);
			la.setSuccess(false);
			la.setMessage(e.getMessage());

		} finally {

			List<String> list = new ArrayList<String>(librariesFound);
			la.setLicensesAvailable(list);
		}

		return la;
	}

	private List<String> resolveArtifactReferences(ArtifactReferences artifactReferences) {
		Set<String> result = new HashSet<>();

		if (artifactReferences.getResolveDependencies()) {

			Map<String, DependencyResolutionResult> artifactInformationMap = dependencyExpert.resolveArtifactAndDependencies(artifactReferences);
			artifactInformationMap.values().forEach(artifactInformation -> {

				List<Artifact> externalArtfacts = new ArrayList<>();

				if (artifactReferences.getIncludeTerminalArtifact()) {
					ArtifactIdentification resolvedArtifact = artifactInformation.getResolvedArtifact();
					boolean ignored = false;
					for (String ignore : artifactReferences.getIgnoredDependencies()) {
						if (resolvedArtifact.getGroupId().startsWith(ignore)) {
							ignored = true;
							break;
						}
					}
					if (!ignored) {
						result.add(resolvedArtifact.asString());
					}
				}

				//@formatter:off
				for (Artifact artifact : artifactInformation.getDependencies()) {
					artifact.getParts().values().stream()
					.filter(p -> p.getType().equals("jar"))
					.filter(p -> p.getRepositoryOrigin().equals("third-party"))
					.map(Part::getResource)
					.forEach(r -> externalArtfacts.add(artifact));
				}
				//@formatter:on

				externalArtfacts.stream().map(Artifact::asString).forEach(result::add);
			});

		} else {
			result.addAll(artifactReferences.getArtifactIdList());
		}

		return new ArrayList<>(result);
	}

	public LicenseImportSpreadsheet createLicenseImportSpreadsheet(AccessRequestContext<CreateLicenseImportSpreadsheet> context) {

		CreateLicenseImportSpreadsheet request = context.getRequest();

		LicenseImportSpreadsheet result = LicenseImportSpreadsheet.T.create();

		try {
			PersistenceGmSession librarySession = sessionFactory.newSession(LibraryConstants.LIBRARY_ACCESS_ID);

			List<String> dependencyList = resolveArtifactReferences(request);

			AbstractSet<String> dependencySet = LibraryTools.splitDependencyList(request.getIgnoredDependencies(), dependencyList);

			byte[] spreadsheetBytes = SpreadsheetSupport.createImportSpreadsheet(librarySession, dependencySet, this::getChecksumAndName);

			Resource callResource = Resource.createTransient(MemoryInputStreamProviders.from(spreadsheetBytes));

			callResource.setName("license-import.xlsx");
			callResource.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			callResource.setFileSize((long) spreadsheetBytes.length);
			callResource.setCreated(new Date());

			result.setLicenseImportSpreadsheet(callResource);

			result.setSuccess(true);

		} catch (Exception e) {
			logger.error("Could not process CreateLicenseImportSpreadsheet request", e);
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}

		return result;
	}

	@SuppressWarnings("unused")
	public LicenseExportSpreadsheet exportLicenseSpreadsheet(AccessRequestContext<ExportLicenseSpreadsheet> context) {

		LicenseExportSpreadsheet result = LicenseExportSpreadsheet.T.create();

		try {
			PersistenceGmSession librarySession = sessionFactory.newSession(LibraryConstants.LIBRARY_ACCESS_ID);

			byte[] spreadsheetBytes = SpreadsheetSupport.createExportSpreadsheet(librarySession);

			Resource callResource = Resource.createTransient(MemoryInputStreamProviders.from(spreadsheetBytes));

			String now = DateTools.encode(new Date(), fileDateTimeFormatter);

			callResource.setName("license-export-" + now + ".xlsx");
			callResource.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			callResource.setFileSize((long) spreadsheetBytes.length);
			callResource.setCreated(new Date());

			result.setLicenseExportSpreadsheet(callResource);

			result.setSuccess(true);

		} catch (Exception e) {
			logger.error("Could not process ExportLicenseSpreadsheet request", e);
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}

		return result;
	}

	public SpreadsheetImportReport importSpreadsheet(AccessRequestContext<ImportSpreadsheet> context) {

		SpreadsheetImportReport result = SpreadsheetImportReport.T.create();

		ImportSpreadsheet request = context.getRequest();
		try {
			PersistenceGmSession librarySession = sessionFactory.newSession(LibraryConstants.LIBRARY_ACCESS_ID);

			Resource res = request.getImportSpreadsheet();
			byte[] spreadsheetBytes = null;
			try (InputStream in = res.openStream()) {
				spreadsheetBytes = IOTools.slurpBytes(in);
			} catch (Exception e) {
				throw new Exception("Could not read resource from request.", e);
			}

			SpreadsheetSupport.importSpreadsheet(librarySession, spreadsheetBytes, result);

		} catch (Exception e) {
			logger.error("Could not process ImportSpreadsheet request", e);
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}

		return result;
	}

	private Map<String, String> getChecksumAndName(String artifactId) {

		try {
			if (client == null) {
				client = clientProvider.provideHttpClient();
			}

			int idx1 = artifactId.indexOf(':');
			int idx2 = artifactId.indexOf('#');
			String groupId = artifactId.substring(0, idx1).replace(".", "/");
			String artifact = artifactId.substring(idx1 + 1, idx2);
			String version = artifactId.substring(idx2 + 1);

			Map<String, String> result = new HashMap<>();

			
			
			String url = "https://" + repositoryUsername + ":" + repositoryPassword
					+ "@" + repositoryUrl.replace("https://", "") + "/api/storage/third-party/" + groupId + "/" + artifact + "/" + version + "/" + artifact
					+ "-" + version + ".jar";
			HttpGet get = new HttpGet(url);
			try (CloseableHttpResponse response = client.execute(get)) {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					String json;
					try (InputStream in = entity.getContent()) {
						json = IOTools.slurp(in, "UTF-8");
					}

					JSONParser parser = new JSONParser();
					try (Reader reader = new StringReader(json)) {
						JSONObject root = (JSONObject) parser.parse(reader);
						JSONObject checksums = (JSONObject) root.get("checksums");

						String sha1 = (String) checksums.get("sha1");
						String sha256 = (String) checksums.get("sha256");
						result.put("SHA1", sha1);
						result.put("SHA256", sha256);
						result.put("name", artifact + "-" + version + ".jar");
						return result;
					}

				}
				HttpTools.consumeResponse(response);
			}
			url = "https://" + repositoryUsername + ":" + repositoryPassword + "@" + repositoryUrl.replace("https://", "") + "/api/storage/third-party/"
					+ groupId + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".pom";
			get = new HttpGet(url);
			try (CloseableHttpResponse response = client.execute(get)) {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					String json;
					try (InputStream in = entity.getContent()) {
						json = IOTools.slurp(in, "UTF-8");
					}

					JSONParser parser = new JSONParser();
					try (Reader reader = new StringReader(json)) {
						JSONObject root = (JSONObject) parser.parse(reader);
						JSONObject checksums = (JSONObject) root.get("checksums");

						String sha1 = (String) checksums.get("sha1");
						String sha256 = (String) checksums.get("sha256");
						result.put("SHA1", sha1);
						result.put("SHA256", sha256);
						result.put("name", artifact + "-" + version + ".pom");
						return result;
					}

				} else {
					logger.info(() -> "Could not resolve " + artifactId);
				}
				HttpTools.consumeResponse(response);
			}

			return result;

		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}
	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Required
	@Configurable
	public void setDependencyExpert(DependencyExpert dependencyExpert) {
		this.dependencyExpert = dependencyExpert;
	}
	@Configurable
	@Required
	public void setClientProvider(HttpClientProvider clientProvider) {
		this.clientProvider = clientProvider;
	}
	@Configurable
	public void setRepositoryUsername(String repositoryUsername) {
		this.repositoryUsername = repositoryUsername;
	}
	@Configurable
	public void setRepositoryPassword(String repositoryPassword) {
		this.repositoryPassword = repositoryPassword;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

}
