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
package com.braintribe.devrock.mungojerry.dialog.experts;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.braintribe.build.gwt.GwtModule;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mungojerry.dialog.tab.AnalysisController;
import com.braintribe.devrock.mungojerry.dialog.tab.ParentPage;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

public class ModuleDeclarationWriter {
	
	private AnalysisController analysisController;
	private ParentPage parentPage;
	
	@Required @Configurable
	public void setAnalysisController(AnalysisController analysisController) {
		this.analysisController = analysisController;
	}
	
	@Required @Configurable
	public void setParentPage(ParentPage parentPage) {
		this.parentPage = parentPage;
	}

	public void save( String moduleName) {
		try {
			// determine place of file from name?			
			IProject project = analysisController.getProject();			
			String moduleDeclarationNameFileName = project.getLocation().toOSString() + "/src/" + moduleName.replaceAll( "\\.", "/") + ".gwt.xml";
			
			Document document = DomParser.load().from( new File(moduleDeclarationNameFileName));
			Element moduleElement = document.getDocumentElement();

			// remove whitespace
			NodeList nodes = moduleElement.getChildNodes();
			List<Text> texts = new LinkedList<Text>(); 
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Text)
					texts.add((Text)node);
			}
			
			for (Text text: texts)
				moduleElement.removeChild(text);


			// update inherits tags
			NodeList inheritsElements = moduleElement.getElementsByTagName("inherits");
			List<Element> oldInheritsElements = new LinkedList<Element>();

			
			for (int i = 0; i < inheritsElements.getLength(); i++) {
				Element inheritsElement = (Element)inheritsElements.item(i);
				oldInheritsElements.add(inheritsElement);
			}

			for (Element inheritsElement: oldInheritsElements) {
				Node node = inheritsElement.getPreviousSibling();
				if (!(
						(node instanceof Comment) && 
						((Comment)node).getTextContent().trim().toLowerCase().equals("resource module"))
					) {	
					moduleElement.removeChild(inheritsElement);
				}
			}
			
			Node insertNode = findInsertionPoint( moduleElement);
			Node firstChild = insertNode != null ? insertNode.getNextSibling() : moduleElement.getFirstChild();
			
			for (GwtModule usedModule: analysisController.getModules()) {
				if (usedModule.getModuleName().equalsIgnoreCase( moduleName)) continue;
				Element inheritsElement = document.createElement("inherits");
				inheritsElement.setAttribute("name", usedModule.getModuleName());
				moduleElement.insertBefore(inheritsElement, firstChild);
			}
	
			
			DomParser.write().from( document).to( new File(moduleDeclarationNameFileName));
			
			parentPage.setDescription("gwt.xml file for module [" + moduleName + "] updated.");

	
		}  catch (DomParserException e) {
			parentPage.setErrorMessage( "error: " + e.getLocalizedMessage());
		}		
	}

	private Node findInsertionPoint(Element moduleElement) {
		NodeList nodes = moduleElement.getChildNodes();
		for (int i = 0; i < nodes.getLength()-1; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.COMMENT_NODE) {
				String value = node.getNodeValue();
				if (value.equalsIgnoreCase( "GENERATOR_INSERTION_POINT")) {
					return node;
				}
			}
		}
		return null;
	}
}
