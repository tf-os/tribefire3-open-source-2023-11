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
package com.braintribe.model.processing.xmi.converter.experts;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.braintribe.model.processing.xmi.converter.registry.XmiRegistry;
import com.braintribe.model.processing.xmi.converter.registry.entries.PackagePartEntry;
import com.braintribe.utils.xml.dom.DomUtils;

/**
 * returns a package name from a class-tag steps up the document from the
 * class-tag to find all package elements - retrieves the relevant ancestors in
 * the DOM builds the {@link XmiRegistry}'s {@link PackagePartEntry} to reflect
 * the packages (required for id recycling)
 * 
 * @author pit
 *
 */
public class PackageExpert {

	private XmiRegistry xmiRegistry;

	public void setXmiRegistry(XmiRegistry xmiRegistry) {
		this.xmiRegistry = xmiRegistry;
	}

	/**
	 * gets the full package name of the class as {@link Element}
	 * 
	 * @param classE - the {@link Element} that represents the class
	 * @return - the {@link String} with the fully qualified name of the package
	 */
	public String getPackageNameOfClass(Element classE) {
		// build classpath
		List<String> packages = new ArrayList<String>();
		List<PackagePartEntry> packagePartEntries = new ArrayList<PackagePartEntry>();
		Element ancestor = getAncestorPackage(classE);
		while (ancestor != null) {
			String packageName = ancestor.getAttribute("name");
			packagePartEntries
					.add(xmiRegistry.addPackagePartEntryById(ancestor.getAttribute("xmi.id"), packageName, ancestor));
			packages.add(packageName);
			ancestor = getAncestorPackage(ancestor);
		}
		// build full package name AND finalize the PackagePartEntries in the
		// XmiRegistry
		String fullPackageName = buildJavaPackageName(packages, packagePartEntries);

		return fullPackageName;
	}

	/**
	 * retrieve the relevant ancestor
	 * 
	 * @param element - the {@link Element} to navigate from
	 * @return - the {@link Element} or null if none's found
	 */
	private Element getAncestorPackage(Element element) {
		Element ancestor = DomUtils.getAncestor(element, ".*Namespace.ownedElement");
		if (ancestor == null)
			return null;
		ancestor = DomUtils.getAncestor(ancestor, ".*Package");
		return ancestor;
	}

	/**
	 * build a full package name from the partial packages, and while doing that,
	 * set the java names to the registry entries, so that they can be identified
	 * globally
	 * 
	 * @param packages - the {@link List} of package names as {@link String}
	 * @param entries  - the {@link List} of {@link PackagePartEntry}
	 * @return - the fully qualified package name
	 */
	private String buildJavaPackageName(List<String> packages, List<PackagePartEntry> entries) {
		StringBuilder builder = new StringBuilder();
		for (int i = packages.size() - 1; i >= 0; i--) {
			if (builder.length() > 0)
				builder.append(".");
			builder.append(packages.get(i));
			entries.get(i).setJavaName(builder.toString());
		}
		return builder.toString();
	}

}
