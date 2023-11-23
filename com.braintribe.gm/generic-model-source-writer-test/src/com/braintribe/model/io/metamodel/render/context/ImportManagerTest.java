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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link ImportManager}
 * 
 * @author peter.gazdik
 */
public class ImportManagerTest {

	private ImportManager manager = new ImportManager("my.package");

	@Test
	public void typeForDefaultPackage() throws Exception {
		manager = new ImportManager("");

		manager.useType("DefaultPackageType");
		manager.useType("pckg1.Type");
		manager.useType("pckg2.Type");

		assertTypeRef("DefaultPackageType", "DefaultPackageType");
		assertTypeRef("pckg1.Type", "Type");
		assertTypeRef("pckg2.Type", "pckg2.Type");

		assertImports("pckg1.Type");
	}

	@Test
	public void typeFromSamePackage() throws Exception {
		manager.useType("my.package.Type");

		assertTypeRef("my.package.Type", "Type");
		assertImports();
	}

	@Test
	public void typeFromSameDiffPackage_First() throws Exception {
		manager.useType("other.package.Type");
		manager.useType("my.package.Type");

		assertTypeRef("my.package.Type", "Type");
		assertTypeRef("other.package.Type", "other.package.Type");
		assertImports();
	}

	@Test
	public void typeFromSameDiffPackage_Last() throws Exception {
		manager.useType("my.package.Type");
		manager.useType("other.package.Type");

		assertTypeRef("my.package.Type", "Type");
		assertTypeRef("other.package.Type", "other.package.Type");
		assertImports();
	}

	@Test
	public void javaLangType() throws Exception {
		manager.useType("java.lang.String");

		assertTypeRef("java.lang.String", "String");
		assertImports();
	}

	@Test
	public void javaLangTypeNamesake_DifferentPackage_First() throws Exception {
		manager.useType("pckg.String");
		manager.useType("java.lang.String");

		assertTypeRef("java.lang.String", "String");
		assertTypeRef("pckg.String", "pckg.String");
		assertImports();
	}

	@Test
	public void javaLangTypeNamesake_DifferentPackage_Last() throws Exception {
		manager.useType("java.lang.String");
		manager.useType("pckg.String");

		assertTypeRef("java.lang.String", "String");
		assertTypeRef("pckg.String", "pckg.String");
		assertImports();
	}

	@Test
	public void testImportsGrouped() throws Exception {
		manager.useType("bbb.Bb1");
		manager.useType("aaa.Type1");
		manager.useType("aaa.Type2");

		List<List<String>> groups = manager.getTypesToImportInGroups();
		assertThat(groups).hasSize(2);
		
		assertThat(groups.get(0)).containsExactly("aaa.Type1", "aaa.Type2");
		assertThat(groups.get(1)).containsExactly("bbb.Bb1");
		
	}

	private void assertTypeRef(String fullName, String expectedTypeRef) {
		assertThat(manager.getTypeRef(fullName)).isEqualTo(expectedTypeRef);
	}

	private void assertImports(String... imports) {
		Set<String> actualImports = newSet(manager.getTypesToImport());

		assertThat(actualImports).isEqualTo(asSet(imports));
	}

}
