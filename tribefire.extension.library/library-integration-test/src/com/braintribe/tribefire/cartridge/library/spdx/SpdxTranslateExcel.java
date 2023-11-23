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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.tribefire.cartridge.library.load.ArtifactInformation;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class SpdxTranslateExcel {

	static DefaultHttpClientProvider clientProvider = new DefaultHttpClientProvider();
	static CloseableHttpClient client = null;

	public static void main(String[] args) throws Exception {
		client = clientProvider.provideHttpClient();

		File file = new File("res/outdated-library-list-old.xlsx");
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
							ai.setLicenseName(value);
							break;
						case 3:
							ai.setLibraryName(value);
							break;
						case 4:
							ai.setLibraryUrl(value);
							break;
						case 5:
							ai.setCopyright(value);
							break;
						case 6:
							ai.setOrganization(value);
							break;
						default:
							break;
					}

					col++;
				}

				if (ai.getGroupId() != null) {
					Map<String, String> checksumMap = getChecksumAndName(ai);
					ai.setSha1(checksumMap.get("SHA1"));
					ai.setSha256(checksumMap.get("SHA256"));
					ai.setFilename(checksumMap.get("name"));
				}

				if (ai.isValid()) {
					aiList.add(ai);
				} else {
					System.out.println("Invalid: " + ai);
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
		Map<String, String> map = new HashMap<>();
		addMapping(map);
		byte[] excel = writeNewExcel(aiList, map);
		IOTools.inputToFile(new ByteArrayInputStream(excel),
				new File("/Users/roman/Development/tribefire/tribefire.extension.library/library-integration-test/res/outdated-library-list.xlsx"));
	}

	private static byte[] writeNewExcel(List<ArtifactInformation> aiList, Map<String, String> map) throws Exception {
		Workbook wb = new XSSFWorkbook();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Sheet sheet = wb.createSheet("License Import");

			Row row = sheet.createRow(0);
			addLibraryHeader(row);

			short rowNumber = 1;
			for (ArtifactInformation ai : aiList) {

				row = sheet.createRow(rowNumber++);
				addLibraryExcelRow(wb, row, ai, map);

			}

			for (int col = 0; col < 11; ++col) {
				sheet.autoSizeColumn(col);
			}

			wb.write(baos);
		} finally {
			wb.close();
		}

		return baos.toByteArray();
	}

	private static void addLibraryExcelRow(Workbook wb, Row row, ArtifactInformation ai, Map<String, String> map) {

		CellStyle wrapStyle = wb.createCellStyle();
		wrapStyle.setWrapText(true);

		row.createCell(0).setCellValue(ai.getArtifact());
		row.createCell(1).setCellValue(ai.getFilename());
		row.createCell(2).setCellValue(StringTools.join(";", ai.getLicenseNames()));

		if (ai.getLicenseNames().size() == 1) {
			String licenseName = ai.getLicenseNames().get(0);
			if (licenseName.equals("CDDL+GPL 1.1")) {
				row.createCell(2).setCellValue("CDDL 1.1");
				row.createCell(3).setCellValue("CDDL-1.1");
				row.createCell(4).setCellValue("(CDDL-1.1 OR GPL-2.0-only)");
			} else {
				String spdxLicense = map.get(licenseName);
				row.createCell(3).setCellValue(spdxLicense);
				row.createCell(4).setCellValue(spdxLicense);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < ai.getLicenseNames().size(); ++i) {
				if (i > 0) {
					sb.append(" OR ");
				}
				String oldLicense = ai.getLicenseNames().get(i);
				String spdxLicense = map.get(oldLicense);
				sb.append(spdxLicense);
			}
			sb.append(")");
			String expr = sb.toString();
			String spdxLicense;
			if (expr.contains("Apache-2.0")) {
				spdxLicense = "Apache-2.0";
			} else if (expr.contains("LGPL-2.1-only")) {
				spdxLicense = "LGPL-2.1-only";
			} else if (expr.contains("CC0-1.0")) {
				spdxLicense = "CC0-1.0";
			} else if (expr.contains("EPL-1.0")) {
				spdxLicense = "EPL-1.0";
			} else if (expr.contains("CDDL-1.1")) {
				spdxLicense = "CDDL-1.1";
			} else if (expr.contains("EPL-1.0")) {
				spdxLicense = "EPL-1.0";
			} else {
				System.err.println("What to do: " + expr);
				spdxLicense = null;
			}

			row.createCell(3).setCellValue(spdxLicense);
			row.createCell(4).setCellValue(expr);
		}

		row.createCell(5).setCellValue(ai.getSha1() != null ? ai.getSha1() : "");
		row.createCell(6).setCellValue(ai.getSha256() != null ? ai.getSha256() : "");

		row.createCell(7).setCellValue(ai.getLibraryName());
		row.createCell(8).setCellValue(ai.getLibraryUrl());
		Cell copyrightCell = row.createCell(9);
		copyrightCell.setCellStyle(wrapStyle);
		copyrightCell.setCellValue(ai.getCopyright());
		row.createCell(10).setCellValue(ai.getOrganization());
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
	}

	/* Absolutely useless. The result of the scancode-toolkit returns either "unknown-license-reference" or multiple
	 * licenses */
	private static void readJson() throws Exception {

		JSONParser parser = new JSONParser();
		try (Reader reader = new InputStreamReader(new FileInputStream("/Users/roman/Downloads/SPDX/scancode-toolkit/output.json"),
				StandardCharsets.UTF_8)) {
			JSONObject root = (JSONObject) parser.parse(reader);
			JSONArray array = (JSONArray) root.get("files");

			if (array.size() > 0) {
				for (int i = 0; i < array.size(); ++i) {
					JSONObject entry = (JSONObject) array.get(i);

					String type = (String) entry.get("type");
					if (type != null && type.equals("file")) {
						String path = (String) entry.get("path");
						System.out.println("File: " + path);
						JSONArray lE = (JSONArray) entry.get("license_expressions");
						if (lE != null) {
							for (int j = 0; j < lE.size(); ++j) {
								String e = (String) lE.get(j);
								System.out.println("\t" + e);
							}
						}

					}

				}
			}
		}

		System.exit(0);
	}

	private static Map<String, String> getChecksumAndName(ArtifactInformation ai) throws Exception {

		String group = ai.getGroupId().replace(".", "/");
		String artifact = ai.getArtifactId();
		String version = ai.getVersion();

		Map<String, String> result = new HashMap<>();

		String url = "https://<user>:<pwd>@artifactory.EXAMPLE.com/artifactory/api/storage/third-party/" + group + "/" + artifact + "/" + version
				+ "/" + artifact + "-" + version + ".jar";
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
		url = "https://<user>:<pwd>@artifactory.EXAMPLE.com/artifactory/api/storage/third-party/" + group + "/" + artifact + "/" + version + "/"
				+ artifact + "-" + version + ".pom";
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
				System.err.println(ai.getArtifact());
			}
			HttpTools.consumeResponse(response);
		}

		return result;
	}

	private static void addMapping(Map<String, String> map) {
		addLicenceMapping(map, "netCDF License", "NetCDF", true);
		addLicenceMapping(map, "Apache License, Version 1.1", "Apache-1.1", true);
		addLicenceMapping(map, "Apache License, Version 2.0", "Apache-2.0", true);
		addLicenceMapping(map, "Bouncy Castle License", "BouncyCastle", false);
		addLicenceMapping(map, "BSD License", "BSD-1-Clause", true);
		addLicenceMapping(map, "2-Clause BSD License", "BSD-2-Clause", true);
		addLicenceMapping(map, "3-Clause BSD License", "BSD-3-Clause", true);
		addLicenceMapping(map, "ObjectWeb Project License", "ObjectWeb", false);
		addLicenceMapping(map, "ThreeTen Project License", "ThreeTen", false);
		addLicenceMapping(map, "JWNL Project License", "JWNL", false);
		addLicenceMapping(map, "ANTLR 4 License", "ANTLR-PD", true);
		addLicenceMapping(map, "CC0 1.0 Universal", "CC0-1.0", true);
		addLicenceMapping(map, "CDDL 1.0", "CDDL-1.0", true);
		addLicenceMapping(map, "CDDL 1.1", "CDDL-1.1", true);
		addLicenceMapping(map, "Eclipse Public License 1.0", "EPL-1.0", true);
		addLicenceMapping(map, "Eclipse Distribution License 1.0", "EclipseDistribution", false);
		addLicenceMapping(map, "Eclipse Public License 2.0", "EPL-2.0", true);
		addLicenceMapping(map, "LGPL 3.0", "LGPL-3.0-only", true);
		addLicenceMapping(map, "LGPL 2.1", "LGPL-2.1-only", true);
		addLicenceMapping(map, "LGPL 2.0", "LGPL-2.0-only", true);
		addLicenceMapping(map, "GWT Terms of Service", "GWT", false);
		addLicenceMapping(map, "Unicode Terms of Use", "Unicode-TOU", true);
		addLicenceMapping(map, "JTidy License", "JTidy", false);
		addLicenceMapping(map, "MIT License", "MIT", true);
		addLicenceMapping(map, "Mozilla License 1.1", "MPL-1.1", true);
		addLicenceMapping(map, "Mozilla License 2.0", "MPL-2.0", true);
		addLicenceMapping(map, "GeoAPI License", "GeoAPI", false);
		addLicenceMapping(map, "Technology License from Sun Microsystems, Inc. to Doug Lea", "SunToDougLea", false);
		addLicenceMapping(map, "JDOM License", "JDOM", false);
		addLicenceMapping(map, "JScience License", "JScience", false);
		addLicenceMapping(map, "Adobe BSD License", "AdobeBSD", false);
		addLicenceMapping(map, "JSON License", "JSON", true);
		addLicenceMapping(map, "W3C Software Notice and License", "W3C", true);
		addLicenceMapping(map, "UnRAR License", "Unrar", false);
		addLicenceMapping(map, "JSoup License", "JSoup", false);
		addLicenceMapping(map, "SLF4J License", "SLF4J", false);
		addLicenceMapping(map, "Oracle Binary Code License Agreement", "OracleBinary", false);
		addLicenceMapping(map, "Aspose License", "Aspose", false);
		addLicenceMapping(map, "JPedal License", "JPedal", false);
		addLicenceMapping(map, "Auxilii License", "Auxilii", false);
		addLicenceMapping(map, "JAI Distribution License", "JAI", false);
		addLicenceMapping(map, "Extreme! Lab PullParser License", "Extreme", false);
		addLicenceMapping(map, "Pivotal License", "Pivotal", false);
		addLicenceMapping(map, "Codehaus Classworlds", "Codehaus", false);
		addLicenceMapping(map, "JAI Image IO License", "JAI", false);
		addLicenceMapping(map, "Thai Open Source Software Center", "ThaiOS", false);
		addLicenceMapping(map, "Flexmark License", "Flexmark", false);
		addLicenceMapping(map, "Universal Permissive License 1.0", "UPL-1.0", true);
		addLicenceMapping(map, "Sencha GXT License", "Sencha", false);
		addLicenceMapping(map, "GPL2 w/ CPE", "GPL-2.0-only WITH Classpath-exception-2.0", true);
		addLicenceMapping(map, "CDDL+GPL 1.1", "CDDL-1.1", true);
		addLicenceMapping(map, "GPL2", "GPL-2.0-only", true);
	}

	private static void addLicenceMapping(Map<String, String> map, String oldId, String spdxId, boolean isListed) {
		if (isListed) {
			map.put(oldId, spdxId);
		} else {
			map.put(oldId, "LicenseRef-" + spdxId);
		}
	}
}
