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
package com.braintribe.model.processing.library.service.libdoc;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.braintribe.logging.Logger;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.utils.StringTools;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;

public class LibraryDetailsGenerator {

	private final static Logger logger = Logger.getLogger(LibraryDetailsGenerator.class);

	protected static float titleFontSize = 14.0f;
	protected static float tableFontSize = 10.0f;
	protected static Color gray = new Color(125, 125, 125);

	public static InMemoryPdf createLibraryDetailPages(List<Library> libraries, LinkedHashMap<DistributionLicense, Integer> licensePageCount,
			int pageOffset, Map<Library, Integer> libraryDetailPageNumbers) throws Exception {

		PDDocument document = new PDDocument();
		PDFont font = PDType1Font.HELVETICA;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		float y = -1;
		int count = libraries.size();
		PDPage page = null;
		float pageHeight = -1;
		float pageWidth = -1;
		int pageNumber = 0;

		for (int i = 0; i < count; ++i) {

			Library library = libraries.get(i);

			if (y == -1) {

				page = new PDPage(PDRectangle.A4);
				document.addPage(page);

				PDRectangle pageSize = page.getMediaBox();
				if (pageWidth < 0)
					pageWidth = pageSize.getWidth();
				if (pageHeight < 0)
					pageHeight = pageSize.getHeight();

				y = pageHeight - 20;

				pageNumber++;
			}

			BaseTable table = createTable(document, page, library, licensePageCount, pageOffset, y, pageWidth, pageHeight, font);

			float tableHeight = table.getHeaderAndDataHeight() + 40;
			float remainingHeight = y;

			final String id = library.getGroupId() + ":" + library.getArtifactId() + "#" + library.getVersion();
			if (tableHeight > remainingHeight) {
				final float currentY = y;
				logger.debug(() -> "Creating new page for " + id + " at position " + currentY + " (tableHeight is " + tableHeight
						+ ",  remainingHeight is " + remainingHeight + ")");
				i--;
				y = -1;
			} else {
				libraryDetailPageNumbers.put(library, pageNumber);
				float draw = table.draw();
				final float currentY = y;
				logger.debug(() -> "  Drawing " + id + " at position " + currentY + " (expected height is " + tableHeight + ", remaining height was "
						+ remainingHeight + ", result is " + draw + ")");
				y -= tableHeight;
			}

		}

		// Save the results and ensure that the document is properly closed:
		document.save(baos);
		document.close();

		// IOTools.inputToFile(new ByteArrayInputStream(baos.toByteArray()), new File("/Users/roman/Downloads/test.pdf"));

		return new InMemoryPdf(baos.toByteArray(), pageNumber);
	}

