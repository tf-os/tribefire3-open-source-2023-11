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
package com.braintribe.model.access.smart.test.query2.shared;

import static com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFile.fileDescriptor;
import static com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFile.name;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.access.smart.test.query2._base.AbstractSmartQueryTests;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.query.smart.test2._common.SmartModelTestSetup;
import com.braintribe.model.processing.query.smart.test2.shared.SharedPartiallySmartSetup;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test2.shared.model.shared.SharedFileDescriptor;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public class SharedPartially_Tc_QueryTest extends AbstractSmartQueryTests {

	private SharedFile fA, fB;
	private SharedFileDescriptor fdA, fdB;

	private SharedFile sfA, sfB;
	private SharedFileDescriptor sfdA;

	@Override
	protected SmartModelTestSetup getSmartModelTestSetup() {
		return SharedPartiallySmartSetup.SHARED_PARTIALLY_SETUP;
	}

	@Test
	public void loadAll() {
		generateDelegateEntities();
		runWithCriteria(allTc());
		loadSmartEntities();

		assertThat(sfA.getFileDescriptor()).isSameAs(sfdA);
		assertThat(sfB.getFileDescriptor()).isNull(); // Must be null as unmapped in B
	}

	private TraversingCriterion allTc() {
		return PreparedTcs.everythingTc;
	}

	private void generateDelegateEntities() {
		// B will be present in the delegate, but is unmapped and therefore the property must be null on the smart level
		fdA = bA.make(SharedFileDescriptor.T).done();
		fdB = bB.make(SharedFileDescriptor.T).done();

		fA = bA.make(SharedFile.T).set(name, "fA").set(fileDescriptor, fdA).done();
		fB = bB.make(SharedFile.T).set(name, "fB").set(fileDescriptor, fdB).done();
	}

	private void runWithCriteria(TraversingCriterion tc) {
		smartAccess.setDefaultTraversingCriteria(asMap(GenericEntity.class, tc));

		SelectQuery selectQuery = query() //
				.select("f") //
				.from(SharedFile.T, "f") //
				.done();

		evaluate(selectQuery);
	}

	/**
	 * If we did this before query evaluation, we would lose the AbsenceInformation, as this would create an entity in the session
	 */
	private void loadSmartEntities() {
		sfA = acquireSmart(fA);
		sfdA = acquireSmart(fdA);
		sfB = acquireSmart(fB);

		assertResultContains(sfA);
		assertResultContains(sfB);
		assertNoMoreResults();
	}

}
