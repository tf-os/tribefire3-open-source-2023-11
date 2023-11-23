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
package com.braintribe.setup.tools;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.utils.DOMTools;
import com.braintribe.utils.xml.XmlTools;

/**
 * Tools for CheckLicenseProcessor
 * 
 *
 */
public interface CheckLicenseProvider {

	// we distinguish java and xml files
	enum MODE {
		JAVA,
		XML,
		OTHER
	}

	public class CheckLicenseResult {
		public boolean result = false;
		public String prefix = null; // convention
		public String license = "";
		public String body = "";
		public String fileName;
		public File rootDir;

		public CheckLicenseResult(String fileName) {
			this.fileName = fileName;
			result = false;
		}

		public CheckLicenseResult(String fileName, String pr, String li, String bo) {
			this.fileName = fileName;
			prefix = pr;
			license = li;
			body = bo;
			result = true;
		}
	}

	public class CheckJdocResult {
		public boolean needsUpdate = false;
		public String data = null;

		public CheckJdocResult(String data) {
			needsUpdate = true;
			this.data = data;
		}

		public CheckJdocResult() {
			needsUpdate = false;
			data = null;
		}
	}

	Element getPomLicenseFragment();
	
	boolean getCheckOnly();

	void addErrorMsg(String string);

	String getHeaderText();

	// check if file contains windows-style CR ("\r\n")
	default boolean checkCRLF(String data) {
		if (data.contains("\r"))
			return true;
		return false;
	}

	default CheckLicenseResult checkLicenseXml(String data) {
		return checkLicenseXml(data, "n/a");
	}

	default CheckLicenseResult checkLicenseXml(String data, String fileName) {

		// xml pattern:
		final String patternString = "^\\s*(<\\?[\\s\\S]*?\\?>){0,1}\\s*(?:<!--([\\s\\S]*?(?i)(?:license)[\\s\\S]*?)-->){0,1}\\s*(<[\\s\\S]*>\\s*)$";
		final Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(data);

		if (!matcher.find()) {
			addErrorMsg("No match in XML File: " + fileName + "!");
			return new CheckLicenseResult(fileName);
		}

		String license = "";
		if (matcher.group(2) != null)
			license = matcher.group(2);

		return new CheckLicenseResult(fileName, matcher.group(1), license, matcher.group(3));
	}

	default CheckLicenseResult checkLicenseJava(String data) {
		return checkLicenseJava(data, "n/a");
	}

	default CheckLicenseResult checkLicenseJava(String data, String fileName) {

		// @formatter:off
		// first check single-line comment
		// ^\s*((?:\/\/.*\s+)*(?:\/\/.*(?i)(?:license).*\s+)(?:\/\/.*(?:\n|\r\n))*)\s*([\s\S]*)$
		// ^^^^ - trailing new lines/WS
		//      ^^^^^^^^^^^^^^ - any lines with "//...."
		//                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ - ONE line with "license" (case i)
		//                                                    ^^^^^^^^^^^^^^^^^^^^^^ - more lines with "//...." but terminated with line break
		//                                                                           ^^^^ - found, or not found
		//                                                                                ^^^ - intermediate WS and line breaks
		//                                                                                   ^^^^^^^^ - file body
		// @formatter:on
		// note, the group-1 still contains the trailing "//"s
		final String patternStringSL = "^\\s*((?: *\\/\\/.*\\n)*(?: *\\/\\/.*(?i)(?:license).*\\n)(?: *\\/\\/.*(?:\\n|\\r\\n))*){0,1}\\s*?(\\S[\\s\\S]*)$";
		final Pattern patternSL = Pattern.compile(patternStringSL);
		Matcher matcher = patternSL.matcher(data);
		boolean foundMatch = matcher.find();

		if (foundMatch && matcher.group(1) != null) {

			String comment = matcher.group(1);
			final String commentPattern = "( *\\/\\/\\s*)";
			final Pattern patternComment = Pattern.compile(commentPattern);
			comment = patternComment.matcher(comment).replaceAll("");
			return new CheckLicenseResult(fileName, null, comment, matcher.group(2));
		}

		// @formatter:off
		// now check multi-line comments
		// ^\s*(?:\/\*\s*([\s\S]*?(?i)(?:license)[\s\S]*?)\*\/){0,1}\s*([\s\S]*?)$
		//  ^^^ - trailing new lines/WS
		//        ^^^^^^^ - "/*" and any following WS+line break
		//               ^^^^^^^^^ - and comment text
		//                        ^^^^^^^^^^^^^^^^^ - required: "license" case-i
		//                                         ^^^^^^^^ - any more comment text
		//                                                 ^^^^^ - trailing "*/"
		//                                                       ^^^^^ - found, not-found
		//                                                            ^^^ - intermediate WS+LB
		//                                                    text body - ^^^^^^^^ 
		// @formatter:on
		final String patternStringML = "^\\s*(?:\\/\\*\\s*([\\s\\S]*?(?i)(?:license)[\\s\\S]*?)\\*\\/){0,1}\\s*?(\\S[\\s\\S]*)$";
		final Pattern patternML = Pattern.compile(patternStringML);
		matcher = patternML.matcher(data);
		foundMatch = matcher.find();

		if (!foundMatch) {
			addErrorMsg("Invalid Java file " + fileName + " found! ");
			return new CheckLicenseResult(fileName);
		}
		String license = "";
		if (matcher.group(1) != null)
			license = matcher.group(1);
		return new CheckLicenseResult(fileName, null, license, matcher.group(2));
	}
	