	protected static BaseTable createTable(PDDocument document, PDPage page, Library library,
			LinkedHashMap<DistributionLicense, Integer> licensePageCount, int pageOffset, float y, float pageWidth, float pageHeight, PDFont font)
			throws Exception {

		BaseTable table = new BaseTable(y, pageHeight, 20, pageWidth - 20, 20, document, page, false, true);

		String message = library.getGroupId() + ":" + library.getArtifactId() + "#" + library.getVersion();

		// Create Header row
		Row<PDPage> headerRow = table.createRow(titleFontSize);
		// float stringWidth = font.getStringWidth(message)*titleFontSize/1000f;
		Cell<PDPage> cell = headerRow.createCell(100, message);
		cell.setFont(PDType1Font.HELVETICA_BOLD);
		cell.setFontSize(titleFontSize);
		cell.setTopPadding(titleFontSize);
		cell.setBottomPadding(titleFontSize);
		table.addHeaderRow(headerRow);

		Row<PDPage> row;

		row = table.createRow(10f);
		cell = row.createCell(20, "", HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
		// float keyCellWidth = cell.getWidth() - 20;
		cell = row.createCell(80, "", HorizontalAlignment.LEFT, VerticalAlignment.TOP);
		float valueCellWidth = cell.getWidth() - 20;

		addRow(table, "Name", library.getName(), font, valueCellWidth);
		addRow(table, "Group", library.getGroupId(), font, valueCellWidth);
		addRow(table, "Artifact", library.getArtifactId(), font, valueCellWidth);
		addRow(table, "Version", library.getVersion(), font, valueCellWidth);
		addRow(table, "Organization", library.getOrganization(), font, valueCellWidth);
		addRow(table, "URL", library.getOrganizationUrl(), font, valueCellWidth);
		addRow(table, "Copyright", library.getCopyright(), font, valueCellWidth);
		addRow(table, "SPDX License ID", library.getSpdxLicenseId(), font, valueCellWidth);
		addRow(table, "SPDX License Expression", library.getSpdxLicenseExpression(), font, valueCellWidth);

		addLicenses(table, library.getLicenses(), licensePageCount, pageOffset, font, valueCellWidth);

		return table;
	}

	protected static void addLicenses(BaseTable table, List<DistributionLicense> licenses,
			LinkedHashMap<DistributionLicense, Integer> licensePageCount, int pageOffset, PDFont font, float valueCellWidth) throws Exception {

		for (DistributionLicense license : licenses) {

			StringBuilder sb = new StringBuilder();
			sb.append(license.getName() + "\n");
			sb.append(license.getUrl() + "\n");
			sb.append("Page " + (licensePageCount.get(license) + pageOffset));
			addRow(table, "License", sb.toString(), font, valueCellWidth);

		}

	}

	protected static void addRow(BaseTable table, String key, String value, PDFont font, float valueCellWidth) throws Exception {
		Row<PDPage> row;
		Cell<PDPage> cell;

		if (value == null) {
			value = "";
		}
		/* StringBuilder b = new StringBuilder(); for (int i = 0; i < value.length(); i++) { if (WinAnsiEncoding.INSTANCE.contains(value.charAt(i))) {
		 * b.append(value.charAt(i)); } } value = b.toString(); */
		List<String> lines = StringTools.getLines(value);
		lines = splitLines(lines, font, valueCellWidth);

		boolean firstLine = true;
		for (String line : lines) {
			row = table.createRow(10f);
			if (firstLine) {
				cell = row.createCell(20, key, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
				cell.setFont(PDType1Font.HELVETICA);
				cell.setFontSize(tableFontSize);
				cell.setTextColor(gray);
				firstLine = false;
			} else {
				cell = row.createCell(20, "", HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
			}

			cell = row.createCell(80, line, HorizontalAlignment.LEFT, VerticalAlignment.TOP);

			float stringWidth = font.getStringWidth(line) * tableFontSize / 1000f;
			float cellWidth = cell.getWidth();
			if (cellWidth < stringWidth) {
				System.out.println("Value: " + line + ", cell width:" + cellWidth + ", textWidth: " + stringWidth);
			}

			cell.setFont(PDType1Font.HELVETICA);
			cell.setFontSize(tableFontSize);
			cell.setTextColor(Color.BLACK);
		}

	}

	protected static List<String> splitLines(List<String> lines, PDFont font, float width) throws Exception {
		List<String> result = new ArrayList<String>();
		for (String line : lines) {
			List<String> part = splitLines(line, font, width);
			result.addAll(part);
		}
		return result;
	}

	protected static List<String> splitLines(String text, PDFont font, float width) throws Exception {
		List<String> lines = new ArrayList<String>();
		int lastSpace = -1;
		while (text.length() > 0) {
			int spaceIndex = text.indexOf(' ', lastSpace + 1);
			if (spaceIndex < 0)
				spaceIndex = text.length();
			String subString = text.substring(0, spaceIndex);
			float size = tableFontSize * font.getStringWidth(subString) / 1000;

			if (size > width) {
				if (lastSpace < 0)
					lastSpace = spaceIndex;
				subString = text.substring(0, lastSpace);
				lines.add(subString);
				text = text.substring(lastSpace).trim();
				lastSpace = -1;
			} else if (spaceIndex == text.length()) {
				lines.add(text);
				text = "";
			} else {
				lastSpace = spaceIndex;
			}
		}

		return lines;
	}

}
