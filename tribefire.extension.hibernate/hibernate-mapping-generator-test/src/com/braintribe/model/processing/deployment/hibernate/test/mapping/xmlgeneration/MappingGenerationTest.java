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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlgeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGeneratingService;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.HbmXmlGeneratingServiceException;
import com.braintribe.model.processing.deployment.hibernate.test.metamodel.MetaModelProvider;
import com.braintribe.testing.category.Slow;
import com.braintribe.utils.IOTools;

/**
 * Tests hibernate mapping generation for {@code HibernateMappingGeneratorTestModel} entities.
 * 
 */
public class MappingGenerationTest {

	public enum XmlComparisonMode {
		xmlDiff,
		custom
	}

	@Rule
	public TemporaryFolder outputFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private static final XmlComparisonMode xmlComparisonMode = XmlComparisonMode.custom;
	private static final boolean isXmlComparisonEnabled = true;
	private static final boolean abortOnXmlComparisonFailure = false;

	private static final String expectedPath = "/test/expected";

	private static final String tablePrefix = "TEST_";
	private static final boolean allUppercase = true;
	private static final String typeHintsJsonFile = "/test/models/HibernateMappingGeneratorTestModelHints.json";
	private static final String metaDataHintsJsonFile = "/test/models/HibernateMappingGeneratorTestModelMetaDataHints.json";

	private static GmMetaModel skeletonMetaModel;
	private static GmMetaModel enrichedMetaModel;

	private static final boolean updateMode = false;
	private static final String updateBase = System.getenv("BT__ARTIFACTS_HOME")
			+ "/com/braintribe/model/processing/deployment/HibernateMappingGeneratorTest/1.3/src";

	public static void main(String[] args) throws Exception {
		runTestSuite();
	}

	public static void runTestSuite() throws Exception {
		MappingGenerationTest generator = new MappingGenerationTest();
		setup();
		generator.testFromEnrichedWithJsonHints();
	}

	@BeforeClass
	public static void setup() throws Exception {
		skeletonMetaModel = MetaModelProvider.provideModel();
		enrichedMetaModel = MetaModelProvider.provideEnrichedModel();
	}

