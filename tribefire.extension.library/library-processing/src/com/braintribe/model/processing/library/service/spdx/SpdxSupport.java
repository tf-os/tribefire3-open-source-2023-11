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
package com.braintribe.model.processing.library.service.spdx;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.jacksonstore.MultiFormatStore.Format;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.Checksum;
import org.spdx.library.model.SpdxCreatorInformation;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxFile;
import org.spdx.library.model.SpdxModelFactory;
import org.spdx.library.model.SpdxPackage;
import org.spdx.library.model.SpdxPackageVerificationCode;
import org.spdx.library.model.enumerations.ChecksumAlgorithm;
import org.spdx.library.model.enumerations.FileType;
import org.spdx.library.model.license.AnyLicenseInfo;
import org.spdx.library.model.license.ExtractedLicenseInfo;
import org.spdx.library.model.license.LicenseInfoFactory;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.ISerializableModelStore;
import org.spdx.storage.simple.InMemSpdxStore;
import org.spdx.utility.verificationcode.JavaSha1ChecksumGenerator;
import org.spdx.utility.verificationcode.VerificationCodeGenerator;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.library.service.documentation.CreateSpdxSbom;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class SpdxSupport {

	private StreamPipeFactory streamPipeFactory = null;

	public Resource createSpdxSbom(CreateSpdxSbom request, Consumer<String> warningConsumer, List<Library> libraries) throws Exception {

		InMemSpdxStore store = new InMemSpdxStore();
		ISerializableModelStore modelStore = new MultiFormatStore(store, Format.JSON_PRETTY);
		ModelCopyManager copyManager = new ModelCopyManager();

		String documentUri = createDocumentUri(request);

		try {
			SpdxDocument document = SpdxModelFactory.createSpdxDocument(modelStore, documentUri, copyManager);

			String creationDate = getTimestamp(request);
			document.setCreationInfo(createCreatorInformation(request, document, creationDate));

			AnyLicenseInfo dataLicense = LicenseInfoFactory.parseSPDXLicenseString("CC0-1.0");
			document.setDataLicense(dataLicense);
			document.setName("SPDX Document for SwissRe Claims");
			document.setSpecVersion("SPDX-2.2");

			// Now that we have the basic document information filled in, let's create a package
			AnyLicenseInfo noAssertionLicense = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NOASSERTION_LICENSE_NAME);
			AnyLicenseInfo pkgDeclaredLicense = noAssertionLicense;

			String pkgId = modelStore.getNextId(IdType.SpdxId, documentUri);

			List<SpdxFile> fileList = new ArrayList<>();
			Map<String, ExtractedLicenseInfo> externalLicenses = new TreeMap<>();

			for (Library library : libraries) {

				AnyLicenseInfo assumedLicense;
				AnyLicenseInfo seenLicenses;

				String spdxLicenseId = library.getSpdxLicenseId();
				String spdxLicenseExpression = library.getSpdxLicenseExpression();

				List<DistributionLicense> licenses = library.getLicenses();

				if (!spdxLicenseId.startsWith(SpdxConstants.NON_STD_LICENSE_ID_PRENUM)) {

					assumedLicense = LicenseInfoFactory.parseSPDXLicenseString(spdxLicenseId);
					seenLicenses = LicenseInfoFactory.parseSPDXLicenseString(spdxLicenseExpression);

				} else {

					ExtractedLicenseInfo extLicense = acquireExtractedLicenseInfo(externalLicenses, library.getLicenses(), modelStore, documentUri,
							copyManager);

					assumedLicense = extLicense;
					seenLicenses = extLicense;
				}

				List<AnyLicenseInfo> seenLicList = Arrays.asList(new AnyLicenseInfo[] { seenLicenses });

				Checksum SHA1 = document.createChecksum(ChecksumAlgorithm.SHA1, library.getSha1());

				String copyright = library.getCopyright();

				//@formatter:off
				SpdxFile spdxFile = document.createSpdxFile(store.getNextId(IdType.SpdxId, documentUri),
						library.getFilename(), assumedLicense, seenLicList, copyright, SHA1)
						.addFileType(FileType.SOURCE)
						.setFileContributors(Arrays.asList(library.getOrganization()))
						.build();
				//@formatter:on

				fileList.add(spdxFile);
			}

			VerificationCodeGenerator vcg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode verificationCode = vcg.generatePackageVerificationCode(fileList.toArray(new SpdxFile[0]), new String[] {},
					store, documentUri);

			String creatorOrganization = request.getCreatorOrganization();
			String originator = StringTools.isBlank(creatorOrganization) ? null : "Organization: " + creatorOrganization;
			String downloadLocation = request.getDownloadLocation() != null ? request.getDownloadLocation() : "NOASSERTION";
			//@formatter:off
			SpdxPackage pkg = document.createPackage(pkgId, request.getPackageName(), noAssertionLicense, 
					request.getCopyrightText(), pkgDeclaredLicense)
					.setFilesAnalyzed(true)
					.setFiles(fileList)
					.setPackageVerificationCode(verificationCode)
					.setComment(request.getPackageComment())
					.setHomepage(request.getHomepage())
					.setDescription(request.getPackageDescription())
					.setOriginator(originator)
					.setDownloadLocation(downloadLocation)
					.setLicenseInfosFromFile(Arrays.asList(noAssertionLicense))
					.build();
			//@formatter:on

			document.getDocumentDescribes().add(pkg);
			document.getExtractedLicenseInfos().addAll(externalLicenses.values());

			List<String> warnings = document.verify();
			if (warnings.size() > 0) {
				for (String warning : warnings) {
					warningConsumer.accept(warning);
				}
			}

			StreamPipe pipe = streamPipeFactory.newPipe("spdx-sbom");

			long size;
			try (CountingOutputStream outputStream = new CountingOutputStream(pipe.openOutputStream())) {
				modelStore.serialize(documentUri, outputStream);
				size = outputStream.getCount();
			}

			Resource callResource = Resource.createTransient(() -> pipe.openInputStream());

			callResource.setName("package.spdx.json");
			callResource.setMimeType("application/json");
			callResource.setFileSize(size);
			callResource.setCreated(new Date());

			return callResource;

		} catch (InvalidSPDXAnalysisException e) {
			throw Exceptions.unchecked(e, "Unexpected error creating SPDX document: " + e.getMessage());
		}
	}

	private ExtractedLicenseInfo acquireExtractedLicenseInfo(Map<String, ExtractedLicenseInfo> externalLicenses, List<DistributionLicense> licenses,
			ISerializableModelStore modelStore, String documentUri, ModelCopyManager copyManager) {

		if (licenses.size() > 1) {
			throw new IllegalStateException("More than one custom license is not yet supported.");
		}
		DistributionLicense license = licenses.get(0);
		String licenseId = license.getSpdxLicenseId();

		return externalLicenses.computeIfAbsent(licenseId, lId -> {

			ExtractedLicenseInfo extLicense;
			try {
				String licenseText;
				try (InputStream in = license.getLicenseFile().openStream()) {
					licenseText = IOTools.slurp(in, "UTF-8");
				}

				extLicense = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, documentUri, lId,
						SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, copyManager);
				extLicense.setExtractedText(licenseText);
				extLicense.setName(license.getName());

				return extLicense;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while trying to create a new extracted license info for " + lId);
			}
		});

	}

	private SpdxCreatorInformation createCreatorInformation(CreateSpdxSbom request, SpdxDocument document, String creationDate)
			throws InvalidSPDXAnalysisException {

		List<String> infoList = new ArrayList<>();

		String tool = request.getCreatorTool();
		if (!StringTools.isEmpty(tool)) {
			infoList.add("Tool: " + tool);
		}
		String organization = request.getCreatorOrganization();
		if (!StringTools.isEmpty(organization)) {
			infoList.add("Organization: " + organization);
		}

		return document.createCreationInfo(infoList, creationDate);
	}

	private String createDocumentUri(CreateSpdxSbom request) {
		String documentUri = request.getDocumentUri();
		if (!StringTools.isBlank(documentUri)) {
			return documentUri;
		}
		String dep = request.getArtifactIdList().get(0);
		int idx1 = dep.indexOf(':');
		int idx2 = dep.indexOf('#');
		String groupId = dep.substring(0, idx2);
		String artifactId = dep.substring(idx1 + 1, idx2);
		String version = dep.substring(idx2 + 1);

		documentUri = "https://braintribe.com/spdx/doc/" + groupId + "/" + artifactId + "/" + version;
		return documentUri;
	}

	private String getTimestamp(CreateSpdxSbom request) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(SpdxConstants.SPDX_DATE_FORMAT);
		Date date = request.getTimestamp();
		if (date == null) {
			date = new Date();
		}
		String creationDate = dateFormat.format(date);
		return creationDate;
	}

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

}
