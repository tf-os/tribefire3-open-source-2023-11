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
package com.braintribe.tribefire.jinni.support;

import static com.braintribe.console.ConsoleOutputs.blue;
import static com.braintribe.console.ConsoleOutputs.configurableSequence;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConfigurableMultiElementConsoleOutputContainer;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.logging.Logger;
import com.braintribe.model.jinni.api.CheckLicense;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.setup.tools.CheckLicenseProvider;
import com.braintribe.utils.xml.XmlTools;

public class CheckLicenseProcessor implements ReasonedServiceProcessor<CheckLicense, Neutral>, InitializationAware {

	private final Logger logger = Logger.getLogger(CheckLicenseProcessor.class);

	private boolean fixCRLF = false;
	private List<String> excludedFiles = new ArrayList<String>();
	private List<String> whiteListJavadoc = new ArrayList<String>();
	private String headerTemplate = "";
	private Element pomLicenseFragment = null;
	private boolean checkOnly = true;
	Path dedicatedLicense = null;
	Path dedicatedCopying = null;
	Path dedicatedCopyingLesser = null;
	Path dedicatedNotice = null;

	private Boolean fixJDoc;

	private boolean verbose = false;

	private String licenseDir;
	
	@Required
	public void setLicenseDir(File licenseDir) {
		this.licenseDir = licenseDir.getAbsolutePath();
	}

	@Override
	public void postConstruct() {
		dedicatedLicense = Paths.get(licenseDir, "default-LICENSE.txt"); // no comments allowed: AS-IS
		dedicatedCopying = Paths.get(licenseDir, "default-COPYING.txt"); // no comments allowed: AS-IS
		dedicatedCopyingLesser = Paths.get(licenseDir, "default-COPYING-LESSER.txt"); // no comments allowed: AS-IS
		dedicatedNotice = Paths.get(licenseDir, "default-NOTICE.txt"); // no comments allowed: AS-IS
		Path excludedList = Paths.get(licenseDir, "excluded-from-licensing.txt");
		Path dedicatedHeaderPath = Paths.get(licenseDir, "license-header.txt");
		Path dedicatedPomFragmentPath = Paths.get(licenseDir, "pom-license-fragment.xml");
		Path whitelistPath = Paths.get(licenseDir, "whitelist-jdoc-authors.txt");

		excludedFiles = readLinesFromFile(excludedList);
		whiteListJavadoc = readLinesFromFile(whitelistPath);
		try {
			headerTemplate = Files.readString(dedicatedHeaderPath).trim();
		} catch (IOException e) {
			throw new RuntimeException("Could not read " + dedicatedHeaderPath, e);
		}
		
		try {
			pomLicenseFragment = XmlTools.loadXML(dedicatedPomFragmentPath.toFile()).getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException("Could not read " + dedicatedPomFragmentPath, e);
		}
	}

