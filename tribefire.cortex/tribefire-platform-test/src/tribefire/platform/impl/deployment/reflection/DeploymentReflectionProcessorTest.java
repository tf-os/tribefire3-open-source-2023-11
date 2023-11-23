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
package tribefire.platform.impl.deployment.reflection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentreflection.DeploymentSummary;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnits;
import com.braintribe.model.deploymentreflection.request.GetDeploymentSummary;
import com.braintribe.model.deploymentreflection.request.GetDeploymentSummaryPlain;
import com.braintribe.model.deploymentreflection.request.UnitFilter;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.logic.TypeConditionConjunction;
import com.braintribe.model.service.api.InstanceId;

/**
 * Tests {@link DeploymentReflectionProcessor}
 * 
 * @author christina.wilpernig
 */
public class DeploymentReflectionProcessorTest extends DeploymentReflectionTestUtils {

	@Test
	public void testCreateDeploymentSummary() {

		DeploymentSummary summary = createTestSummary();
		Map<InstanceId, QualifiedDeployedUnits> units = summary.getUnitsByInstanceId();

		DeploymentReflectionProcessor p = createQualifiedDeploymentReflectionProcessor();
		DeploymentSummary deploymentSummary = p.createDeploymentSummary(units);

		// Units by node
		Map<String, QualifiedDeployedUnits> testUnitsByNode = summary.getUnitsByNode();
		Map<String, QualifiedDeployedUnits> unitsByNode = deploymentSummary.getUnitsByNode();

		assertThat(testUnitsByNode).isNotNull();
		assertThat(unitsByNode).isNotNull();
		assertThat(testUnitsByNode == unitsByNode);

		// Units by cartridge
		Map<String, QualifiedDeployedUnits> testUnitsByCartridge = summary.getUnitsByCartridge();
		Map<String, QualifiedDeployedUnits> unitsByCartridge = deploymentSummary.getUnitsByCartridge();

		assertThat(testUnitsByCartridge).isNotNull();
		assertThat(unitsByCartridge).isNotNull();
		assertThat(testUnitsByCartridge == unitsByCartridge);

		// Units by instance ids
		Map<InstanceId, QualifiedDeployedUnits> testUnitsByInstanceId = summary.getUnitsByInstanceId();
		Map<InstanceId, QualifiedDeployedUnits> unitsByInstanceId = deploymentSummary.getUnitsByInstanceId();

		assertThat(testUnitsByInstanceId).isNotNull();
		assertThat(unitsByInstanceId).isNotNull();
		assertThat(testUnitsByInstanceId == unitsByInstanceId);

		// Units by deployable
		Map<Deployable, QualifiedDeployedUnits> unitsByDeployable = deploymentSummary.getUnitsByDeployable();
		assertThat(unitsByDeployable).isNotNull();

		QualifiedDeployedUnits testTotalUnits = summary.getTotalUnits();
		assertThat(testTotalUnits).isNotNull();

		QualifiedDeployedUnits totalUnits = deploymentSummary.getTotalUnits();
		assertThat(totalUnits).isNotNull();

		assertThat(testTotalUnits.getUnits().size()).isEqualTo(totalUnits.getUnits().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFilter() {
		GetDeploymentSummaryPlain plain = GetDeploymentSummaryPlain.T.create();
		plain.setNodeId("testNode");
		plain.setWireKind("notExisting");
		plain.setTypeSignature("");
		plain.setIsAssignableTo(false);

		DeploymentReflectionContext.createGetDeploymentSummaryRequest(plain);
	}

	@Test
	public void testcreateRequestWithSimpleFilter() {
		GetDeploymentSummaryPlain plain = GetDeploymentSummaryPlain.T.create();
		plain.setNodeId("testNode");
		plain.setWireKind("hardwired");
		plain.setTypeSignature("");
		plain.setIsAssignableTo(false);

		GetDeploymentSummary request = DeploymentReflectionContext.createGetDeploymentSummaryRequest(plain);

		assertThat(request.getMulticastFilter()).isNotNull();
		assertThat(request.getUnitFilter()).isNotNull();

		UnitFilter unitFilter = request.getUnitFilter();

		TypeCondition deployableFilter = unitFilter.getDeployableFilter();
		assertThat(deployableFilter).isInstanceOf(IsAssignableTo.class);

	}

	@Test
	public void testPlainRequestWithMoreComplexFilter() {
		GetDeploymentSummaryPlain plain = GetDeploymentSummaryPlain.T.create();
		plain.setNodeId("testNode");
		plain.setWireKind("hardwired");
		plain.setTypeSignature("com.braintribe.model.generic.typecondition.basic.IsAssignableTo");
		plain.setIsAssignableTo(false);

		GetDeploymentSummary request = DeploymentReflectionContext.createGetDeploymentSummaryRequest(plain);

		assertThat(request.getMulticastFilter()).isNotNull();
		assertThat(request.getUnitFilter()).isNotNull();

		UnitFilter unitFilter = request.getUnitFilter();

		TypeCondition deployableFilter = unitFilter.getDeployableFilter();
		assertThat(deployableFilter).isInstanceOf(TypeConditionConjunction.class);
	}
}
