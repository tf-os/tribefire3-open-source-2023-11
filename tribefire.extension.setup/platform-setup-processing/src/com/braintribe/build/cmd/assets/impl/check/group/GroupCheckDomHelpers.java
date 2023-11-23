package com.braintribe.build.cmd.assets.impl.check.group;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;

public class GroupCheckDomHelpers {

	final public static String GROUP_IMPORT_SCRIPT = "com.braintribe.devrock.ant:group-ant-script#1.0";
	final public static String PARENT_IMPORT_SCRIPT = "com.braintribe.devrock.ant:parent-ant-script#1.0";

	public static Element extractBtImportElement(File buildXmlFile) {
		return getElementByXPath(buildXmlFile, "import");
	}

	public static Element extractProjectNameElement(File projectXmlFile) {
		return getElementByXPath(projectXmlFile, "/projectDescription/name");
	}

	public static Document getDocument(File xmlFile) {
		if (!xmlFile.exists() || !xmlFile.isFile()) {
			throw new IllegalStateException("Either XML file " + xmlFile.getAbsolutePath() + " is not found or it's not a file!");
		}
		return DOMTools.parse(FileTools.readStringFromFile(xmlFile));
	}

	private static Element getElementByXPath(File xmlFile, final String xpath) {
		return getElementByXPath(getDocument(xmlFile), xpath);
	}

	public static Element getElementByXPath(Document document, final String xpath) {
		return DOMTools.getElementByXPath(document, xpath);
	}
}
