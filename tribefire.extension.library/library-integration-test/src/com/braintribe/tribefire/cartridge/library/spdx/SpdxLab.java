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
package com.braintribe.tribefire.cartridge.library.spdx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.spdx.jacksonstore.MultiFormatStore;
import org.spdx.jacksonstore.MultiFormatStore.Format;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.Annotation;
import org.spdx.library.model.Checksum;
import org.spdx.library.model.ExternalDocumentRef;
import org.spdx.library.model.GenericModelObject;
import org.spdx.library.model.SpdxDocument;
import org.spdx.library.model.SpdxFile;
import org.spdx.library.model.SpdxModelFactory;
import org.spdx.library.model.SpdxPackage;
import org.spdx.library.model.SpdxPackageVerificationCode;
import org.spdx.library.model.TypedValue;
import org.spdx.library.model.enumerations.AnnotationType;
import org.spdx.library.model.enumerations.ChecksumAlgorithm;
import org.spdx.library.model.enumerations.FileType;
import org.spdx.library.model.license.AnyLicenseInfo;
import org.spdx.library.model.license.ExternalExtractedLicenseInfo;
import org.spdx.library.model.license.ExtractedLicenseInfo;
import org.spdx.library.model.license.LicenseInfoFactory;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.library.model.license.SpdxNoAssertionLicense;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.ISerializableModelStore;
import org.spdx.storage.listedlicense.SpdxListedLicenseWebStore;
import org.spdx.storage.simple.InMemSpdxStore;
import org.spdx.utility.verificationcode.JavaSha1ChecksumGenerator;
import org.spdx.utility.verificationcode.VerificationCodeGenerator;

import com.braintribe.utils.StringTools;

