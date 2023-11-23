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
package com.braintribe.model.processing.library.service.spreadsheet;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.library.service.license.SpreadsheetImportReport;
import com.braintribe.model.processing.library.service.expert.DependencyExpert;
import com.braintribe.model.processing.library.service.util.Comparators;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class SpreadsheetSupport {

	private final static Logger logger = Logger.getLogger(SpreadsheetSupport.class);

	public static byte[] createExportSpreadsheet(PersistenceGmSession librarySession) throws Exception {

		Workbook wb = new XSSFWorkbook();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			libraryExportSheet(librarySession, wb);
			licenseExportSheet(librarySession, wb);

			wb.write(baos);
		} finally {
			wb.close();
		}

		return baos.toByteArray();
	}

	protected static void licenseExportSheet(PersistenceGmSession librarySession, Workbook wb) {

		TreeSet<DistributionLicense> licensesSet = getExistingLicenses(librarySession);
		Sheet sheet = wb.createSheet("License Export");

		Row row = sheet.createRow(0);
		addLicenseHeader(row);

		short rowNumber = 1;
		for (DistributionLicense license : licensesSet) {

			row = sheet.createRow(rowNumber++);

			String licenseFile = license.getLicenseFile().getName();
			String pdf = license.getLicenseFilePdf().getName();
			addLicenseExcelRow(wb, row, license.getName(), license.getUrl(), license.getCommercial(), licenseFile, pdf,
					license.getInternalDocumentationUrl(), license.getSpdxLicenseId(), license.getSpdxListedLicense());

		}

		for (int col = 0; col < 8; ++col) {
			sheet.autoSizeColumn(col);
		}
	}

	protected static void libraryExportSheet(PersistenceGmSession librarySession, Workbook wb) {

		TreeSet<Library> librarySet = getExistingArtifacts(librarySession);
		Sheet sheet = wb.createSheet("Library Export");

		Row row = sheet.createRow(0);
		addLibraryHeader(row);

		short rowNumber = 1;
		for (Library library : librarySet) {

			String artifactId = library.getGroupId() + ":" + library.getArtifactId() + "#" + library.getVersion();

			row = sheet.createRow(rowNumber++);

			String licenseIds = null;
			List<DistributionLicense> licenses = library.getLicenses();
			if (licenses != null && !licenses.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (DistributionLicense dl : licenses) {
					if (sb.length() > 0) {
						sb.append("; ");
					}
					sb.append(dl.getName());
				}
				licenseIds = sb.toString();
			}

			addLibraryExcelRow(wb, row, artifactId, library.getFilename(), licenseIds, library.getSpdxLicenseId(), library.getSpdxLicenseExpression(),
					library.getSha1(), library.getSha256(), library.getName(), library.getLibraryUrl(), library.getCopyright(),
					library.getOrganization(), null);

		}

		for (int col = 0; col < 11; ++col) {
			sheet.autoSizeColumn(col);
		}
	}

	public static void importSpreadsheet(PersistenceGmSession librarySession, byte[] spreadsheetBytes, SpreadsheetImportReport report)
			throws Exception {

		StringBuilder message = new StringBuilder();

		List<ArtifactInformation> aiList = readSpreadsheetContent(spreadsheetBytes, report, message);
		Map<String, DistributionLicense> licenseMap = getLicenses(librarySession);
		Set<String> alreadyExistingArtifacts = getExistingArtifactIds(librarySession);

		TreeSet<String> licensesFound = new TreeSet<>();
		TreeSet<String> licensesMissing = new TreeSet<>();
		TreeSet<String> alreadyImported = new TreeSet<>();
		TreeSet<String> imported = new TreeSet<>();
		TreeSet<String> notImported = new TreeSet<>();
		for (ArtifactInformation ai : aiList) {

			List<String> licenseNames = ai.getLicenseNames();
			List<DistributionLicense> referencedLicenses = new ArrayList<>();
			boolean foundAllLicenses = true;
			for (String lName : licenseNames) {
				DistributionLicense dl = licenseMap.get(lName);
				if (dl != null) {
					referencedLicenses.add(dl);
					licensesFound.add(lName);
				} else {
					licensesMissing.add(lName);
					message.append("Could not find license ");
					message.append(lName);
					message.append('\n');
					foundAllLicenses = false;
				}
			}

			if (foundAllLicenses) {

				if (alreadyExistingArtifacts.contains(ai.getArtifact())) {
					alreadyImported.add(ai.getArtifact());

				} else {

					alreadyExistingArtifacts.add(ai.getArtifact());

					try {
						Library l = librarySession.create(Library.T);
						l.setGroupId(ai.getGroupId());
						l.setArtifactId(ai.getArtifactId());
						l.setVersion(ai.getVersion());
						l.setCopyright(ai.getCopyright());
						l.setLibraryUrl(ai.getLibraryUrl());
						l.getLicenses().addAll(referencedLicenses);
						l.setOrganization(ai.getOrganization());
						l.setName(ai.getLibraryName());
						l.setSpdxLicenseId(ai.getSpdxLicenseId());
						l.setSpdxLicenseExpression(ai.getSpdxLicenseExpression());
						l.setSha1(ai.getSha1());
						l.setSha256(ai.getSha256());
						l.setFilename(ai.getFilename());
						librarySession.commit();

						imported.add(ai.getArtifact());
					} catch (Exception e) {
						message.append(e.getMessage());
						message.append('\n');
						logger.error("Could not import artifact " + ai.toString(), e);
						notImported.add(ai.getArtifact());
					}
				}
			} else {
				notImported.add(ai.getArtifact());
			}

		}

		report.getLibrariesAlreadyImported().addAll(alreadyImported);
		report.getLibrariesImported().addAll(imported);
		report.getLibrariesNotImported().addAll(notImported);
		report.getLicensesFound().addAll(licensesFound);
		report.getLicensesMissing().addAll(licensesMissing);

		boolean success = notImported.isEmpty() && licensesMissing.isEmpty();

		if (message.length() > 0) {
			report.setMessage(message.toString().trim());
		}
		report.setSuccess(success);
	}

	private static Set<String> getExistingArtifactIds(PersistenceGmSession librarySession) {
		TraversingCriterion tc = TC.create().conjunction().property()
				.typeCondition(orTc(isKind(TypeKind.entityType), isKind(TypeKind.collectionType))).negation().pattern().entity(Library.T)
				.property(Library.licenses).close().close().done();
		Set<String> result = new HashSet<>();
		EntityQuery query = EntityQueryBuilder.from(Library.T).tc(tc).done();
		List<Library> list = librarySession.query().entities(query).list();
		for (Library l : list) {
			result.add(l.getGroupId() + ":" + l.getArtifactId() + "#" + l.getVersion());
		}
		return result;
	}
	private static TreeSet<Library> getExistingArtifacts(PersistenceGmSession librarySession) {
		TraversingCriterion tc = TC.create().conjunction().property()
				.typeCondition(orTc(isKind(TypeKind.entityType), isKind(TypeKind.collectionType))).negation().pattern().entity(Library.T)
				.property(Library.licenses).close().close().done();

		EntityQuery query = EntityQueryBuilder.from(Library.T).tc(tc).done();
		List<Library> list = librarySession.query().entities(query).list();
		TreeSet<Library> set = new TreeSet<Library>(Comparators.libraryComparator());
		set.addAll(list);
		return set;
	}
	private static TreeSet<DistributionLicense> getExistingLicenses(PersistenceGmSession librarySession) {

		EntityQuery query = EntityQueryBuilder.from(DistributionLicense.T).done();
		List<DistributionLicense> list = librarySession.query().entities(query).list();
		TreeSet<DistributionLicense> set = new TreeSet<DistributionLicense>(Comparators.licenseComparator());
		set.addAll(list);
		return set;
	}

	private static Map<String, DistributionLicense> getLicenses(PersistenceGmSession librarySession) {

		Map<String, DistributionLicense> result = new HashMap<>();
		EntityQuery query = EntityQueryBuilder.from(DistributionLicense.T).done();
		List<DistributionLicense> list = librarySession.query().entities(query).list();
		for (DistributionLicense dl : list) {
			String licenseName = dl.getName();
			result.put(licenseName, dl);
		}

		return result;
	}

	private static List<ArtifactInformation> readSpreadsheetContent(byte[] spreadsheetBytes, SpreadsheetImportReport report, StringBuilder message)
			throws Exception {

		List<ArtifactInformation> aiList = new ArrayList<>();
		Workbook workbook = null;
		FileInputStream excelFile = null;
		try {

			workbook = new XSSFWorkbook(new ByteArrayInputStream(spreadsheetBytes));
			Sheet datatypeSheet = workbook.getSheetAt(0);

			int rowNumber = 0;
			for (Row row : datatypeSheet) {

				rowNumber++;
				if (rowNumber == 1) {
					continue;
				}

				ArtifactInformation ai = new ArtifactInformation();
				int col = 1;
				for (Cell cell : row) {

					String value = cell.getStringCellValue();

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

				String groupId = ai.getGroupId();
				boolean btArtifact = false;
				if (groupId != null && groupId.startsWith("com.braintribe")) {
					btArtifact = true;
				}
				if (ai.isValid() && !btArtifact) {

					aiList.add(ai);

				} else {
					if (StringTools.isEmpty(ai.getArtifact())) {
						// probably an empty line
						message.append("Row " + rowNumber + " has not been processed.\n");
					} else {
						report.getLibrariesNotImported().add(ai.getArtifact());
						message.append(ai.getArtifact() + " in row " + rowNumber + " has not been processed.\n");
					}
				}

			}
		} catch (IOException e) {
			throw new Exception("Error while reading the Excel content", e);
		} finally {
			IOTools.closeCloseable(workbook, null);
			IOTools.closeCloseable(excelFile, null);
		}

		return aiList;
	}

	public static byte[] createImportSpreadsheet(PersistenceGmSession librarySession, AbstractSet<String> dependencySet,
			Function<String, Map<String, String>> checksumProvider) throws Exception {

		Workbook wb = new XSSFWorkbook();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Sheet sheet = wb.createSheet("License Import");

			Row row = sheet.createRow(0);
			addLibraryHeader(row);

			Set<String> existing = getExistingArtifactIds(librarySession);
			short rowNumber = 1;
			for (String artifactIdString : dependencySet) {

				if (!existing.contains(artifactIdString)) {

					String groupId = StringTools.getSubstringBefore(artifactIdString, ":");
					String artifactId = StringTools.getSubstringBetween(artifactIdString, ":", "#");

					//@formatter:off
					EntityQuery query = EntityQueryBuilder.from(Library.T).where()
							.conjunction()
							.property(Library.groupId).eq(groupId)
							.property(Library.artifactId).eq(artifactId)
							.close()
							.orderBy(Library.version, OrderingDirection.descending)
							.tc(DependencyExpert.tc).done();
					//@formatter:off
					Library prevVersion = librarySession.query().entities(query).first();

					row = sheet.createRow(rowNumber++);
					addExcelRowBasedOnPreviousVersion(wb, row, artifactIdString, prevVersion, checksumProvider);
					
					existing.add(artifactIdString);
					
				} else {
					logger.debug(() -> "Artifact "+artifactIdString+" is already in the library. Not including it in the spreadsheet.");
				}
			}

			for (int col=0; col<11; ++col) {
				sheet.autoSizeColumn(col);
			}

			wb.write(baos);
		} finally {
			wb.close();
		}

		return baos.toByteArray();
	}


	private static void addLibraryHeader(Row row) {
		row.createCell(0).setCellValue("Artifact");
		row.createCell(1).setCellValue("Filename");
		row.createCell(2).setCellValue("License ID");
		row.createCell(3).setCellValue("SPDX License");
		row.createCell(4).setCellValue("SPDX Expression");
		row.createCell(5).setCellValue("SHA1");
		row.createCell(6).setCellValue("SHA256");
		row.createCell(7).setCellValue("Name");
		row.createCell(8).setCellValue("Artifact URL");
		row.createCell(9).setCellValue("Copyright");
		row.createCell(10).setCellValue("Organization");
		row.createCell(11).setCellValue("Template");
	}
	private static void addLicenseHeader(Row row) {
		row.createCell(0).setCellValue("Name");
		row.createCell(1).setCellValue("URL");
		row.createCell(2).setCellValue("Commercial");
		row.createCell(3).setCellValue("Original File");
		row.createCell(4).setCellValue("PDF Version");
		row.createCell(5).setCellValue("Internal Documentation");
		row.createCell(6).setCellValue("SPDX Id");
		row.createCell(7).setCellValue("SPDX Listed");
	}
	
	private static void addExcelRowBasedOnPreviousVersion(Workbook wb, Row row, String artifactId, Library prevVersion, Function<String, Map<String, String>> checksumProvider) {
		String licenseIds = null;
		String name = null;
		String artifactUrl = null;
		String copyright = null;
		String organization = null;
		String filename = null;
		String spdxLicenseId = null;
		String spdxLicenseExpression = null;

		int idx1 = artifactId.indexOf(':');
		int idx2 = artifactId.indexOf('#');
		String artifact = artifactId.substring(idx1+1, idx2);
		String version = artifactId.substring(idx2+1);
		filename = artifact+"-"+version+".jar";
		
		if (prevVersion != null) {
			List<DistributionLicense> licenses = prevVersion.getLicenses();
			if (licenses != null && !licenses.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (DistributionLicense dl : licenses) {
					if (sb.length() > 0) {
						sb.append("; ");
					}
					sb.append(dl.getName());
				}
				licenseIds = sb.toString();
			}
			name = prevVersion.getName();
			artifactUrl = prevVersion.getLibraryUrl();
			copyright = prevVersion.getCopyright();
			organization = prevVersion.getOrganization();
			spdxLicenseId = prevVersion.getSpdxLicenseId();
			spdxLicenseExpression = prevVersion.getSpdxLicenseExpression();
		}
		String comment = null;
		if (prevVersion != null) {
			String prevId = prevVersion.getGroupId()+":"+prevVersion.getArtifactId()+"#"+prevVersion.getVersion();
			comment = "Based on: "+prevId;
		}
		
		String sha1 = null;
		String sha256 = null;
		if (checksumProvider != null) {
			Map<String, String> map = checksumProvider.apply(artifactId);
			if (map != null) {
				sha1 = map.get("SHA1");
				sha256 = map.get("SHA256");
			}
		}
		
		addLibraryExcelRow(wb, row, artifactId, filename, licenseIds, spdxLicenseId, spdxLicenseExpression, sha1, sha256, name, artifactUrl, copyright, organization, comment);
	}
	
	private static void addLibraryExcelRow(Workbook wb, Row row, String artifactId, String filename, String licenseIds, String spdxLicenseId, String spdxLicenseExpression, String sha1, String sha256, String name, String artifactUrl, String copyright, String organization, String comment) {

		CellStyle wrapStyle = wb.createCellStyle();
		wrapStyle.setWrapText(true);

		row.createCell(0).setCellValue(artifactId);
		row.createCell(1).setCellValue(filename);
		row.createCell(2).setCellValue(licenseIds != null ? licenseIds : "x");
		row.createCell(3).setCellValue(spdxLicenseId != null ? spdxLicenseId : "x");
		row.createCell(4).setCellValue(spdxLicenseExpression != null ? spdxLicenseExpression : "x");
		row.createCell(5).setCellValue(sha1 != null ? sha1 : "x");
		row.createCell(6).setCellValue(sha256 != null ? sha256 : "x");
		row.createCell(7).setCellValue(name != null ? name : "x");
		row.createCell(8).setCellValue(artifactUrl != null ? artifactUrl : "x");
		Cell copyrightCell = row.createCell(9);
		copyrightCell.setCellStyle(wrapStyle);
		copyrightCell.setCellValue(copyright != null ? copyright : "x");
		row.createCell(10).setCellValue(organization != null ? organization : "x");
		if (!StringTools.isEmpty(comment)) {
			row.createCell(11).setCellValue(comment);
		}
	}
	
	private static void addLicenseExcelRow(Workbook wb, Row row, String name, String url, boolean commercial, String licenseFile, String pdf, String internalDocUrl, String spdxId, boolean spdxListed) {

		CellStyle wrapStyle = wb.createCellStyle();
		wrapStyle.setWrapText(true);

		row.createCell(0).setCellValue(name);
		row.createCell(1).setCellValue(url);
		row.createCell(2).setCellValue(String.valueOf(commercial));
		row.createCell(3).setCellValue(licenseFile);
		row.createCell(4).setCellValue(pdf);
		row.createCell(5).setCellValue(internalDocUrl);
		row.createCell(6).setCellValue(spdxId);
		row.createCell(7).setCellValue(""+spdxListed);
	}

}
