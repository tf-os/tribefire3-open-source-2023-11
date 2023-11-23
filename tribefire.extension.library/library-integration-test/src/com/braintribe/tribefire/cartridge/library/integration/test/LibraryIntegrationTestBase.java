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
package com.braintribe.tribefire.cartridge.library.integration.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.BeforeClass;

import com.braintribe.logging.Logger;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.tribefire.cartridge.library.integration.test.util.ArtifactInformation;
import com.braintribe.tribefire.cartridge.library.load.LicenseResource;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;

public abstract class LibraryIntegrationTestBase extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(LibraryIntegrationTestBase.class);

	protected static PersistenceGmSession librarySession = null;

	protected static List<LicenseResource> resources = new ArrayList<>();
	protected static Map<String, DistributionLicense> licenseMap = new HashMap<>();

	protected static boolean initialized = false;

	@BeforeClass
	public static void beforeClass() throws Exception {
		ZipSecureFile.setMinInflateRatio(0.001d);

		if (!initialized) {
			apiFactory().build();
			PersistenceGmSessionFactory sessionFactory = apiFactory().buildSessionFactory();
			librarySession = sessionFactory.newSession("library.access");

			emptyLibraryDatabase();
			populateLibraryDatabase();
			initialized = true;
			log.info("Test preparation finished successfully!");
		}

	}

	protected static void emptyLibraryDatabase() {
		EntityQuery query = EntityQueryBuilder.from(Library.T).done();
		List<Library> list = librarySession.query().entities(query).list();
		for (Library l : list) {
			librarySession.deleteEntity(l);
		}
		librarySession.commit();
	}

	protected static void log(String message) {
		System.out.println(DateTools.encode(new Date(), DateTools.ISO8601_DATE_WITH_MS_FORMAT) + " [Master]: " + message);
	}

	protected static void populateLibraryDatabase() throws Exception {
		loadDistributionLicenses();
		Set<String> existingLibraryNames = loadLibraryNames();
		List<ArtifactInformation> aiList = loadLicenseInformation();
		insertArtifactInformation(aiList, existingLibraryNames);
	}

	private static void insertArtifactInformation(List<ArtifactInformation> aiList, Set<String> existingLibraryNames) {

		int count = 0;
		for (ArtifactInformation ai : aiList) {

			if (!existingLibraryNames.contains(ai.getArtifact())) {

				Library l = librarySession.create(Library.T);
				l.setGroupId(ai.getGroupId());
				l.setArtifactId(ai.getArtifactId());
				l.setVersion(ai.getVersion());
				l.setCopyright(ai.getCopyright());
				l.setLibraryUrl(ai.getLibraryUrl());
				if (ai.getLicenses() != null && !ai.getLicenses().isEmpty()) {
					l.setLicenses(ai.getLicenses());
				}
				l.setOrganization(ai.getOrganization());
				l.setName(ai.getLibraryName());
				l.setSpdxLicenseId(ai.getSpdxLicenseId());
				l.setSpdxLicenseExpression(ai.getSpdxLicenseExpression());
				l.setSha1(ai.getSha1());
				l.setSha256(ai.getSha256());
				l.setFilename(ai.getFilename());
				librarySession.commit();

				count++;
			}
		}
		log("Added: " + count + " libraries.");

	}

	private static List<ArtifactInformation> loadLicenseInformation() throws Exception {
		File file = new File("res/outdated-library-list.xlsx");
		List<ArtifactInformation> aiList = new ArrayList<>();
		Workbook workbook = null;
		FileInputStream excelFile = null;
		try {

			excelFile = new FileInputStream(file);
			workbook = new XSSFWorkbook(excelFile);
			Sheet datatypeSheet = workbook.getSheetAt(0);

			Iterator<Row> iterator = datatypeSheet.iterator();

			int row = 1;
			while (iterator.hasNext()) {

				Row currentRow = iterator.next();

				if (row == 1) {
					row++;
					continue;
				}

				Iterator<Cell> cellIterator = currentRow.iterator();

				ArtifactInformation ai = new ArtifactInformation();

				int col = 1;
				while (cellIterator.hasNext()) {

					Cell currentCell = cellIterator.next();
					String value = currentCell.getStringCellValue();

					switch (col) {
						case 1:
							ai.setArtifact(value);
							break;
						case 2:
							ai.setFilename(value);
							break;
						case 3:
							ai.setLicenseName(value);
							break;
						case 4:
							ai.setSpdxLicenseId(value);
							break;
						case 5:
							ai.setSpdxLicenseExpression(value);
							break;
						case 6:
							ai.setSha1(value);
							break;
						case 7:
							ai.setSha256(value);
							break;
						case 8:
							ai.setLibraryName(value);
							break;
						case 9:
							ai.setLibraryUrl(value);
							break;
						case 10:
							ai.setCopyright(value);
							break;
						case 11:
							ai.setOrganization(value);
							break;
						default:
							break;
					}

					col++;
				}

				if (ai.isValid()) {
					aiList.add(ai);
				} else {
					log("Invalid: " + ai);
				}

				row++;
			}
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find Excel file: " + file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new Exception("Error while reading Excel file: " + file.getAbsolutePath(), e);
		} finally {
			IOTools.closeCloseable(workbook, null);
			IOTools.closeCloseable(excelFile, null);
		}

		log("Found " + aiList.size() + " artifact information lines.");
		for (ArtifactInformation ai : aiList) {
			List<String> licenseNames = ai.getLicenseNames();
			if (!licenseNames.isEmpty()) {
				for (String ln : licenseNames) {
					DistributionLicense distributionLicense = licenseMap.get(ln);
					if (distributionLicense != null) {
						ai.addLicense(distributionLicense);
					} else {
						log("Could not find a license for name " + ln + " which is referenced in " + ai);
					}
				}
			} else {
				log(ai.toString() + " does not contain license information.");
			}
		}

		return aiList;

	}

	private static void loadDistributionLicenses() throws IOException, FileNotFoundException {
		popuplateLicenseResources();
		for (LicenseResource r : resources) {
			Resource originalResource = null;
			try (FileInputStream fis = new FileInputStream(r.originalFile)) {
				originalResource = librarySession.resources().create().name(r.originalFile.getName()).mimeType("text/plain").store(fis);
			}
			Resource pdfResource = null;
			try (FileInputStream fis = new FileInputStream(r.pdfFile)) {
				pdfResource = librarySession.resources().create().name(r.pdfFile.getName()).mimeType("application/pdf").store(fis);
			}

			DistributionLicense dl = librarySession.create(DistributionLicense.T);
			dl.setLicenseFile(originalResource);
			dl.setLicenseFilePdf(pdfResource);
			dl.setName(r.name);
			dl.setCommercial(r.commercial);
			dl.setUrl(r.url);
			dl.setInternalDocumentationUrl(r.internalUrl);

			if (r.isSpdxListed) {
				dl.setSpdxLicenseId(r.spdxId);
			} else {
				dl.setSpdxLicenseId("LicenseRef-" + r.spdxId);
			}
			dl.setSpdxListedLicense(r.isSpdxListed);

		}
		librarySession.commit();

		EntityQuery query = EntityQueryBuilder.from(DistributionLicense.T).done();
		List<DistributionLicense> list = librarySession.query().entities(query).list();
		for (DistributionLicense l : list) {
			licenseMap.put(l.getName(), l);
		}
	}

	private static void popuplateLicenseResources() {
		File root = new File("res/licenses");

		//@formatter:off
		resources.add(new LicenseResource(new File(root, "netcdf.txt"), new File(root, "netcdf.pdf"), "netCDF License", "NetCDF", true, "http://www.unidata.ucar.edu/software/netcdf/copyright.html", null, false));
		resources.add(new LicenseResource(new File(root, "apache-1.1.txt"), new File(root, "apache-1.1.pdf"), "Apache License, Version 1.1", "Apache-1.1", true, "http://www.apache.org/licenses/LICENSE-1.1", null, false));
		resources.add(new LicenseResource(new File(root, "apache-2.0.txt"), new File(root, "apache-2.0.pdf"), "Apache License, Version 2.0", "Apache-2.0", true, "http://www.apache.org/licenses/LICENSE-2.0.txt", null, false));
		resources.add(new LicenseResource(new File(root, "bouncycastle.txt"), new File(root, "bouncycastle.pdf"), "Bouncy Castle License", "BouncyCastle", false, "http://www.bouncycastle.org/licence.html", null, false));
		resources.add(new LicenseResource(new File(root, "bsd.txt"), new File(root, "bsd.pdf"), "BSD License", "BSD-1-Clause", true, "http://www.linfo.org/bsdlicense.html", null, false));
		resources.add(new LicenseResource(new File(root, "bsd-2-clause.txt"), new File(root, "bsd-2-clause.pdf"), "2-Clause BSD License", "BSD-2-Clause", true, "https://opensource.org/licenses/bsd-license.php", null, false));
		resources.add(new LicenseResource(new File(root, "bsd-3-clause.txt"), new File(root, "bsd-3-clause.pdf"), "3-Clause BSD License", "BSD-3-Clause", true, "http://opensource.org/licenses/BSD-3-Clause", null, false));
		resources.add(new LicenseResource(new File(root, "objectweb.txt"), new File(root, "objectweb.pdf"), "ObjectWeb Project License", "ObjectWeb", false, "http://asm.objectweb.org/license.html", null, false));
		resources.add(new LicenseResource(new File(root, "threeten.txt"), new File(root, "threeten.pdf"), "ThreeTen Project License", "ThreeTen", false, "https://raw.githubusercontent.com/ThreeTen/threetenbp/master/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "jwnl.txt"), new File(root, "jwnl.pdf"), "JWNL Project License", "JWNL", false, "http://svn.code.sf.net/p/jwordnet/code/trunk/jwnl/license.txt", null, false));
		resources.add(new LicenseResource(new File(root, "antlr.txt"), new File(root, "antlr.pdf"), "ANTLR 4 License", "ANTLR-PD", true, "http://antlr.org/license.html", null, false));
		resources.add(new LicenseResource(new File(root, "cco.txt"), new File(root, "cco.pdf"), "CC0 1.0 Universal", "CC0-1.0", true, "http://creativecommons.org/publicdomain/zero/1.0/", null, false));
		resources.add(new LicenseResource(new File(root, "cddl-1.0.txt"), new File(root, "cddl-1.0.pdf"), "CDDL 1.0", "CDDL-1.0", true, "https://oss.oracle.com/licenses/CDDL", null, false));
		resources.add(new LicenseResource(new File(root, "cddl-1.1.txt"), new File(root, "cddl-1.1.pdf"), "CDDL 1.1", "CDDL-1.1", true, "https://oss.oracle.com/licenses/CDDL-1.1", null, false));
		resources.add(new LicenseResource(new File(root, "eclipse.txt"), new File(root, "eclipse.pdf"), "Eclipse Public License 1.0", "EPL-1.0", true, "http://www.eclipse.org/legal/epl-v10.html", null, false));
		resources.add(new LicenseResource(new File(root, "edl-1.0.txt"), new File(root, "edl-1.0.pdf"), "Eclipse Distribution License 1.0", "EclipseDistribution", false, "http://www.eclipse.org/org/documents/edl-v10.php", null, false));
		resources.add(new LicenseResource(new File(root, "eclipse2.txt"), new File(root, "eclipse2.pdf"), "Eclipse Public License 2.0", "EPL-2.0", true, "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt", null, false));
		resources.add(new LicenseResource(new File(root, "lgpl-3.0.txt"), new File(root, "lgpl-3.0.pdf"), "LGPL 3.0", "LGPL-3.0-only", true, "https://www.gnu.org/licenses/lgpl-3.0.en.html", null, false));
		resources.add(new LicenseResource(new File(root, "lgpl-2.1.txt"), new File(root, "lgpl-2.1.pdf"), "LGPL 2.1", "LGPL-2.1-only", true, "https://www.gnu.org/licenses/old-licenses/lgpl-2.1", null, false));
		resources.add(new LicenseResource(new File(root, "lgpl-2.0.txt"), new File(root, "lgpl-2.0.pdf"), "LGPL 2.0", "LGPL-2.0-only", true, "https://www.gnu.org/licenses/old-licenses/lgpl-2.0", null, false));
		resources.add(new LicenseResource(new File(root, "gwt.txt"), new File(root, "gwt.pdf"), "GWT Terms of Service", "GWT", false, "http://www.gwtproject.org/terms.html", null, false));
		resources.add(new LicenseResource(new File(root, "unicode.txt"), new File(root, "unicode.pdf"), "Unicode Terms of Use", "Unicode-TOU", true, "http://www.unicode.org/copyright.html#License", null, false));
		resources.add(new LicenseResource(new File(root, "jtidy.txt"), new File(root, "jtidy.pdf"), "JTidy License", "JTidy", false, "https://sourceforge.net/p/jtidy/code/HEAD/tree/trunk/jtidy/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "mit.txt"), new File(root, "mit.pdf"), "MIT License", "MIT", true, "https://opensource.org/licenses/mit-license.php", null, false));
		resources.add(new LicenseResource(new File(root, "mozilla-1.1.txt"), new File(root, "mozilla-1.1.pdf"), "Mozilla License 1.1", "MPL-1.1", true, "https://www.mozilla.org/en-US/MPL/1.1/", null, false));
		resources.add(new LicenseResource(new File(root, "mozilla-2.0.txt"), new File(root, "mozilla-2.0.pdf"), "Mozilla License 2.0", "MPL-2.0", true, "https://www.mozilla.org/en-US/MPL/2.0/", null, false));
		resources.add(new LicenseResource(new File(root, "geoapi.txt"), new File(root, "geoapi.pdf"), "GeoAPI License", "GeoAPI", false, "https://svn.code.sf.net/p/geoapi/code/branches/3.0.x/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "douglea.txt"), new File(root, "douglea.pdf"), "Technology License from Sun Microsystems, Inc. to Doug Lea", "SunToDougLea", false, "http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/sun-u.c.license.pdf", null, false));
		resources.add(new LicenseResource(new File(root, "jdom.txt"), new File(root, "jdom.pdf"), "JDOM License", "JDOM", false, "https://raw.githubusercontent.com/hunterhacker/jdom/master/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "jscience.txt"), new File(root, "jscience.pdf"), "JScience License", "JScience", false, "http://jscience.org/doc/license.txt", null, false));
		resources.add(new LicenseResource(new File(root, "adobe-bsd.txt"), new File(root, "adobe-bsd.pdf"), "Adobe BSD License", "AdobeBSD", false, "http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html", null, false));
		resources.add(new LicenseResource(new File(root, "json.txt"), new File(root, "json.pdf"), "JSON License", "JSON", true, "http://json.org/license.html", null, false));
		resources.add(new LicenseResource(new File(root, "w3c.txt"), new File(root, "w3c.pdf"), "W3C Software Notice and License", "W3C", true, "https://www.w3.org/Consortium/Legal/copyright-software-19980720", null, false));
		resources.add(new LicenseResource(new File(root, "unrar.txt"), new File(root, "unrar.pdf"), "UnRAR License", "Unrar", false, "https://raw.githubusercontent.com/junrar/junrar/master/license.txt", null, false));
		resources.add(new LicenseResource(new File(root, "jsoup.txt"), new File(root, "jsoup.pdf"), "JSoup License", "JSoup", false, "https://jsoup.org/license", null, false));
		resources.add(new LicenseResource(new File(root, "slf4j.txt"), new File(root, "slf4j.pdf"), "SLF4J License", "SLF4J", false, "https://www.slf4j.org/license.html", null, false));
		resources.add(new LicenseResource(new File(root, "oracle-binary.txt"), new File(root, "oracle-binary.pdf"), "Oracle Binary Code License Agreement", "OracleBinary", false, "http://www.oracle.com/technetwork/java/javase/downloads/java-se-archive-license-1382604.html", null, false));
		resources.add(new LicenseResource(new File(root, "aspose.txt"), new File(root, "aspose.pdf"), "Aspose License", "Aspose", false, "http://www.aspose.com/corporate/purchase/policies/License-Types/default.aspx", "https://confluence.braintribe.com/display/INTEGRATION/Aspose", true));
		resources.add(new LicenseResource(new File(root, "jpedal.txt"), new File(root, "jpedal.pdf"), "JPedal License", "JPedal", false, "https://www.idrsolutions.com/jpedal/pricing/", "https://confluence.braintribe.com/display/INTEGRATION/JPedal", true));
		resources.add(new LicenseResource(new File(root, "auxilii.txt"), new File(root, "auxilii.pdf"), "Auxilii License", "Auxilii", false, "https://auxilii.com/", null, true));
		resources.add(new LicenseResource(new File(root, "jai.txt"), new File(root, "jai.pdf"), "JAI Distribution License", "JAI", false, "http://www.oracle.com/technetwork/java/iio-141084.html", null, false));
		resources.add(new LicenseResource(new File(root, "extreme-pull.txt"), new File(root, "extreme-pull.pdf"), "Extreme! Lab PullParser License", "Extreme", false, "https://www.extreme.indiana.edu/xgws/xsoap/xpp/download/PullParser2/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "pivotal.txt"), new File(root, "pivotal.pdf"), "Pivotal License", "Pivotal", false, "https://www.vmware.com/be/download/eula/pivotal-cf_eula.html", null, false));
		resources.add(new LicenseResource(new File(root, "codehaus-classworlds.txt"), new File(root, "codehaus-classworlds.pdf"), "Codehaus Classworlds", "Codehaus", false, "http://classworlds.codehaus.org/ ", null, false));
		resources.add(new LicenseResource(new File(root, "jaiimageio.txt"), new File(root, "jaiimageio.pdf"), "JAI Image IO License", "JAI", false, "https://github.com/jai-imageio/jai-imageio-core/blob/master/COPYRIGHT.txt", null, false));
		resources.add(new LicenseResource(new File(root, "thai-os.txt"), new File(root, "thai-os.pdf"), "Thai Open Source Software Center", "ThaiOS", false, "https://github.com/orbeon/msv/blob/master/relaxngDatatype/copying.txt", null, false));
		resources.add(new LicenseResource(new File(root, "flexmark.txt"), new File(root, "flexmark.pdf"), "Flexmark License", "Flexmark", false, "https://github.com/vsch/flexmark-java/blob/master/LICENSE.txt", null, false));
		resources.add(new LicenseResource(new File(root, "universal-permissive-1.0.txt"), new File(root, "universal-permissive-1.0.pdf"), "Universal Permissive License 1.0", "UPL-1.0", true, "https://opensource.org/licenses/UPL", null, false));
		resources.add(new LicenseResource(new File(root, "sencha.txt"), new File(root, "sencha.pdf"), "Sencha GXT License", "Sencha", false, "https://www.sencha.com/legal/sencha-software-license-agreement/", null, true));
		resources.add(new LicenseResource(new File(root, "gpl2_with_cpexception.txt"), new File(root, "gpl2_with_cpexception.pdf"), "GPL2 w/ CPE", "GPL-2.0-with-classpath-exception", true, "http://openjdk.java.net/legal/gplv2+ce.html", null, false));
		resources.add(new LicenseResource(new File(root, "gpl-2.0.txt"), new File(root, "gpl-2.0.pdf"), "GPL2", "GPL-2.0-only", true, "https://opensource.org/licenses/GPL-2.0", null, false));
		//resources.add(new LicenseResource(new File(root, "cddl-gpl-1.1.txt"), new File(root, "cddl-gpl-1.1.pdf"), "CDDL+GPL 1.1", "CDDL-1.1", true, "https://oss.oracle.com/licenses/CDDL+GPL-1.1", null, false));
		//@formatter:on

		Set<String> alreadyInserted = loadLicensesNames();
		for (Iterator<LicenseResource> it = resources.iterator(); it.hasNext();) {
			LicenseResource lr = it.next();
			if (alreadyInserted.contains(lr.name)) {
				it.remove();
			}
		}

		log("Found " + resources.size() + " new license resources.");

	}

	private static Set<String> loadLibraryNames() {
		EntityQuery query = EntityQueryBuilder.from(Library.T).done();
		List<Library> list = librarySession.query().entities(query).list();
		Set<String> result = new HashSet<>();
		for (Library l : list) {
			String artifactId = l.getGroupId() + ":" + l.getArtifactId() + "#" + l.getVersion();
			result.add(artifactId);
		}
		return result;
	}

	private static Set<String> loadLicensesNames() {
		EntityQuery query = EntityQueryBuilder.from(DistributionLicense.T).done();
		List<DistributionLicense> list = librarySession.query().entities(query).list();
		Set<String> result = new HashSet<>();
		for (DistributionLicense l : list) {
			result.add(l.getName());
		}
		return result;
	}
}
