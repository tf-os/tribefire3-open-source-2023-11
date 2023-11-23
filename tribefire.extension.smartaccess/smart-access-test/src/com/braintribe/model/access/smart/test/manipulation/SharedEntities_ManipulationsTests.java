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
package com.braintribe.model.access.smart.test.manipulation;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import org.junit.Test;

import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedFile;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * These tests check if the correct delegate is chosen.
 * 
 */
public class SharedEntities_ManipulationsTests extends AbstractManipulationsTests {

	/**
	 * This works out of the box, cause the {@link ManipulationRemotifier} creates references containing the partition,
	 * so we do not have to infer anything.
	 */
	@Test
	public void explicitAssignment() throws Exception {
		SharedFile f = newSharedFile();
		f.setPartition(accessIdA);
		commit();

		BtAssertions.assertThat(f.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(1);
	}

	@Test
	public void changingValue() throws Exception {
		SharedFile f = newSharedFile();
		f.setPartition(accessIdA);
		commit();

		f.setName("File 1");
		commit();

		BtAssertions.assertThat(f.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(1);
	}

	@Test
	public void defaultAssignment() throws Exception {
		SharedFile f = newSharedFile();
		commit();

		BtAssertions.assertThat(f.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countSharedFileInB()).isEqualTo(1);
	}

	// ###################################
	// ## . . Assigning To Defined . . .##
	// ###################################

	@Test
	public void assigningToDefined_Entity() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f1.setParent(f2);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningToDefined_Set() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f1.getChildSet().add(f2);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningToDefined_List() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f1.getChildList().add(f2);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningToDefined_MapKey() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f1.getChildMap().put(f2, null);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningToDefined_MapValue() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f1.getChildMap().put(null, f2);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	// ###################################
	// ## . . . Assigning Defined . . . ##
	// ###################################

	@Test
	public void assigningDefined_Entity() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.setParent(f1);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningDefined_Entity_DefinedAlreadyExists() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);
		commit(); // This line makes the difference compared to previous test

		SharedFile f2 = newSharedFile();
		f2.setParent(f1);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningDefined_Set() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.getChildSet().add(f1);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningDefined_List() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.getChildList().add(f1);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningDefined_MapKey() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.getChildMap().put(f1, null);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	@Test
	public void assigningDefined_MapValue() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.getChildMap().put(null, f1);

		commit();

		BtAssertions.assertThat(countSharedFileInA()).isEqualTo(2);
	}

	// #####################################
	// ## . . . . . . HELPERS . . . . . . ##
	// #####################################

	protected SharedFile newSharedFile() {
		return newEntity(SharedFile.T);
	}

	protected long countSharedFileInA() {
		return count(SharedFile.class, smoodA);
	}

	protected long countSharedFileInB() {
		return count(SharedFile.class, smoodB);
	}

	protected SharedFile fileFromA(String name) {
		return selectByProperty(SharedFile.class, "name", name, smoodA);
	}

	protected SharedFile fileFromB(String name) {
		return selectByProperty(SharedFile.class, "name", name, smoodB);
	}

}