	default CheckLicenseResult checkLicense(CheckLicenseResult data, File rootDir) {
		return analyseCheckResult(data, "n/a", rootDir);
	}

	default CheckLicenseResult analyseCheckResult(CheckLicenseResult data, String fileName, File rootDir) {

		data.rootDir = rootDir;
		
		String headerText = getHeaderText();
		
		if (headerText.isEmpty()) {
			addErrorMsg("Cannot process without licence-header data in " + fileName + "! ");
			// this is an exception!
			data.result = false; // cannot do anything
			return data;
		}

		if (data.license.equals(headerText)) {
			data.result = false;
			return data;
		}
		
		data.result = true;
		data.license = headerText;
		
		return data;
	}

	default String assembleNewData(CheckLicenseResult check, MODE mode) {

		String header = check.license;
		String prefix = check.prefix;
		String body = check.body;

		StringWriter writer = new StringWriter();

		if (prefix != null)
			writer.write(prefix + "\n");

		switch (mode) {
			case XML: {
				writer.write("<!--\n");
				writer.write(header.trim());
				writer.write("\n-->\n");
				break;
			}
			case JAVA: {
				Scanner scanner = new Scanner(header.trim());
				writer.write("// ============================================================================\n");
				while (scanner.hasNextLine()) {
					writer.write("// ");
					writer.write(scanner.nextLine());
					writer.write("\n");
				}
				writer.write("// ============================================================================\n");
				break;
			}
			case OTHER:
				break;
		}

		if (body != null) {
			File file = new File(check.fileName);
			
			if (file.getName().equals("pom.xml"))
				body = injectLicenseElement(check.fileName, body);
			
			writer.write(body);
		}

		return writer.toString();
	}
	
	default String injectLicenseElement(String fileName, String body) {
		try {
			Document document = DOMTools.parse(body);
			
			Element projectElement = document.getDocumentElement();
			Element existingLicensesElement = DOMTools.getElementByPath(projectElement, "licenses");
			
			if (existingLicensesElement != null) {
				Node node = existingLicensesElement;
				while (true) {
					Node previousNode = node.getPreviousSibling();
					projectElement.removeChild(node);
					if (previousNode == null || previousNode instanceof Element)
						break;
					
					node = previousNode;
				}
			}
			
			Element predecessorElement = DOMTools.getElementByPath(projectElement, "properties");
			
			if (predecessorElement == null)
				predecessorElement = DOMTools.getElementByPath(projectElement, "version");
			
			if (predecessorElement == null)
				throw new IllegalStateException("Unexpected pom.xml content missing mandatory version element: " + fileName);
			
			Node successorNode = predecessorElement.getNextSibling();
			
			for (Node node = getPomLicenseFragment().getFirstChild(); node != null; node = node.getNextSibling()) {
				Node clonedNode = node.cloneNode(true);
				document.adoptNode(clonedNode);
				projectElement.insertBefore(clonedNode, successorNode);
			}
			
			return DOMTools.toString(document);
			
		} catch (Exception e) {
			throw new RuntimeException("Error while parsing pom xml file: " + fileName, e);
		}
	}

	default CheckJdocResult checkJdoc(String data, List<String> whitelist, Map<String, Integer> authorList) {
		// search for javadoc "@author ..." authors

		StringBuilder modData = new StringBuilder(); // updated version of file, if needed
		boolean update = false;

		String patternStr = "^\\h*\\**\\h*(@author)\\h";
		Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(data);

		int location = 0;
		int nextLocation = 0;
		while (matcher.find(location)) {

			nextLocation = matcher.start(1); // 1 is the (@author) group

			// ... move back to beginning of line
			int lineStart = data.lastIndexOf("\n", nextLocation);
			String lineStartSnip = data.substring(lineStart, nextLocation); // without the newline

			modData.append(data.substring(location, lineStart)); // everything until start of line
			location = matcher.end(1); // after "@author"

			nextLocation = data.indexOf("\n", location);
			String authorInfo = data.substring(location, nextLocation);
			location = nextLocation; // after newline

			String[] authors = authorInfo.split(",");
			List<String> remainingAuthors = new ArrayList<>();
			for (String author : authors) {
				author = author.trim();
				if (whitelist.contains(author)) {
					remainingAuthors.add(author);
				} else {
					// non-WL
					authorList.merge(author, 1, Integer::sum);
					update = true;
				}
			}

			if (remainingAuthors.size() > 0) {
				// keep whitelisted authors
				modData.append(lineStartSnip);
				modData.append("@author ");
				String comma = "";
				for (String author : remainingAuthors) {
					modData.append(author);
					modData.append(comma);
					comma = ", ";
				}
			}
		}
		if (!update)
			return new CheckJdocResult();

		modData.append(data.substring(location, data.length())); // remaining data
		return new CheckJdocResult(modData.toString());
	}
}