public class SpdxLab {
	public static void main(String[] args) throws Exception {

		SpdxListedLicenseWebStore sllw = new SpdxListedLicenseWebStore();
		Stream<TypedValue> items = sllw.getAllItems(null, SpdxConstants.CLASS_SPDX_LISTED_LICENSE);
		AtomicInteger counter = new AtomicInteger(0);
		items.forEach(e -> {
			System.out.println(e.getType() + " #" + e.getId());
			counter.incrementAndGet();
		});

		System.out.println("Listed: " + counter.get());

		List<String> spdxListedLicenseIds = sllw.getSpdxListedLicenseIds();

		System.out.println("Ids: " + spdxListedLicenseIds.size());

		SpdxListedLicense result = (SpdxListedLicense) SpdxModelFactory.createModelObject(sllw, "https://spdx.org/licenses/", "AGPL-1.0",
				SpdxConstants.CLASS_SPDX_LISTED_LICENSE, null);
		String licenseText = result.getLicenseText();

		System.out.println("List Version: " + sllw.getLicenseListVersion());
		System.out.println("Comment: " + result.getComment());
		System.out.println("Depr. Version: " + result.getDeprecatedVersion());
		System.out.println("Document URI: " + result.getDocumentUri());
		System.out.println("Id: " + result.getLicenseId());
		System.out.println("Name: " + result.getName());
		System.out.println("Type: " + result.getType());
		System.out.println("FSF Libre: " + result.getFsfLibre());
		System.out.println("Deprecated: " + result.isDeprecated());
		System.out.println("Is FSF Libre: " + result.isFsfLibre());
		System.out.println("OSI approved: " + result.isOsiApproved());
		System.out.println("Is strict: " + result.isStrict());
		Collection<String> seeAlso = result.getSeeAlso();
		if (seeAlso != null && !seeAlso.isEmpty()) {
			seeAlso.forEach(a -> System.out.println("See also: " + a));
		}
		System.out.println("Prop Names: " + result.getPropertyValueNames());
		System.out.println("License Text: " + StringTools.getFirstNCharacters(licenseText, 100));

		myTest("/Users/roman/Downloads/0target/test.json");

		SpdxNoAssertionLicense ref = (SpdxNoAssertionLicense) SpdxModelFactory.createModelObject(sllw, "https://spdx.org/licenses/",
				"LicenseRef-CUSTOM-1.0", SpdxConstants.CLASS_NOASSERTION_LICENSE, null);

		// GenericModelObject gmo = new GenericModelObject(sllw, "https://spdx.org/licenses/",
		// sllw.getNextId(IdType.Anonymous, "https://spdx.org/licenses/"), null, true);
		GenericModelObject gmo = new GenericModelObject();

		Checksum checksum2 = gmo.createChecksum(ChecksumAlgorithm.SHA256, "41ae84877dd3ce346a7d97fc7f493c8e9dcacfa67ce6974ace56baecee9a8411");

		// SpdxFileBuilder fileBuilder = ref.createSpdxFile("test-file-1", "Test File 1", new SpdxNoAssertionLicense(), Collections.emptyList(),
		// "Copyright Myself", checksum2);
		// fileBuilder.setComment("Comment 1");
		// SpdxFile spdxFile = fileBuilder.build();

		// ExternalExtractedLicenseInfo extr = (ExternalExtractedLicenseInfo) SpdxModelFactory.createModelObject(sllw, "https://spdx.org/licenses/",
		// "LicenseRef-CUSTOM-1.0", SpdxConstants.CLASS_EXTERNAL_EXTRACTED_LICENSE, null);

		ExternalDocumentRef ref2 = gmo.createExternalDocumentRef(SpdxConstants.EXTERNAL_DOC_REF_PRENUM + "DOCID1", "http://doc/uri/one", checksum2);

		// ExternalExtractedLicenseInfo

		ExtractedLicenseInfo e = new ExternalExtractedLicenseInfo(sllw, "https://spdx.org/licenses/",
				SpdxConstants.NON_STD_LICENSE_ID_PRENUM + "CUSTOM-1.0", null, true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ISerializableModelStore modelStore = new MultiFormatStore(sllw, Format.JSON_PRETTY);
		modelStore.serialize(e.getDocumentUri(), baos);

		sllw.close();

	}

	private static void myTest(String filePath) throws Exception {
		File outFile = new File(filePath);
		if (outFile.exists()) {
			outFile.delete();
		}
		InMemSpdxStore store = new InMemSpdxStore();
		ISerializableModelStore modelStore = new MultiFormatStore(store, Format.JSON_PRETTY);
		ModelCopyManager copyManager = new ModelCopyManager();

		String documentUri = "https://braintribe.com/spdx/doc/swissre.claims/claims-setup/1.0.99";

		try {
			SpdxDocument document = SpdxModelFactory.createSpdxDocument(modelStore, documentUri, copyManager);

			SimpleDateFormat dateFormat = new SimpleDateFormat(SpdxConstants.SPDX_DATE_FORMAT);
			String creationDate = dateFormat.format(new Date());
			document.setCreationInfo(
					document.createCreationInfo(Arrays.asList(new String[] { "Tool: Library Module", "Organization: Braintribe" }), creationDate));

			AnyLicenseInfo dataLicense = LicenseInfoFactory.parseSPDXLicenseString("CC0-1.0");
			document.setDataLicense(dataLicense);
			document.setName("SPDX Document for SwissRe Claims");
			document.setSpecVersion("SPDX-2.2");

			// Now that we have the basic document information filled in, let's create a package
			AnyLicenseInfo noAssertionLicense = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NOASSERTION_LICENSE_NAME);
			AnyLicenseInfo pkgDeclaredLicense = noAssertionLicense;

			String pkgId = modelStore.getNextId(IdType.SpdxId, documentUri);

			ExtractedLicenseInfo extLicense = (ExtractedLicenseInfo) SpdxModelFactory.createModelObject(modelStore, documentUri,
					"LicenseRef-CUSTOM-1.0", SpdxConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO, copyManager);
			extLicense.setExtractedText("custom license...yeah");
			extLicense.setName("Custom License Number 1 Name");

			AnyLicenseInfo agplLicense = LicenseInfoFactory.parseSPDXLicenseString("AGPL-1.0-only");
			List<AnyLicenseInfo> seenLic = Arrays.asList(new AnyLicenseInfo[] { agplLicense, extLicense });

			Checksum SHA1 = document.createChecksum(ChecksumAlgorithm.SHA1, "1123456789abcdef0123456789abcdef01234567");
			List<String> contributors = Arrays.asList(new String[] { "Contrib1", "Contrib2" });
			Annotation ANNOTATION1 = document.createAnnotation("Organization: Annotator1", AnnotationType.OTHER, creationDate, "Comment 1");
			Annotation ANNOTATION2 = document.createAnnotation("Tool: Annotator2", AnnotationType.REVIEW, creationDate, "Comment 2");
			//@formatter:off
			SpdxFile fileDep1 = document.createSpdxFile(store.getNextId(IdType.SpdxId, documentUri),
					"fileDep1", agplLicense, seenLic, "Copyright1", SHA1)
					.setLicenseComments("License Comments1")
					.setComment("Comment1")
					.setNoticeText("Notice Text")
					.addFileType(FileType.SOURCE)
					.setFileContributors(contributors)
					.setAnnotations(Arrays.asList(new Annotation[] {ANNOTATION1, ANNOTATION2}))
					.build();
			//@formatter:on

			VerificationCodeGenerator vcg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode verificationCode = vcg.generatePackageVerificationCode(new SpdxFile[] { fileDep1 }, new String[] {}, store,
					documentUri);

			//@formatter:off
			SpdxPackage pkg = document.createPackage(pkgId, "SwissRe Claims", noAssertionLicense, 
					"Copyright braintribe.com", pkgDeclaredLicense)
					.setFilesAnalyzed(true)
					.setFiles(Arrays.asList(new SpdxFile[] {fileDep1}))
					.setPackageVerificationCode(verificationCode)
					.setComment("This package describes the third party licenses of SwissRe Claims")
					.setHomepage("http://braintribe.com")
					.setDescription("Details about 3rd party licenses")
					.setOriginator("Organization: Braintribe")
					.setDownloadLocation("NOASSERTION")
					.setLicenseInfosFromFile(Arrays.asList(noAssertionLicense))
					.build();
			//@formatter:on

			document.getDocumentDescribes().add(pkg);
			document.getExtractedLicenseInfos().add(extLicense);

			List<String> warnings = document.verify();
			if (warnings.size() > 0) {
				System.out.println("The document has the following warnings:");
				for (String warning : warnings) {
					System.out.print("\t");
					System.out.println(warning);
				}
			}
			// Last step is to serialize
			try (OutputStream outputStream = new FileOutputStream(outFile)) {
				modelStore.serialize(documentUri, outputStream);
			}
			System.out.println("Example document written to " + filePath);

			System.exit(0);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Unexpected error creating SPDX document: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("I/O error writing output JSON file");
			System.exit(1);
		}

	}

