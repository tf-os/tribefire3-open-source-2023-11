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

import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentreflection.DeployedComponent;
import com.braintribe.model.deploymentreflection.DeployedUnit;
import com.braintribe.model.deploymentreflection.DeploymentStatus;
import com.braintribe.model.deploymentreflection.DeploymentSummary;
import com.braintribe.model.deploymentreflection.InstanceDescriptor;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnit;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnits;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.wire.api.util.Sets;

public abstract class DeploymentReflectionTestUtils {
	
	protected Supplier<Set<String>> getRolesProvider() {
		Set<String> roles = Sets.set("tf-admin", "tf-internal", "tf-reflect");
		Supplier<Set<String>> rolesProvider = () -> roles;
		
		return rolesProvider;
	}
	
	
	protected DeploymentSummary createTestSummary() {
		DeploymentSummary sum = DeploymentSummary.T.create();
		
		Map<String, QualifiedDeployedUnits> unitsByNode = new IdentityHashMap<>();
		Map<String,QualifiedDeployedUnits> unitsByCartridge = new IdentityHashMap<>();
//		Map<Deployable,QualifiedDeployedUnits> unitsByDeployable = new IdentityHashMap<>(); // TODO check that
		Map<InstanceId, QualifiedDeployedUnits> unitsByInstance = new IdentityHashMap<>();
		
		QualifiedDeployedUnits totalUnits = QualifiedDeployedUnits.T.create();
		
		fillMap(true, "nodeOne", 1, unitsByNode, totalUnits, unitsByInstance);
		fillMap(true, "nodeTwo", 2, unitsByNode, totalUnits,unitsByInstance);
		fillMap(true, "nodeThree", 3, unitsByNode, totalUnits, unitsByInstance);

		fillMap(false, "appFour", 4, unitsByCartridge, totalUnits, unitsByInstance);
		fillMap(false, "appFive", 1, unitsByCartridge, totalUnits, unitsByInstance);
		fillMap(false, "appSix", 2, unitsByCartridge, totalUnits, unitsByInstance);
		
		sum.setUnitsByCartridge(unitsByCartridge);
		sum.setUnitsByNode(unitsByNode);
		sum.setUnitsByInstanceId(unitsByInstance);
		sum.setTotalUnits(totalUnits);
		
		return sum;
	}

	private void fillMap(boolean isUnitNode, String componentId, int amountDeployedUnits, Map<String, QualifiedDeployedUnits> unitsByComponent,
			QualifiedDeployedUnits totalUnits, Map<InstanceId, QualifiedDeployedUnits> unitsByInstance) {
		
		InstanceId instance = null;
		if(isUnitNode) {
			instance = createTestNodeInstanceId(componentId);
		} else {
			instance = createTestCartridgeInstanceId(componentId);
		}
		
		Set<DeployedUnit> units = createReflectionDeployedUnits(amountDeployedUnits);
		Set<QualifiedDeployedUnit> qualUnits = new HashSet<>();
		
		for(DeployedUnit u : units) {
			QualifiedDeployedUnit unit = QualifiedDeployedUnit.T.create();
			unit.setDeployedUnit(u);
			unit.setInstanceId(instance);
			
			qualUnits.add(unit);
			totalUnits.getUnits().add(unit);
		}
		
		QualifiedDeployedUnits qualifiedUnits = QualifiedDeployedUnits.T.create();
		qualifiedUnits.setUnits(qualUnits);
		
		/**
		 * either node-id or cartridge-id
		 */
		unitsByComponent.put(componentId, qualifiedUnits);
		
		unitsByInstance.put(instance, qualifiedUnits);
	}


	protected InstanceId createTestNodeInstanceId(String nodeId) {
		return createTestInstanceId(null, nodeId);
	}
	
	protected InstanceId createTestCartridgeInstanceId(String applicationId) {
		return createTestInstanceId(applicationId, null);
	}
	
	protected DeploymentStatus createDeploymentStatus() {
		DeploymentStatus status = DeploymentStatus.T.create();
		
		status.setUnits(createReflectionDeployedUnits(2));
		
		return status;
	}
	
	protected InstanceId createTestInstanceId(String applicationId, String nodeId) {
		InstanceId instanceId = InstanceId.T.create();
		instanceId.setApplicationId(applicationId);
		instanceId.setNodeId(nodeId);
		return instanceId;
	}
	
	protected Deployable createDeployable(String name, String externalId) {
		TestDeployable d = TestDeployable.T.create();
		d.setExternalId(externalId);
		d.setName(name);
		d.setAutoDeploy(true);
		return d;
	}
	
	public interface TestDeployable extends Deployable {
		final EntityType<TestDeployable> T = EntityTypes.T(TestDeployable.class);
	}
	
	protected Set<DeployedUnit> createReflectionDeployedUnits(int amount) {
		Set<DeployedUnit> units = new HashSet<DeployedUnit>();
		
		for(int i=0; i<amount; i++) {
			units.add(createReflectionDeployedUnit(true));
		}
		
		return units;
	}
	
	protected DeployedUnit createReflectionDeployedUnit(boolean isHardwired) {
		DeployedUnit unit = DeployedUnit.T.create();
		
		unit.setIsHardwired(isHardwired);
		
		UUID uuid = UUID.randomUUID();
		String uid = uuid.toString();
		
		unit.setDeployable(createDeployable("deployable-"+uid, "deployable."+uid));
		unit.setComponents(createComponents(2));
		
		return unit;
	}

	private Set<DeployedComponent> createComponents(int amount) {
		Set<DeployedComponent> comps = new HashSet<DeployedComponent>();
		
		for(int i=0; i<amount; i++) {
			DeployedComponent component = DeployedComponent.T.create();
			UUID idOne = UUID.randomUUID();
			String id1 = idOne.toString();
			component.setComponentBinder(createInstanceDescriptor(id1+i, "beanName"+i, "beanSpace"+i));
			
			UUID idTwo = UUID.randomUUID();
			String id2 = idTwo.toString();
			component.setSuppliedImplementation(createInstanceDescriptor(id2+(i+2), "beanName"+(i+2), "beanSpace"+(i+2)));
			component.setComponentType("componentType"+i);
			
			comps.add(component);
		}
		
		return comps;
	}
	
	protected InstanceDescriptor createInstanceDescriptor(String identityHint, String beanName, String beanSpace) {
		InstanceDescriptor descriptor = InstanceDescriptor.T.create();
		
		descriptor.setIdentityHint(identityHint);
		descriptor.setBeanName(beanName);
		descriptor.setBeanSpace(beanSpace);
		
		return descriptor;
	}
	
	protected DeploymentReflectionProcessor createQualifiedDeploymentReflectionProcessor() {
		DeploymentReflectionProcessor p = new DeploymentReflectionProcessor();
		p.setAllowedRoles(new HashSet<String>(Arrays.asList("tf-invalid")));
		p.setUserRolesProvider(getRolesProvider());
		return p;
	}
	
}