	@Before
	public void cleanOutputFolder() throws Exception {
		cleanCommonOutput();
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromSkeleton() throws Exception {

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModel", 42);
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromSkeletonWithDecisions() throws Exception {

		File hints = new File(tempFolder.getRoot(), "skeleton.out.json");

		// 1. generate the hints output file
		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsOutputFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModel", 42);
		cleanCommonOutput();

		// 2. generate using the hints output file as hints input
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModel", 42);
		cleanCommonOutput();

		// 3. generate using the hints output file as hints input after altering properties order
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(MetaModelProvider.provideShuffledModel());
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModel", 42);
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromSkeletonWithJsonHints() throws Exception {

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(getPathFromClasspath(typeHintsJsonFile).toFile());
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModelWithHints", 42);
	}

	@Test(expected = HbmXmlGeneratingServiceException.class)
	public void testFromSkeletonWithInvalidHintsFile() throws Exception {

		File hints = new File(tempFolder.getRoot(), "inexistentFile.txt");

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

	}

	@Test(expected = HbmXmlGeneratingServiceException.class)
	public void testFromSkeletonWithInvalidJsonHints() throws Exception {

		String hints = "not a valid JSON at all";

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHints(hints);
		generatorService.renderMappings();

	}

	@Test(expected = HbmXmlGeneratingServiceException.class)
	public void testFromSkeletonWithUnexpectedJsonFormatHints() throws Exception {

		String hints = "{ \"valid\": false }"; // A valid JSON, but unexpected format

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHints(hints);
		generatorService.renderMappings();

	}

	@Test
	public void testFromSkeletonWithJsonMetaDataHints() throws Exception {

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(getPathFromClasspath(metaDataHintsJsonFile).toFile());
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModelWithMetaDataHints", 42);
	}

	@Category(Slow.class)
	@Test
	public void testFromSkeletonWithJsonHintsAndDecisions() throws Exception {

		File hints = new File(tempFolder.getRoot(), "skeletonWithJsonHints.out.json");

		// 1. generate the hints output file
		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(getPathFromClasspath(typeHintsJsonFile).toFile());
		generatorService.setTypeHintsOutputFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModelWithHints", 42);
		cleanCommonOutput();

		// 2. generate using the hints output file as hints input
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(skeletonMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModelWithHints", 42);
		cleanCommonOutput();

		// 3. generate using the hints output file as hints input after altering properties order
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(MetaModelProvider.provideShuffledModel());
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testSkeletonMetaModelWithHints", 42);
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromEnriched() throws Exception {

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModel", 40);
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromEnrichedWithDecisions() throws Exception {

		File hints = new File(tempFolder.getRoot(), "enriched.out.json");

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsOutputFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModel", 40);
		cleanCommonOutput();

		// 2. generate using the hints output file as hints input
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModel", 40);
		cleanCommonOutput();

		// 3. generate using the hints output file as hints input after altering properties order
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(MetaModelProvider.provideShuffledEnrichedModel());
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModel", 40);
	}

	// @Category(Slow.class) //set to slow in case XmlComparisonMode.xmlDiff is used
	@Test
	public void testFromEnrichedWithJsonHints() throws Exception {

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(getPathFromClasspath(typeHintsJsonFile).toFile());
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModelWithHints", 40);
	}

	@Category(Slow.class)
	@Test
	public void testFromEnrichedWithJsonHintsAndDecisions() throws Exception {

		File hints = new File(tempFolder.getRoot(), "enrichedWithJsonHints.out.json");

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(getPathFromClasspath(typeHintsJsonFile).toFile());
		generatorService.setTypeHintsOutputFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModelWithHints", 40);
		cleanCommonOutput();

		// 2. generate using the hints output file as hints input
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(enrichedMetaModel);
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModelWithHints", 40);
		cleanCommonOutput();

		// 3. generate using the hints output file as hints input after altering properties order
		generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(MetaModelProvider.provideShuffledEnrichedModel());
		generatorService.setOutputFolder(getCommonOutput());
		// generatorService.setTablePrefix(tablePrefix);
		generatorService.setAllUppercase(allUppercase);
		generatorService.setTypeHintsFile(hints);
		generatorService.renderMappings();

		compareMappings("testEnrichedMetaModelWithHints", 40);
	}

	protected void compareMappings(final Path outputPath, final String expectedPathFolder, int expectedItemCount) throws Exception {

		if (!isXmlComparisonEnabled) {
			return;
		}

		System.out.println("Comparing mappings generated in: " + outputPath.toFile());

		int outputItemCount = (outputPath.toFile().list() != null) ? outputPath.toFile().list().length : -1;

		Assert.assertEquals(expectedItemCount + " maping files are expected, but " + outputItemCount + " were generated", expectedItemCount,
				outputItemCount);

		final List<String> failures = new ArrayList<String>(outputItemCount);

		if (xmlComparisonMode == XmlComparisonMode.xmlDiff) {
			XMLUnit.setIgnoreComments(Boolean.TRUE);
			XMLUnit.setIgnoreAttributeOrder(true);
			XMLUnit.setIgnoreWhitespace(true);
		}

		Files.walkFileTree(outputPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path pathToGenerated, BasicFileAttributes attrs) throws IOException {

				String pathToRealExpected = expectedPathFolder + "/" + pathToGenerated.getFileName();

				String generatedXml = new String(Files.readAllBytes(pathToGenerated));
				String realExpectedXml = getContents(pathToRealExpected);

				// System.out.println("Expected:\n" + realExpectedXml);
				// System.out.println("Generated:\n" + generatedXml);

				if (xmlComparisonMode == XmlComparisonMode.xmlDiff) {
					Diff xmlsDiff = null;
					try {
						xmlsDiff = new Diff(generatedXml, realExpectedXml);
						xmlsDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
					} catch (SAXException e) {
						throw new IOException(e);
					}

					boolean similar = xmlsDiff.similar();

					if (similar) {
						System.out.println(pathToGenerated.getFileName() + ": ok");
					} else {
						System.err.println(pathToGenerated.getFileName() + ": INCOMPATIBLE");

						String diffDetails = "Comparison failed" + "\r\n incompatible file: " + pathToGenerated.getFileName()
								+ "\r\n generated path:    " + pathToGenerated + "\r\n expected path:     " + pathToRealExpected
								+ "\r\n diff details:      " + xmlsDiff;

						if (abortOnXmlComparisonFailure) {
							System.err.println(diffDetails);
							Assert.fail(diffDetails);
						} else {
							failures.add(diffDetails);
						}

					}
				} else {

					if (updateMode) {
						Path path = Paths.get(updateBase + pathToRealExpected);
						try (BufferedWriter w = Files.newBufferedWriter(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
							w.write(generatedXml);
							w.flush();
							System.out.println("Wrote to " + path);
						}
						realExpectedXml = generatedXml;
					}

					Assert.assertEquals(pathToGenerated.getFileName() + ": INCOMPATIBLE", cleanUpXml(realExpectedXml), cleanUpXml(generatedXml));

				}

				return FileVisitResult.CONTINUE;
			}
		});

		if (failures.isEmpty()) {
			System.out.println("Comparison of " + outputItemCount + " mapping files completed. No incompatibilities found.");
		} else {
			System.err.println("Comparison of " + outputItemCount + " mapping files completed with " + failures.size() + " failures:");
			System.err.println();

			StringBuilder sb = new StringBuilder();
			for (String failure : failures)
				sb.append(failure).append("\r\n");
			String finalDiffDetails = sb.toString();

			System.err.println(finalDiffDetails);

			Assert.fail(finalDiffDetails);
		}
	}

	protected void compareMappings(String expectedFolderSuffix, int totalExpected) throws Exception {

		if (!isXmlComparisonEnabled)
			return;

		compareMappings(outputFolder.getRoot().toPath(), MappingGenerationTest.expectedPath + File.separator + expectedFolderSuffix, totalExpected);
	}

	protected File getCommonOutput() throws Exception {
		return outputFolder.getRoot();
	}

	protected void cleanCommonOutput() throws Exception {
		outputFolder.delete();
	}

	protected Path getPathFromClasspath(String relativePath) {

		URL resource = getClass().getResource(relativePath);

		if (resource == null) {
			throw new RuntimeException("Classpath path not found: " + relativePath);
		}

		try {
			return Paths.get(resource.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (FileSystemNotFoundException e) {
			Path targetFolder = null;

			try {
				targetFolder = tempFolder.getRoot().getAbsoluteFile().toPath().resolve(relativePath);
				Files.createDirectories(targetFolder.getParent());
			} catch (IOException e2) {
				throw new RuntimeException(e);
			}

			try {
				IOTools.pump(getClass().getResourceAsStream(relativePath), Files.newOutputStream(targetFolder, StandardOpenOption.CREATE));
				return targetFolder;
			} catch (IOException e1) {
				throw new RuntimeException("Failed to pump to " + targetFolder + ": " + e1.getMessage(), e1);
			}

		}
	}

	protected String getContents(String relativePath) {
		try {
			relativePath = relativePath.replace("\\", "/");
			return IOTools.slurp(getClass().getResourceAsStream(relativePath), null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to read from " + relativePath + ": " + e.getMessage(), e);
		}
	}

	private static String cleanUpXml(String input) {
		input = input.replaceAll("\r\n", "\n");
		input = input.replaceAll("<!--.*?-->", "");
		input = input.replaceAll("\\s*\\n", "");
		input = input.replace("<!DOCTYPE hibernate-mapping PUBLIC\t\"-//Hibernate/Hibernate Mapping DTD//EN\"\t\"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd\">", "");
		input = input.replaceAll("\\s+", " ");
		input = input.replaceAll("\" />", "\"/>");
		return input;
	}

}