	private static void create(String filePath) throws Exception {
		File outFile = new File(filePath);
		if (outFile.exists()) {
			outFile.delete();
		}

		/* First thing we need is a store to store document as build the SPDX document We'll chose the MultiFormatStore since it supports serializing
		 * to JSON files It takes an underlying model store as the first parameter - the inMemSpdxStore is a simple built in store included in the
		 * Spdx-Java-Library. The second parameter is the format to use when serializing or deserializing */
		ISerializableModelStore modelStore = new MultiFormatStore(new InMemSpdxStore(), Format.JSON_PRETTY);
		/* There are several other choices which can be made for a model store - most of which are in separate libraries that need to be included as
		 * dependencies. Below are a few examples: IModelStore modelStore = new InMemSpdxStore(); - the simplest store, but does not support
		 * serializing or deserializing ISerializableModelStore modelStore = new TagValueStore(new InMemSpdxStore()); Supports serializing and
		 * deserializing SPDX tag/value files ISerializableModelStore modelStore = new RdfStore(); Supports serializing and deserializing various RDF
		 * formats (e.g. RDF/XML, RDF/Turtle) ISerializableModelStore modelStore = new SpreadsheetStore(new InMemSpdxStore()); Supports serializing
		 * and deserializing .XLS or .XLSX spreadsheets */

		/* The documentUri is a unique identifier for the SPDX documents */
		String documentUri = "https://org.spdx.examples/spdx/doc/b7490f5a-b6ac-45e7-9971-4c27f1db97f7";
		/* The ModelCopyManager is used when using more than one Model Store. The Listed Licenses uses it's own model store, so we will need a
		 * ModelCopyManager to manage the copying of the listed license information over to the document model store */
		ModelCopyManager copyManager = new ModelCopyManager();
		try {
			// Time to create the document
			SpdxDocument document = SpdxModelFactory.createSpdxDocument(modelStore, documentUri, copyManager);
			// Let's add a few required fields to the document
			SimpleDateFormat dateFormat = new SimpleDateFormat(SpdxConstants.SPDX_DATE_FORMAT);
			String creationDate = dateFormat.format(new Date());
			document.setCreationInfo(document.createCreationInfo(Arrays.asList(new String[] { "Tool: Simple SPDX Document Example" }), creationDate));
			/* Now that we have the initial model object 'document' created, we can use the helper methods from that cleass to create any other model
			 * elements such as the CreationInfo above. These helper functions will use the same Document URI, Model Store and Model Copy Manager as
			 * the document element. */
			AnyLicenseInfo dataLicense = LicenseInfoFactory.parseSPDXLicenseString("CC0-1.0");
			/* Note that by passing in the modelStore and documentUri, the parsed license information is stored in the same model store we are using
			 * for the document */
			document.setDataLicense(dataLicense);
			document.setName("SPDX Example Document");
			document.setSpecVersion("SPDX-2.2");

			// Now that we have the basic document information filled in, let's create a package
			AnyLicenseInfo pkgConcludedLicense = LicenseInfoFactory.parseSPDXLicenseString("Apache-2.0 AND MIT");
			AnyLicenseInfo pkgDeclaredLicense = LicenseInfoFactory.parseSPDXLicenseString("Apache-2.0");
			String pkgId = modelStore.getNextId(IdType.SpdxId, documentUri);
			// The ID's used for SPDX elements must be unique. Calling the model store getNextId function is a
			// convenient and safe method to make sure you have a correctly formatted and unique ID
			SpdxPackage pkg = document.createPackage(pkgId, "Example Package Name", pkgConcludedLicense, "Copyright example.org", pkgDeclaredLicense)
					.setFilesAnalyzed(false) // Default is true and we don't want to add all the required fields
					.setComment("This package is used as an example in creating an SPDX document from scratch").setDownloadLocation("NOASSERTION")
					.build();
			/* Note that many of the more complex elements use a builder pattern as in the example above */

			document.getDocumentDescribes().add(pkg); // Let's add the package as the described element for the document
			// That's for creating the simple document. Let's verify to make sure nothing is out of spec
			List<String> warnings = document.verify();
			if (warnings.size() > 0) {
				System.out.println("The document has the following warnings:");
				for (String warning : warnings) {
					System.out.print("\t");
					System.out.println(warning);
				}
			}
			// Last step is to serialize
			try (OutputStream outputStream = new FileOutputStream(outFile)) {
				modelStore.serialize(documentUri, outputStream);
			}
			System.out.println("Example document written to " + filePath);
			System.exit(0);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Unexpected error creating SPDX document: " + e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.out.println("I/O error writing output JSON file");
			System.exit(1);
		}

	}
}
