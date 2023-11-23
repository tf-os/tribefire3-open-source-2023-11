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
package com.braintribe.model.io.metamodel.render.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * @author peter.gazdik
 */
public class ImportManager {

	private final String pckg;

	private static String JAVA_LANG_PACKAGE = "java.lang";

	private final Map<String, String> simpleToImport = newMap();
	private final Set<String> samePackageTypesSimple = newSet();
	private final Set<String> javaLangTypesSimple = newSet();

	public ImportManager(String pckg) {
		NullSafe.nonNull(pckg, "package");

		if (pckg.startsWith("java."))
			throw new IllegalArgumentException("Java package is not supported: " + pckg);

		this.pckg = pckg;
	}

	public void useType(Class<?> clazz) {
		useType(clazz.getName());
	}

	public void useType(String fullName) {
		if (fullName == null)
			return;

		String simpleName = getSimpleName(fullName);

		if (samePackageTypesSimple.contains(simpleName))
			return;

		if (isSamePackage(fullName)) {
			samePackageTypesSimple.add(simpleName);

			simpleToImport.remove(simpleName);
			javaLangTypesSimple.remove(simpleName);

			return;
		}

		if (javaLangTypesSimple.contains(simpleName))
			return;

		if (isPackage(fullName, JAVA_LANG_PACKAGE)) {
			javaLangTypesSimple.add(simpleName);

			simpleToImport.remove(simpleName);

			return;
		}

		simpleToImport.putIfAbsent(simpleName, fullName);
	}

	public String getTypeRef(Class<?> clazz) {
		return getTypeRef(clazz.getName());
	}

	public String getTypeRef(String fullName) {
		String simpleName = getSimpleName(fullName);

		if (isSamePackage(fullName))
			return simpleName;

		String importedType = simpleToImport.get(simpleName);
		if (fullName.equals(importedType))
			return simpleName;

		if (importedType != null)
			return fullName;

		return isJavaLang(simpleName, fullName) ? simpleName : fullName;
	}

	private String getSimpleName(String fullName) {
		if (hasDot(fullName))
			return StringTools.getSubstringAfterLast(fullName, ".");
		else
			return fullName;
	}

	private boolean isSamePackage(String fullName) {
		if (pckg.length() == 0)
			return !hasDot(fullName);

		return isPackage(fullName, pckg);
	}

	private boolean isPackage(String fullName, String pckg) {
		if (!fullName.startsWith(pckg))
			return false; // different package

		String rest = fullName.substring(pckg.length() + 1);
		return !rest.isEmpty() && !hasDot(rest);
	}

	private static boolean hasDot(String rest) {
		return rest.contains(".");
	}

	private static boolean isJavaLang(String simpleName, String fullName) {
		return (fullName.length() == JAVA_LANG_PACKAGE.length() + 1 + simpleName.length()) && //
				fullName.startsWith(JAVA_LANG_PACKAGE);
	}

	public Collection<String> getTypesToImport() {
		return simpleToImport.values();
	}

	public List<List<String>> getTypesToImportInGroups() {
		Map<String, List<String>> collect = simpleToImport.values().stream() //
				.sorted() //
				.collect(Collectors.groupingBy( //
						this::getPackageGroup, //
						() -> new TreeMap<>(), //
						Collectors.toList() //
				));

		return collect.values().stream() //
				.collect(Collectors.toList());
	}

	private String getPackageGroup(String pckg) {
		return StringTools.getSubstringBefore(pckg, ".");
	}
}
