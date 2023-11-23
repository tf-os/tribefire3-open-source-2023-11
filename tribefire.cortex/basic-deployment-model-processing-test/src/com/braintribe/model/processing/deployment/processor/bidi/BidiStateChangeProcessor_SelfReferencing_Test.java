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
package com.braintribe.model.processing.deployment.processor.bidi;

import java.util.ArrayList;

import org.junit.Test;

import com.braintribe.model.processing.deployment.processor.bidi.data.Folder;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class BidiStateChangeProcessor_SelfReferencing_Test extends AbstractBidiScpTests {

	// ####################################
	// ## . . . . Simple tests . . . . . ##
	// ####################################

	@Test
	public void testSetParentFolder() throws Exception {
		apply(session -> {
			Folder parent = newFolder(session, "ParentFolder");
			Folder subFolder = newFolder(session, "SubFolder");
			subFolder.setParent(parent);
		});

		Folder parent = folderByName("ParentFolder");
		Folder subFolder = folderByName("SubFolder");

		BtAssertions.assertThat(parent.getSubFolders()).contains(subFolder);
		BtAssertions.assertThat(subFolder.getParent()).isEqualTo(parent);
	}

	@Test
	public void testSetSubFolderFolder() throws Exception {
		apply(session -> {
			Folder parent = newFolder(session, "ParentFolder");
			Folder subFolder = newFolder(session, "SubFolder");

			parent.setSubFolders(new ArrayList<Folder>());
			parent.getSubFolders().add(subFolder);
		});

		Folder parent = folderByName("ParentFolder");
		Folder subFolder = folderByName("SubFolder");

		BtAssertions.assertThat(parent.getSubFolders()).contains(subFolder);
		BtAssertions.assertThat(subFolder.getParent()).isEqualTo(parent);
	}

	@Test
	public void testRemoveParentFolder() throws Exception {
		// Preparation
		apply(session -> {
			Folder parent = newFolder(session, "ParentFolder");
			Folder subFolder = newFolder(session, "SubFolder");

			parent.setSubFolders(new ArrayList<Folder>());
			parent.getSubFolders().add(subFolder);
		});

		// Actual test
		apply(session -> {
			Folder subFolder = folderByName("SubFolder", session);
			subFolder.setParent(null);
		});

		Folder parent = folderByName("ParentFolder");
		Folder subFolder = folderByName("SubFolder");

		BtAssertions.assertThat(subFolder.getParent()).isNull();
		BtAssertions.assertThat(subFolder).isNotIn(parent.getSubFolders());
	}

	@Test
	public void testRemoveSubFolder() throws Exception {
		// Preparation
		apply(session -> {
			Folder parent = newFolder(session, "ParentFolder");
			Folder subFolder1 = newFolder(session, "SubFolder1");
			Folder subFolder2 = newFolder(session, "SubFolder2");
			Folder subFolder3 = newFolder(session, "SubFolder3");

			parent.setSubFolders(new ArrayList<Folder>());
			parent.getSubFolders().add(subFolder1);
			parent.getSubFolders().add(subFolder2);
			parent.getSubFolders().add(subFolder3);

		});

		// Actual test
		apply(session -> {
			Folder subFolder2 = folderByName("SubFolder2", session);
			Folder parent = folderByName("ParentFolder", session);

			parent.getSubFolders().remove(subFolder2);
		});

		Folder parent = folderByName("ParentFolder");
		Folder subFolder2 = folderByName("SubFolder2");

		BtAssertions.assertThat(parent.getSubFolders()).hasSize(2);
		BtAssertions.assertThat(subFolder2).isNotIn(parent.getSubFolders());
		BtAssertions.assertThat(subFolder2.getParent()).isNull();
	}

}
