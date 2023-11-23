```groovy
import com.braintribe.devrock.templates.model.artifact.CreateParentArtifact;
import com.braintribe.devrock.templates.model.artifact.CreateUnitTestArtifact;
import com.braintribe.devrock.templates.model.artifact.CreateIntegrationTestArtifact;
import tribefire.cortex.assets.templates.model.CreateAssetAggregatorAsset;
import tribefire.cortex.assets.templates.model.CreateCustomCartridgeAsset;
import tribefire.cortex.assets.templates.model.CreateMarkdownDocumentationAsset;
import tribefire.cortex.assets.templates.model.CreateModelPrimingAsset;
import tribefire.cortex.assets.templates.model.CreatePluginPrimingAsset;
import com.braintribe.devrock.templates.processing.model.Dependency;

def parentArtifactId = 'parent';
def cartridgeArtifactId = $request.name.concat('-cartridge');
def dataModelArtifactId = $request.name.concat('-cartridge-data-model');
def deploymentModelArtifactId = $request.name.concat('-cartridge-deployment-model');
def serviceModelArtifactId = $request.name.concat('-cartridge-service-model');
def cortexServiceModelArtifactId = $request.name.concat('-cartridge-cortex-service-model');
def initializerArtifactId = $request.name.concat('-cartridge-initializer');
def documentationArtifactId = $request.name.concat('-cartridge-documentation');
def setupArtifactId = $request.name.concat('-cartridge-setup');
def unitTestArtifactId = $request.name.concat('-cartridge-unit-test');
def integrationTestArtifactId = $request.name.concat('-cartridge-integration-test');

def setAttributesFromMap(Object o, Map<String, Object> map) {
	map.each { key, value ->
		o."${key}" = value
	}
}

def parent = CreateParentArtifact.T.create();
parent.setGroupId($request.groupId);
parent.setVersion($request.version);
def parentAddons = [];
if($request.buildSystem == 'bt-ant') {
	parent.setBuildSystem('bt-ant');
	parentAddons.push('com.braintribe.devrock.templates:parent-bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	parent.setBuildSystem('maven');
	parentAddons.push('com.braintribe.devrock.templates:parent-maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	parent.setIde('eclipse');
	parentAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	parent.setSourceControl('git');
	parentAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
parent.setTemplateAddons(parentAddons);

def cartridge = CreateCustomCartridgeAsset.T.create();
cartridge.setGroupId($request.groupId);
cartridge.setArtifactId(cartridgeArtifactId);
cartridge.setVersion($request.version);
def cartridgeAddons = [];
if($request.buildSystem == 'bt-ant') {
	cartridge.setBuildSystem('bt-ant');
	cartridgeAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	cartridge.setBuildSystem('maven');
	cartridgeAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	cartridge.setIde('eclipse');
	cartridgeAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	cartridge.setSourceControl('git');
	cartridgeAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
cartridge.setTemplateAddons(cartridgeAddons);
def cartridgeDep1 = Dependency.T.create();
setAttributesFromMap(cartridgeDep1, [groupId: $request.groupId, artifactId: dataModelArtifactId, tags: ['asset']]);
def cartridgeDep2 = Dependency.T.create();
setAttributesFromMap(cartridgeDep2, [groupId: $request.groupId, artifactId: deploymentModelArtifactId, tags: ['asset']]);
def cartridgeDep3 = Dependency.T.create();
setAttributesFromMap(cartridgeDep3, [groupId: $request.groupId, artifactId: serviceModelArtifactId, tags: ['asset']]);
def cartridgeDep4 = Dependency.T.create();
setAttributesFromMap(cartridgeDep4, [groupId: $request.groupId, artifactId: cortexServiceModelArtifactId, tags: ['asset']]);
def cartridgeDep5 = Dependency.T.create();
setAttributesFromMap(cartridgeDep5, [groupId: 'tribefire.cortex', artifactId: 'tribefire-cartridge-default-deps', tags: ['asset']]);
cartridge.setDependencies([cartridgeDep1, cartridgeDep2, cartridgeDep3, cartridgeDep4, cartridgeDep5]);

def dataModel = CreateModelPrimingAsset.T.create();
dataModel.setGroupId($request.groupId);
dataModel.setArtifactId(dataModelArtifactId);
dataModel.setVersion($request.version);
def dataModelAddons = [];
if($request.buildSystem == 'bt-ant') {
	dataModel.setBuildSystem('bt-ant');
	dataModelAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	dataModel.setBuildSystem('maven');
	dataModelAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	dataModel.setIde('eclipse');
	dataModelAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	dataModel.setSourceControl('git');
	dataModelAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
dataModel.setTemplateAddons(dataModelAddons);

def deploymentModel = CreateModelPrimingAsset.T.create();
deploymentModel.setGroupId($request.groupId);
deploymentModel.setArtifactId(deploymentModelArtifactId);
deploymentModel.setVersion($request.version);
def deploymentModelAddons = [];
if($request.buildSystem == 'bt-ant') {
	deploymentModel.setBuildSystem('bt-ant');
	deploymentModelAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	deploymentModel.setBuildSystem('maven');
	deploymentModelAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	deploymentModel.setIde('eclipse');
	deploymentModelAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	deploymentModel.setSourceControl('git');
	deploymentModelAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
deploymentModel.setTemplateAddons(deploymentModelAddons);
def deploymentModelDep1 = Dependency.T.create();
setAttributesFromMap(deploymentModelDep1, [groupId: 'com.braintribe.gm', artifactId: 'extension-deployment-model', tags: ['asset']]);
deploymentModel.setDependencies([deploymentModelDep1]);

def serviceModel = CreateModelPrimingAsset.T.create();
serviceModel.setGroupId($request.groupId);
serviceModel.setArtifactId(serviceModelArtifactId);
serviceModel.setVersion($request.version);
def serviceModelAddons = [];
if($request.buildSystem == 'bt-ant') {
	serviceModel.setBuildSystem('bt-ant');
	serviceModelAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	serviceModel.setBuildSystem('maven');
	serviceModelAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	serviceModel.setIde('eclipse');
	serviceModelAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	serviceModel.setSourceControl('git');
	serviceModelAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
serviceModel.setTemplateAddons(serviceModelAddons);
def serviceModelDep1 = Dependency.T.create();
setAttributesFromMap(serviceModelDep1, [groupId: 'com.braintribe.gm', artifactId: 'access-request-model', tags: ['asset']]);
def serviceModelDep2 = Dependency.T.create();
setAttributesFromMap(serviceModelDep2, [groupId: $request.groupId, artifactId: dataModelArtifactId, tags: ['asset']]);
serviceModel.setDependencies([serviceModelDep1, serviceModelDep2]);

def cortexServiceModel = CreateModelPrimingAsset.T.create();
cortexServiceModel.setGroupId($request.groupId);
cortexServiceModel.setArtifactId(cortexServiceModelArtifactId);
cortexServiceModel.setVersion($request.version);
def cortexServiceModelAddons = [];
if($request.buildSystem == 'bt-ant') {
	cortexServiceModel.setBuildSystem('bt-ant');
	cortexServiceModelAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	cortexServiceModel.setBuildSystem('maven');
	cortexServiceModelAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	cortexServiceModel.setIde('eclipse');
	cortexServiceModelAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	cortexServiceModel.setSourceControl('git');
	cortexServiceModelAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
cortexServiceModel.setTemplateAddons(cortexServiceModelAddons);
def cortexServiceModelDep1 = Dependency.T.create();
setAttributesFromMap(cortexServiceModelDep1, [groupId: 'com.braintribe.gm', artifactId: 'access-request-model', tags: ['asset']]);
def cortexServiceModelDep2 = Dependency.T.create();
setAttributesFromMap(cortexServiceModelDep2, [groupId: $request.groupId, artifactId: deploymentModelArtifactId, tags: ['asset']]);
cortexServiceModel.setDependencies([cortexServiceModelDep1, cortexServiceModelDep2]);

def initializer = CreatePluginPrimingAsset.T.create();
initializer.setGroupId($request.groupId);
initializer.setArtifactId(initializerArtifactId);
initializer.setVersion($request.version);
initializer.setAccessId($request.accessId);
def initializerAddons = [];
if($request.buildSystem == 'bt-ant') {
	initializer.setBuildSystem('bt-ant');
	initializerAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	initializer.setBuildSystem('maven');
	initializerAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	initializer.setIde('eclipse');
	initializerAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	initializer.setSourceControl('git');
	initializerAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
initializer.setTemplateAddons(initializerAddons);
def initializerDep1 = Dependency.T.create();
setAttributesFromMap(initializerDep1, [groupId: $request.groupId, artifactId: cartridgeArtifactId, classifier: 'asset', type: 'man', tags: ['asset']]);
def initializerDep2 = Dependency.T.create();
setAttributesFromMap(initializerDep2, [groupId: $request.groupId, artifactId: dataModelArtifactId, tags: ['asset']]);
def initializerDep3 = Dependency.T.create();
setAttributesFromMap(initializerDep3, [groupId: $request.groupId, artifactId: deploymentModelArtifactId, tags: ['asset']]);
def initializerDep4 = Dependency.T.create();
setAttributesFromMap(initializerDep4, [groupId: $request.groupId, artifactId: serviceModelArtifactId, tags: ['asset']]);
def initializerDep5 = Dependency.T.create();
setAttributesFromMap(initializerDep5, [groupId: $request.groupId, artifactId: cortexServiceModelArtifactId, tags: ['asset']]);
def initializerDep6 = Dependency.T.create();
setAttributesFromMap(initializerDep6, [groupId: 'tribefire.cortex', artifactId: 'core-persistence-deployment-model', scope: 'provided', tags: ['asset']]);
def initializerDep7 = Dependency.T.create();
setAttributesFromMap(initializerDep7, [groupId: 'tribefire.cortex', artifactId: 'cortex-deployment-model', scope: 'provided', tags: ['asset']]);
def initializerDep8 = Dependency.T.create();
setAttributesFromMap(initializerDep8, [groupId: 'com.braintribe.gm', artifactId: 'persistence-initializer-support']);
def initializerDep9 = Dependency.T.create();
setAttributesFromMap(initializerDep9, [groupId: 'com.braintribe.gm', artifactId: 'persistence-initializer-provided-default-deps', scope: 'provided']);
initializer.setDependencies([initializerDep1, initializerDep2, initializerDep3, initializerDep4, initializerDep5, initializerDep6, initializerDep7, initializerDep8, initializerDep9]);

def documentation = CreateMarkdownDocumentationAsset.T.create();
documentation.setGroupId($request.groupId);
documentation.setArtifactId(documentationArtifactId);
documentation.setVersion($request.version);
def documentationAddons = [];
if($request.buildSystem == 'bt-ant') {
	documentation.setBuildSystem('bt-ant');
	documentationAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	documentation.setBuildSystem('maven');
	documentationAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	documentation.setIde('eclipse');
	documentationAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	documentation.setSourceControl('git');
	documentationAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
documentation.setTemplateAddons(documentationAddons);

def setup = CreateAssetAggregatorAsset.T.create();
setup.setGroupId($request.groupId);
setup.setArtifactId(setupArtifactId);
setup.setVersion($request.version);
def setupAddons = [];
if($request.buildSystem == 'bt-ant') {
	setup.setBuildSystem('bt-ant');
	setupAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	setup.setBuildSystem('maven');
	setupAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	setup.setIde('eclipse');
	setupAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	setup.setSourceControl('git');
	setupAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
setup.setTemplateAddons(setupAddons);
def setupDep1 = Dependency.T.create();
setAttributesFromMap(setupDep1, [groupId: 'tribefire.cortex.assets', artifactId: 'tribefire-standard-aggregator', classifier: 'asset', type: 'man', tags: ['asset']]);
def setupDep2 = Dependency.T.create();
setAttributesFromMap(setupDep2, [groupId: $request.groupId, artifactId: initializerArtifactId, classifier: 'asset', type: 'man', tags: ['asset']]);
setup.setDependencies([setupDep1, setupDep2]);
	
def unitTest = CreateUnitTestArtifact.T.create();
unitTest.setGroupId($request.groupId);
unitTest.setArtifactId(unitTestArtifactId);
unitTest.setVersion($request.version);
def unitTestAddons = [];
if($request.buildSystem == 'bt-ant') {
	unitTest.setBuildSystem('bt-ant');
	unitTestAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	unitTest.setBuildSystem('maven');
	unitTestAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	unitTest.setIde('eclipse');
	unitTestAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	unitTest.setSourceControl('git');
	unitTestAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
unitTest.setTemplateAddons(unitTestAddons);
def unitTestDep1 = Dependency.T.create();
setAttributesFromMap(unitTestDep1, [groupId: $request.groupId, artifactId: cartridgeArtifactId, classifier: 'classes']);
unitTest.setDependencies([unitTestDep1]);

def integrationTest = CreateIntegrationTestArtifact.T.create();
integrationTest.setGroupId($request.groupId);
integrationTest.setArtifactId(integrationTestArtifactId);
integrationTest.setVersion($request.version);
def integrationTestAddons = [];
if($request.buildSystem == 'bt-ant') {
	integrationTest.setBuildSystem('bt-ant');
	integrationTestAddons.push('com.braintribe.devrock.templates:bt-ant-build-system-addon#1.0');
} else if($request.buildSystem == 'maven') {
	integrationTest.setBuildSystem('maven');
	integrationTestAddons.push('com.braintribe.devrock.templates:maven-build-system-addon#1.0');
}
if($request.ide == 'eclipse') {
	integrationTest.setIde('eclipse');
	integrationTestAddons.push('com.braintribe.devrock.templates:eclipse-project-metadata-addon#1.0');
}
if($request.sourceControl == 'git') {
	integrationTest.setSourceControl('git');
	integrationTestAddons.push('com.braintribe.devrock.templates:git-config-addon#1.0');
}
integrationTest.setTemplateAddons(integrationTestAddons);
def integrationTestDep1 = Dependency.T.create();
setAttributesFromMap(integrationTestDep1, [groupId: $request.groupId, artifactId: dataModelArtifactId, tags: ['asset']]);
def integrationTestDep2 = Dependency.T.create();
setAttributesFromMap(integrationTestDep2, [groupId: $request.groupId, artifactId: deploymentModelArtifactId, tags: ['asset']]);
def integrationTestDep3 = Dependency.T.create();
setAttributesFromMap(integrationTestDep3, [groupId: $request.groupId, artifactId: serviceModelArtifactId, tags: ['asset']]);
def integrationTestDep4 = Dependency.T.create();
setAttributesFromMap(integrationTestDep4, [groupId: $request.groupId, artifactId: cortexServiceModelArtifactId, tags: ['asset']]);
integrationTest.setDependencies([integrationTestDep1, integrationTestDep2, integrationTestDep3, integrationTestDep4]);

def requests = [parent, cartridge, dataModel, deploymentModel, serviceModel, cortexServiceModel, initializer, documentation, setup, unitTest, integrationTest];
```