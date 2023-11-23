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
package com.braintribe.model.access.smood.collaboration.distributed.parallel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.DcsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.distributed.model.DcsaEntity;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class ParallelDcsaTest extends AbstractParallelDcsaTestBase {

	private static final int MERGE_FACTOR = 5;
	private static final int QUERY_FACTOR = 4;

	private final AtomicLong mergeCounter = new AtomicLong(1L);
	private final AtomicLong createQueryCounter = new AtomicLong(1L);
	private final AtomicLong editQueryCounter = new AtomicLong(1L);

	private boolean doMergeWhileCreate = false;
	private boolean doQueryWhileCreateAndEdit = false;

	// @formatter:off
	private static final int MAX_QUERY_RESULT_SIZE = 13; // 1, 10-19, 21, 31
	private static final SelectQuery query = new SelectQueryBuilder()
			.select("e")
			.from(DcsaEntity.T, "e")
			.where()
				.property("e", "name").like("*1*")
			.done();
	// @formatter:on

	// #######################################################
	// ## . . . . . . . . . . . Tests . . . . . . . . . . . ##
	// #######################################################

	@Test
	public void createUpdateOnly() throws Exception {
		runTest(false, false);
	}

	@Test
	public void createUpdate_WithQueries() throws Exception {
		runTest(false, true);
	}

	@Test
	public void createUpdate_WithMerges() throws Exception {
		runTest(true, false);
	}

	@Test
	public void createUpdate_WithMergesAndQueries() throws Exception {
		runTest(true, true);
	}

	private void runTest(boolean doMerges, boolean doQueries) throws Exception {
		doMergeWhileCreate = doMerges;
		doQueryWhileCreateAndEdit = doQueries;

		createEntities();
		checkAllUnitsHaveSameFiles();

		editEntities();
		checkAllUnitsHaveSameFiles();

		mergeTrunks();
		checkAllUnitsHaveSameFiles();
	}

	// #######################################################
	// ## . . . . . . . . . Creation phase . . . . . . . . .##
	// #######################################################

	private void createEntities() {
		// create entities by always doing a create on a random node
		forEachEntity_InParallel(this::createOrMergeOnRandomNode);

		// check every node has all the entities
		forEachNode_InParallel(this::checkHasAllEntities);
	}

	private void createOrMergeOnRandomNode(int entityNumber) {
		handleMergeIfNeeded();
		handleQueryIfNeeded_Create();

		PersistenceGmSession session = dcsaCluster.randomSession();

		DcsaEntity entity = session.create(DcsaEntity.T);
		entity.setName(newEntityName(entityNumber));

		session.commit();
	}

	private void handleMergeIfNeeded() {
		if (doMergeWhileCreate && mergeCounter.getAndIncrement() % MERGE_FACTOR == 0)
			mergeTrunkOnRandomNode();
	}

	private void handleQueryIfNeeded_Create() {
		if (doQueryWhileCreateAndEdit && createQueryCounter.getAndIncrement() % QUERY_FACTOR == 0)
			queryOnRandomNode_Create();
	}

	private void queryOnRandomNode_Create() {
		List<?> entities = dcsaCluster.randomUnit().csa.query(query).getResults();

		Assertions.assertThat(entities.size()).isLessThanOrEqualTo(MAX_QUERY_RESULT_SIZE);
	}

	private void checkHasAllEntities(DcsaDeployedUnit dcsaUnit) {
		Map<String, DcsaEntity> entityByName = getAllEntitiesByName(dcsaUnit.newSession());

		for (int i = 0; i < ENTITY_COUNT; i++)
			assertThat(entityByName).containsKey(newEntityName(i));
	}

	private String newEntityName(int entityNumber) {
		return "Dcsa-" + entityNumber;
	}

	// #######################################################
	// ## . . . . . . . . . Updating phase . . . . . . . . .##
	// #######################################################

	private void editEntities() {
		// updates entities, each on a random node
		forEachEntity_InParallel(this::updateEntityOnRandomNode);

		// check every node has updated entities
		forEachNode_InParallel(this::checkHasAllEntityUpdates);
	}

	private void updateEntityOnRandomNode(int entityNumber) {
		handleQueryIfNeeded_Edit();

		PersistenceGmSession session = dcsaCluster.randomSession();

		DcsaEntity entity = findEntityByName(session, newEntityName(entityNumber));
		entity.setName(updatedEntityName(entityNumber));

		session.commit();
	}

	private void handleQueryIfNeeded_Edit() {
		if (doQueryWhileCreateAndEdit && editQueryCounter.getAndIncrement() % QUERY_FACTOR == 0)
			queryOnRandomNode_Edit();
	}

	private void checkHasAllEntityUpdates(DcsaDeployedUnit dcsaUnit) {
		Map<String, DcsaEntity> entityByName = getAllEntitiesByName(dcsaUnit.newSession());

		for (int i = 0; i < ENTITY_COUNT; i++)
			assertThat(entityByName).containsKey(updatedEntityName(i));
	}

	private String updatedEntityName(int entityNumber) {
		return "Updated-Dcsa-" + entityNumber;
	}

	private void mergeTrunks() {
		forEachNode_InParallel(this::mergeTrunkOnNode);
	}

	private void mergeTrunkOnRandomNode() {
		mergeTrunkOnNode(dcsaCluster.randomUnit());
	}

	private void queryOnRandomNode_Edit() {
		List<?> entities = dcsaCluster.randomUnit().csa.query(query).getResults();

		Assertions.assertThat(entities).hasSize(MAX_QUERY_RESULT_SIZE);
	}

	private void mergeTrunkOnNode(DcsaDeployedUnit dcsaUnit) {
		mergeStage(dcsaUnit, "trunk", "stage0");
	}

	// #######################################################
	// ## . . . . Assert All Units Are Same On Disk . . . . ##
	// #######################################################

	private void checkAllUnitsHaveSameFiles() {
		for (int i = 1; i < CLUSTER_SIZE; i++)
			checkHasDataFilesSameAsUnit0(i);
	}

	private void checkHasDataFilesSameAsUnit0(int n) {
		DcsaDeployedUnit unit0 = dcsaCluster.unit(0);
		DcsaDeployedUnit unitN = dcsaCluster.unit(n);

		List<File> files0 = getFiles(unit0);
		List<File> filesN = getFiles(unitN);

		compareFileLists("TEST FAILED. Difference found between files of unit 0  and " + n + ".", files0, filesN);
	}

	private void compareFileLists(String errorPrefix, List<File> files1, List<File> files2) {
		if (files1.size() != files2.size())
			throw new RuntimeException(errorPrefix + " Number of files doesn't match. File lists are: " + files1 + " and: " + files2);

		Iterator<File> it1 = files1.iterator();
		Iterator<File> it2 = files2.iterator();

		while (it1.hasNext()) {
			File f1 = it1.next();
			File f2 = it2.next();

			Assertions.assertThat(f1.exists()).as(errorPrefix).isSameAs(f2.exists());

			if (f1.exists())
				Assertions.assertThat(f1).as(errorPrefix).hasSameBinaryContentAs(f2);
		}
	}

	private List<File> getFiles(DcsaDeployedUnit dcsaUnit) {
		return dcsaUnit.configuration.getInitializers().stream() //
				.filter(this::isManInitializer) //
				.map(si -> (ManInitializer) si) //
				.map(ManInitializer::getName) //
				.map(dcsaUnit::stageManFile) //
				.collect(Collectors.toList());
	}

	private boolean isManInitializer(SmoodInitializer si) {
		return si instanceof ManInitializer;
	}

	@Override
	protected CollaborativeSmoodConfiguration prepareNewConfiguration() {
		return configForStages("stage0", "trunk");
	}
}
