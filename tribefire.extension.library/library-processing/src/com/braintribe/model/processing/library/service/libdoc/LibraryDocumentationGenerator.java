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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.library.service.documentation.DocumentLibraries;
import com.braintribe.model.processing.library.service.util.PdfTools;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.html.HtmlTools;
import com.braintribe.utils.velocity.VelocityTools;

public class LibraryDocumentationGenerator implements LifecycleAware {

	private final static Logger logger = Logger.getLogger(LibraryDocumentationGenerator.class);

	private static final String libraryDocumentationTemplateLocation = "com/braintribe/model/processing/library/service/templates/libraryDocumentation.html.vm";
	private static final String backgroundLocation = "com/braintribe/model/processing/library/service/templates/background.png";

	private static final DateTimeFormatter coverDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.US);

	private File background = null;
	private Object backgroundLockObject = new Object();

	private Map<String, Template> templateMap = new HashMap<>();
	private VelocityEngine engine = null;

	private PdfTools pdfTools;
	private ExecutorService executor;

	public File generateLibraryDocumentation(PersistenceGmSession librarySession, DocumentLibraries request, List<Library> libraries)
			throws Exception {

		File mergedLicensesFile = null;
		File mergedIndexWithDetails = null;

		try {
			// Structure:
			// Index of all libraries (names, artifactId, page)
			// Index of all licenses (name, page)
			// Details of libraries
			// Table of all data (page to license)
			// Licenses

			Future<InMemoryPdf> coverPageFuture = executor.submit(() -> createCoverPage(request));

			LinkedHashMap<DistributionLicense, Integer> licensePageCount = new LinkedHashMap<>();
			mergedLicensesFile = mergeLicenses(librarySession, libraries, licensePageCount);

			// Create license index
			InMemoryPdf licenseIndexPdf = createLicenseIndex(licensePageCount, 0);
			int licenseIndexPageCount = licenseIndexPdf.getSize();

			// Create library detail pages
			Map<Library, Integer> libraryDetailPageNumbers = new HashMap<>();
			InMemoryPdf libraryDetailPdf = createLibraryDetailPages(libraries, licensePageCount, 0, libraryDetailPageNumbers);
			int libraryDetailPageCount = libraryDetailPdf.getSize();

			// Create library index
			InMemoryPdf libraryIndexPdf = createLibraryIndex(libraries, 0, libraryDetailPageNumbers);
			int libraryIndexPageCount = libraryIndexPdf.getSize();

			InMemoryPdf coverPage = coverPageFuture.get(); // createCoverPage(request);
			int coverPageSize = coverPage.getSize();

			// Redo index and overview files with corrected page numbers
			libraryIndexPdf = createLibraryIndex(libraries, coverPageSize + libraryIndexPageCount + licenseIndexPageCount, libraryDetailPageNumbers);
			licenseIndexPdf = createLicenseIndex(licensePageCount,
					coverPageSize + libraryIndexPageCount + licenseIndexPageCount + libraryDetailPageCount);
			libraryDetailPdf = createLibraryDetailPages(libraries, licensePageCount,
					coverPageSize + libraryIndexPageCount + licenseIndexPageCount + libraryDetailPageCount, libraryDetailPageNumbers);

			// Merge index with PDF
			mergedIndexWithDetails = mergeIndexWithLibraries(mergedLicensesFile, coverPage, libraryIndexPdf, licenseIndexPdf, libraryDetailPdf);

			// Add page numbers
			File finalResult = addPageNumbers(mergedIndexWithDetails);
			return finalResult;
		} finally {
			FileTools.deleteFileSilently(mergedLicensesFile);
			FileTools.deleteFileSilently(mergedIndexWithDetails);
		}
	}

	private static File addPageNumbers(File mergedIndexWithDetails) throws Exception {

		File result = null;
		try {
			result = File.createTempFile("licenses-documentation-with-pages", ".pdf");
			FileTools.deleteFileWhenOrphaned(result);
		} catch (Exception e) {
			throw new Exception("Could not create a temporary file", e);
		}

		PDDocument doc = null;
		OutputStream os = null;
		try {
			doc = PDDocument.load(mergedIndexWithDetails);

			PDPageTree pages = doc.getDocumentCatalog().getPages();
			PDFont font = PDType1Font.HELVETICA;
			float fontSize = 10.0f;
			int pageCount = pages.getCount();
			for (int i = 0; i < pageCount; i++) {

				String message = "" + (i + 1) + "/" + pageCount;
				PDPage page = pages.get(i);
				PDRectangle pageSize = page.getMediaBox();
				float stringWidth = font.getStringWidth(message) * fontSize / 1000f;
				float height = (font.getFontDescriptor().getCapHeight()) / 1000f * fontSize;

				float pageWidth = pageSize.getWidth();
				// float pageHeight = pageSize.getHeight();
				// double centeredXPosition = (pageWidth - stringWidth)/2f;
				// double centeredYPosition = pageHeight/2f;
				double xPosition = 0;
				double yPosition = 0;
				double padding = height;

				xPosition = pageWidth - stringWidth - padding;
				yPosition = height + padding;
				// append the content to the existing stream
				PDPageContentStream contentStreamPage = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
				contentStreamPage.beginText();
				// set font and font size
				contentStreamPage.setFont(font, fontSize);
				// set text color to red
				contentStreamPage.setNonStrokingColor(45, 45, 45);
				contentStreamPage.setTextMatrix(Matrix.getTranslateInstance((float) xPosition, (float) yPosition));

				contentStreamPage.showText(message);
				contentStreamPage.endText();
				contentStreamPage.close();

				GregorianCalendar cal = new GregorianCalendar();
				message = "Copyright (c) " + cal.get(Calendar.YEAR) + " Braintribe Technology GmbH, Vienna, Austria";
				stringWidth = font.getStringWidth(message) * fontSize / 1000f;
				xPosition = padding;
				yPosition = height + padding;
				PDPageContentStream contentStreamCopyright = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
				contentStreamCopyright.beginText();
				// set font and font size
				contentStreamCopyright.setFont(font, fontSize);
				// set text color to red
				contentStreamCopyright.setNonStrokingColor(45, 45, 45);
				contentStreamCopyright.setTextMatrix(Matrix.getTranslateInstance((float) xPosition, (float) yPosition));

				contentStreamCopyright.showText(message);
				contentStreamCopyright.endText();
				contentStreamCopyright.close();

			}

			os = new FileOutputStream(result);
			doc.save(os);
		} catch (Exception e) {
			throw new Exception("Could not add line numbers to PDF.", e);
		} finally {
			if (doc != null) {
				try {
					doc.close();
				} catch (Exception e) {
					logger.error("Error while trying to close document.", e);
				}
			}
		}

		return result;
	}

	private static InMemoryPdf createLibraryDetailPages(List<Library> libraries, LinkedHashMap<DistributionLicense, Integer> licensePageCount,
			int pageOffset, Map<Library, Integer> libraryDetailPageNumbers) throws Exception {
		return LibraryDetailsGenerator.createLibraryDetailPages(libraries, licensePageCount, pageOffset, libraryDetailPageNumbers);
	}

	private static File mergeLicenses(PersistenceGmSession librarySession, List<Library> libraries,
			LinkedHashMap<DistributionLicense, Integer> licensePageCount) throws Exception {

		File mergedFile = null;
		try {
			mergedFile = File.createTempFile("merged-licenses", ".pdf");
			FileTools.deleteFileWhenOrphaned(mergedFile);
		} catch (Exception e) {
			throw new Exception("Could not create a temporary file", e);
		}

		PDFMergerUtility merger = new PDFMergerUtility();

		TreeSet<DistributionLicense> licenses = getUniqueLicensesOfLibraries(libraries);
		List<File> tempFiles = new ArrayList<>();
		try {
			int currentPage = 1;

			for (DistributionLicense l : licenses) {

				int pageCount;
				try {
					pageCount = getPageCount(librarySession, l.getLicenseFilePdf());
				} catch (Exception e) {
					throw new Exception("Could not compute the page count of license file " + l.getName(), e);
				}

				licensePageCount.put(l, currentPage);
				currentPage += pageCount;

				try {
					try (InputStream in = librarySession.resources().retrieve(l.getLicenseFilePdf()).stream()) {
						File tempFile = File.createTempFile(l.getName(), ".pdf");
						tempFiles.add(tempFile);
						IOTools.inputToFile(in, tempFile);
						merger.addSource(tempFile);
					}

				} catch (Exception e) {
					throw new Exception("Could not open stream for resource " + l.getLicenseFilePdf().getId(), e);
				}
			}

			try (OutputStream os = new FileOutputStream(mergedFile)) {
				merger.setDestinationStream(os);
				merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
			}

		} catch (Exception e) {
			throw new Exception("Error while trying to merge PDF resources.", e);
		} finally {
			for (File f : tempFiles) {
				FileTools.deleteFileSilently(f);
			}
		}

		return mergedFile;

	}

	private static TreeSet<DistributionLicense> getUniqueLicensesOfLibraries(List<Library> libraries) {
		TreeSet<DistributionLicense> licenseSet = new TreeSet<DistributionLicense>(new Comparator<DistributionLicense>() {
			@Override
			public int compare(DistributionLicense o1, DistributionLicense o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});

		for (Library l : libraries) {
			licenseSet.addAll(l.getLicenses());
		}

		return licenseSet;
	}

	private static File mergeIndexWithLibraries(File mergedLicensesFile, InMemoryPdf... additionalPdfs) throws Exception {
		File result = null;
		try {
			result = File.createTempFile("licenses-documentation", ".pdf");
			FileTools.deleteFileWhenOrphaned(result);
		} catch (Exception e) {
			throw new Exception("Could not create a temporary file", e);
		}

		PDFMergerUtility merger = new PDFMergerUtility();
		List<InputStream> inStreams = new ArrayList<>();
		OutputStream os = null;
		try {

			for (InMemoryPdf in : additionalPdfs) {
				InputStream mergedIn = new ByteArrayInputStream(in.getContent());
				inStreams.add(mergedIn);
				merger.addSource(mergedIn);
			}

			InputStream indexIn = new FileInputStream(mergedLicensesFile);
			inStreams.add(indexIn);
			merger.addSource(indexIn);

			os = new FileOutputStream(result);
			merger.setDestinationStream(os);
			merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

		} catch (Exception e) {
			throw new Exception("Error while trying to merge PDF resources.", e);
		} finally {
			for (InputStream in : inStreams) {
				IOTools.closeCloseable(in, logger);
			}
			IOTools.closeCloseable(os, logger);
		}

		return result;
	}

	private static InMemoryPdf createLicenseIndex(LinkedHashMap<DistributionLicense, Integer> pageInformation, int pageOffset) throws Exception {

		LinkedHashMap<String, Integer> indexMap = new LinkedHashMap<>();
		for (Map.Entry<DistributionLicense, Integer> entry : pageInformation.entrySet()) {
			DistributionLicense license = entry.getKey();
			String id = license.getName();
			Integer pageNumber = pageOffset + entry.getValue();
			indexMap.put(id, pageNumber);
		}
		return createIndexPages("Licenses", indexMap);
	}

	private InMemoryPdf createCoverPage(DocumentLibraries request) throws Exception {

		String title = request.getTitle();
		if (StringTools.isEmpty(title)) {
			title = "tribefire";
		}
		String timestamp = request.getTimestamp();
		if (StringTools.isEmpty(timestamp)) {
			timestamp = DateTools.encode(new Date(), coverDateTimeFormat);
		}

		String background = ensureBackground();

		VelocityContext context = new VelocityContext();
		context.put("current_year", new GregorianCalendar().get(Calendar.YEAR));
		context.put("tools", HtmlTools.class);
		context.put("timestamp", timestamp);
		context.put("title", title);
		context.put("background", background);
		Template template = templateMap.get(libraryDocumentationTemplateLocation);

		StringWriter sw = new StringWriter();
		template.merge(context, sw);

		byte[] content = pdfTools.createPdfFromHtml(sw.toString(), false);
		return new InMemoryPdf(content, 1);
	}

	private String ensureBackground() {
		if (background == null || !background.exists()) {
			synchronized (backgroundLockObject) {
				if (background == null || !background.exists()) {
					try {
						background = File.createTempFile("background", ".png");
					} catch (Exception e) {
						logger.error("Could not create a temporary file for the background image.", e);
						background = new File("background.png");
					}
					background.delete();
					try (InputStream in = LibraryDocumentationGenerator.class.getClassLoader().getResourceAsStream(backgroundLocation)) {
						IOTools.inputToFile(in, background);
					} catch (Exception e) {
						logger.error("Could not create a local copy of the background image.", e);
					}
				}
			}
		}
		try {
			return background.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			logger.error("Could not create a URL from " + background.getAbsolutePath(), e);
			return "";
		}
	}

	private static InMemoryPdf createLibraryIndex(List<Library> libraries, int pageOffset, Map<Library, Integer> libraryDetailPageNumbers)
			throws Exception {

		LinkedHashMap<String, Integer> indexMap = new LinkedHashMap<>();
		for (Library l : libraries) {
			String id = l.getGroupId() + ":" + l.getArtifactId() + "#" + l.getVersion();
			indexMap.put(id, libraryDetailPageNumbers.get(l) + pageOffset);
		}
		return createIndexPages("Libraries", indexMap);
	}

	private static InMemoryPdf createIndexPages(String title, LinkedHashMap<String, Integer> indexMap) throws Exception {
		return IndexGenerator.createIndexPages(title, indexMap);
	}

	private static int getPageCount(PersistenceGmSession librarySession, Resource pdfResource) throws Exception {
		PDDocument doc = null;
		try (InputStream in = librarySession.resources().retrieve(pdfResource).stream()) {
			doc = PDDocument.load(in);
			int count = doc.getDocumentCatalog().getPages().getCount();
			return count;
		} catch (Exception e) {
			throw new Exception("Could not get the page count from resource " + pdfResource.getName(), e);
		} finally {
			if (doc != null) {
				try {
					doc.close();
				} catch (Exception e) {
					logger.error("Could not close PDF document", e);
				}
			}
		}
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	@Override
	public void postConstruct() {
		ClassLoader moduleClassLoader = LibraryDocumentationGenerator.class.getClassLoader();
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(moduleClassLoader);

			engine = VelocityTools.newResourceLoaderVelocityEngine(true);

			Template template = engine.getTemplate(libraryDocumentationTemplateLocation, "UTF-8");
			templateMap.put(libraryDocumentationTemplateLocation, template);

		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
	@Override
	public void preDestroy() {
		if (background != null) {
			try {
				background.delete();
			} catch (Exception e) {
				logger.info("Could not delete temporary background file " + background.getAbsolutePath(), e);
			}
		}
	}

	@Required
	@Configurable
	public void setPdfTools(PdfTools pdfTools) {
		this.pdfTools = pdfTools;
	}

}