	// read config file. exclude commented lines. return as List<String>
	private List<String> readLinesFromFile(Path fileName) {
		List<String> result = new ArrayList<>();
		try {
			FileInputStream inputStream = new FileInputStream(fileName.toFile());
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#"))
					continue;
				result.add(line);
			}
		} catch (FileNotFoundException e) {
			logger.warn("The config File \"" + fileName + "\" was not found. ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Maybe<Neutral> processReasoned(ServiceRequestContext context, CheckLicense request) {
		verbose  = context.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();

		if (headerTemplate.isEmpty()) {
			return Reasons.build(InvalidArgument.T).text("No license-header.txt found! This will not produce useful results!").toMaybe();
		}
		if (excludedFiles.isEmpty()) {
			return Reasons.build(InvalidArgument.T).text("No file exclusion pattern found. Are you sure?").toMaybe();
		}

		fixCRLF = request.getFixCRLF();
		fixJDoc = request.getJDocCleanup();

		File dir = new File(request.getDir().getPath());

		// check dir/.check-license-exclude and read data
		Path extraExcludePath = Paths.get(dir.getAbsolutePath(), ".check-license-exclude");
		if (extraExcludePath.toFile().isFile()) {
			excludedFiles.addAll(readLinesFromFile(extraExcludePath));
		}

		checkOnly = request.getOnlyChecking();

		GlobFiles gf = new GlobFiles(excludedFiles, dir);
		gf.checkRepo(dir.toPath(), "LICENSE", dedicatedLicense); // check repo itself LICENSE
		gf.checkRepo(dir.toPath(), "NOTICE", dedicatedNotice); // check repo itself NOTICE
		gf.checkRepo(dir.toPath(), "COPYING", dedicatedCopying); // check repo itself COPYING
		gf.checkRepo(dir.toPath(), "COPYING.LESSER", dedicatedCopyingLesser); // check repo itself COPYING.LESSER

		try {
			// go through all the files in all the directories
			Files.walkFileTree(dir.toPath(), gf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		gf.done();

		if (!gf.errorMsg.isEmpty()) {
			System.out.println("Those non-fatal errors were collected:");
			return Reasons.build(InvalidArgument.T).text(gf.errorMsg).toMaybe();
		}
		return Maybe.complete(Neutral.NEUTRAL);
	}

	// helper class to
	// go through entire directory tree
	private class GlobFiles extends SimpleFileVisitor<Path> implements CheckLicenseProvider {

		private final PathMatcher matcherJava;
		private final PathMatcher matcherXML;
		private List<PathMatcher> excludedFilePatterns = new ArrayList<>();

		List<Path> wrongFiles = new ArrayList<>();
		List<Path> wrongCRLF = new ArrayList<>();
		Map<String, Integer> authorList = new HashMap<>(); // non-whitelisted javadoc authors

		int numMatchesJava = 0;
		int numMatchesXml = 0;
		int updateRequired = 0;
		int numExcluded = 0; // how many files were excluded
		int numCRLF = 0; // how many Windows-style files
		int numCRLFfixed = 0;
		int numFilesCopy = 0; // how many LICENSE/NOTICE/COPYING* files copied
		int numFilesWrong = 0;

		String errorMsg = "";
		File rootDir;

		GlobFiles(List<String> excludes, File dir) {
			this.rootDir = dir;
			matcherJava = FileSystems.getDefault().getPathMatcher("glob:*.java");
			matcherXML = FileSystems.getDefault().getPathMatcher("glob:*.xml");
			for (String exclude : excludes)
				excludedFilePatterns.add(FileSystems.getDefault().getPathMatcher("glob:" + exclude));
		}

		void checkRepo(Path repo, String name, Path dedicatedContent) {

			if (!dedicatedContent.toFile().exists())
				return;

			// there should be a LICENSE file in a repo
			Path path = Paths.get(repo.toString(), name);

			for (PathMatcher checkExclude : excludedFilePatterns)
				if (checkExclude.matches(path.toAbsolutePath())) {
					numExcluded = getNumExcluded() + 1;
					return;
				}

			boolean needToCopy = false;

			if (!Files.exists(path)) {

				// LICENSE file is missing
				needToCopy = true;

			} else {

				// file is there. Check, if it is correct one.
				try {
					if (Files.mismatch(path, dedicatedContent) != -1) {
						// oh, this is the wrong content ??
						needToCopy = true;
						wrongFiles.add(path);
					}
				} catch (IOException e) {
					errorMsg += "Cannot compare file " + path + " ! ";
					e.printStackTrace();
				}
			}

			// update LICENSE if needed and wanted
			if (needToCopy) {
				numFilesWrong++;

				if (!checkOnly) {
					numFilesCopy++;
					try {
						Files.copy(dedicatedContent, path, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						errorMsg += "Cannot copy file " + path + " ! ";
						System.out.println(e.toString());
					}
				}
			}
		}

		void check(Path file) {

			if (file.toFile().isDirectory())
				return;

			for (PathMatcher checkExclude : excludedFilePatterns)
				if (checkExclude.matches(file.toAbsolutePath())) {
					numExcluded = getNumExcluded() + 1;
					return;
				}

			Path name = file.getFileName();
			Path relativePath = rootDir.toPath().relativize(file);

			boolean needsUpdateOnDisk = false;

			// now check for xml and java files
			boolean isJava = matcherJava.matches(name) && relativePath.getNameCount() > 2 && relativePath.getName(1).toString().contains("src");
			boolean isXml = matcherXML.matches(name) && relativePath.getNameCount() == 2;
			if (isJava || isXml) {
				
				if (verbose) {
					ConfigurableConsoleOutputContainer info = new ConfigurableMultiElementConsoleOutputContainer();
					info.append(" - \"" + file.toAbsolutePath().toString() + "\"");
					println(sequence(info));
				}

				try {
					String data = Files.readString(file);
					String fileName = file.toString();

					if (data.isEmpty()) {
						logger.warn("File: " + file + " appears to be empty. ");
						errorMsg += "File: " + file + " appears to be empty. ";
						return;
					}

					if (checkCRLF(data)) {
						numCRLF = getNumCRLF() + 1;
						wrongCRLF.add(file);
						if (fixCRLF) {
							numCRLFfixed = getNumCRLFfixed() + 1;
							data = data.replace("\r", "");
							needsUpdateOnDisk = true;
						}
					}

					if (isJava) {
						numMatchesJava = getNumMatchesJava() + 1;
						CheckLicenseResult check = checkLicenseJava(data, fileName);
						check = analyseCheckResult(check, fileName, rootDir);
						if (check.result) {
							updateRequired = getUpdateRequired() + 1;
							if (!checkOnly) {
								data = assembleNewData(check, MODE.JAVA);
								needsUpdateOnDisk = true;
							}
						}
						if (fixJDoc) {
							CheckJdocResult checkJDoc = checkJdoc(data, whiteListJavadoc, authorList);
							if (!checkOnly && checkJDoc.needsUpdate) {
								needsUpdateOnDisk = true;
								data = checkJDoc.data;
							}
						}

					} else if (isXml) {
						
						numMatchesXml = getNumMatchesXml() + 1;
						CheckLicenseResult check = checkLicenseXml(data, fileName);
						check = analyseCheckResult(check, fileName, rootDir);
						if (check.result) {
							updateRequired = getUpdateRequired() + 1;
							if (!checkOnly) {
								data = assembleNewData(check, MODE.XML);
								needsUpdateOnDisk = true;
							}
						}
					}

					if (needsUpdateOnDisk) {
						writeToDisk(data, file);
					}
				} catch (FileNotFoundException e) {
					errorMsg += "File " + file + " not found! ";
					e.printStackTrace();
				} catch (IOException e) {
					errorMsg += "File " + file + " reading error! ";
					e.printStackTrace();
				}
			}
		}

		private void writeToDisk(String data, Path file) {

			file.toFile().delete(); // delete original file (it is still in git history!)
			// write new file
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(file.toFile(), StandardCharsets.UTF_8));
				writer.write(data);
				writer.close();
			} catch (IOException e) {
				errorMsg += "FAILED TO WRITE " + file + "! ";
				e.printStackTrace();
			}
		}

		// final output
		public void done() {
			ConfigurableConsoleOutputContainer output = configurableSequence();
			output.append("LICENSE/NOTICE/COPYING/COPYING.LESSER files\n");
			output.append("   - Need update : ");
			output.append(blue(String.valueOf(getNumFilesWrong())));
			output.append("\n");
			output.append("   - Changed     : ");
			output.append(blue(String.valueOf(getNumFilesCopy())));
			output.append("\n");
			if (getNumCRLF() > 0) {
				output.append(("Files with CRLF  : "));
				output.append(blue(String.valueOf(getNumCRLF())));
				output.append("\n");
				output.append(("CRLF fixed       : "));
				output.append(blue(String.valueOf(getNumCRLFfixed())));
				output.append("\n");
			}
			output.append(("Java Files       : "));
			output.append(blue(String.valueOf(getNumMatchesJava())));
			output.append("\n");
			output.append(("XML Files        : "));
			output.append(blue(String.valueOf(getNumMatchesXml())));
			output.append("\n");
			if (checkOnly) {
				output.append(("Update required  : "));
				output.append(blue(String.valueOf(getUpdateRequired())));
				output.append("\n");
			} else {
				output.append(("Updated          : "));
				output.append(blue(String.valueOf(getUpdateRequired())));
				output.append("\n");
			}
			output.append(("Excluded         : "));
			output.append(blue(String.valueOf(getNumExcluded())));
			output.append("\n");
			Map<String, Integer> jdocAuthors = getJdocAuthors();
			if (!jdocAuthors.isEmpty()) {
				output.append(("NonWL doc authors: "));
				String comma = "";
				for (Map.Entry<String, Integer> author : jdocAuthors.entrySet()) {
					String name = author.getKey();
					Integer number = author.getValue();
					output.append(comma);
					output.append("\"");
					output.append(blue(name));
					output.append("\"");
					output.append(" (");
					output.append(number.toString());
					output.append(")");
					comma = ", ";
				}
				output.append("\n");
			}
			if (checkOnly)
				output.append("Note, if you want to apply updates, specify --onlyChecking false and --year YYYY" + "\n");
			else
				output.append(red("Warning, check all changes carefully before commiting!" + "\n"));
			println(sequence(output));
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			check(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			check(dir);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			logger.error(exc);
			return FileVisitResult.CONTINUE;
		}

		public String getHeaderText() {
			return headerTemplate;
		}
		
		public Element getPomLicenseFragment() {
			return pomLicenseFragment;
		}
		
		public int getNumMatchesJava() {
			return numMatchesJava;
		}

		public int getNumMatchesXml() {
			return numMatchesXml;
		}

		public int getUpdateRequired() {
			return updateRequired;
		}

		public int getNumExcluded() {
			return numExcluded;
		}

		public int getNumCRLF() {
			return numCRLF;
		}

		public int getNumCRLFfixed() {
			return numCRLFfixed;
		}

		public int getNumFilesCopy() {
			return numFilesCopy;
		}

		public int getNumFilesWrong() {
			return numFilesWrong;
		}

		public Map<String, Integer> getJdocAuthors() {
			return authorList;
		}

		@Override
		public boolean getCheckOnly() {
			return checkOnly;
		}

		@Override
		public void addErrorMsg(String string) {
			errorMsg += string;
		}

	}
}
