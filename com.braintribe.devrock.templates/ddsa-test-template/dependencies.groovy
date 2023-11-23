import com.braintribe.devrock.templates.model.artifact.CreateBuildSystemConfig;
import com.braintribe.devrock.templates.model.artifact.CreateProjectMetadata;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;
import com.braintribe.devrock.templates.model.Dependency;

def buildSystemConfigRequest = CreateBuildSystemConfig.T.create();
support.mapFromTo(request, buildSystemConfigRequest);
buildSystemConfigRequest.artifactType = 'unit-test';
def dep1 = Dependency.T.create();
dep1.groupId = 'com.braintribe.wire';
dep1.artifactId = 'wire';
def dep2 = Dependency.T.create();
dep2.groupId = 'com.braintribe.gm';
dep2.artifactId = 'simple-service-wirings';
def dep3 = Dependency.T.create();
dep3.groupId = 'com.braintribe.gm';
dep3.artifactId = 'simple-access-wirings';
def dep4 = Dependency.T.create();
dep4.groupId = 'com.braintribe.gm';
dep4.artifactId = 'gm-unit-test-deps';
buildSystemConfigRequest.dependencies = support.distinctDependencies([dep1, dep2, dep3, dep4] + buildSystemConfigRequest.dependencies);

def projectMetadataRequest = CreateProjectMetadata.T.create();
support.mapFromTo(request, projectMetadataRequest);
projectMetadataRequest.projectName = request.artifactId + " - " + request.groupId;
projectMetadataRequest.sourceDirectory = 'src';
if ('eclipse'.equals(request.ide)) {
	projectMetadataRequest.classPathEntries = ["org.eclipse.jdt.launching.JRE_CONTAINER"];
	projectMetadataRequest.builders = ['org.eclipse.jdt.core.javabuilder'];
	projectMetadataRequest.natures  = ['org.eclipse.jdt.core.javanature'];
}

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest);
sourceControlConfigRequest.ignoredFiles = ['/' + projectMetadataRequest.outputDirectory];

[buildSystemConfigRequest, projectMetadataRequest, sourceControlConfigRequest]