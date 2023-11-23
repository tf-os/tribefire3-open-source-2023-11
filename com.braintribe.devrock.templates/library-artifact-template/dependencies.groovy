import com.braintribe.devrock.templates.model.Dependency;
import com.braintribe.devrock.templates.model.artifact.CreateBuildSystemConfig;
import com.braintribe.devrock.templates.model.artifact.CreateProjectMetadata;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;
import com.braintribe.devrock.templates.model.artifact.CreateUnitTest;
import com.braintribe.devrock.templates.model.artifact.CreateServiceTest;

def buildSystemConfigRequest = CreateBuildSystemConfig.T.create();
support.mapFromTo(request, buildSystemConfigRequest);
buildSystemConfigRequest.artifactType = 'library';

def projectMetadataRequest = CreateProjectMetadata.T.create();
support.mapFromTo(request, projectMetadataRequest);
projectMetadataRequest.projectName = request.artifactId + " - " + request.groupId;
projectMetadataRequest.sourceDirectory = 'src';
if ('eclipse'.equals(request.ide)) {
	projectMetadataRequest.classPathEntries = ["org.eclipse.jdt.launching.JRE_CONTAINER"];
	projectMetadataRequest.builders = ['com.braintribe.devrock.arb.builder.ArtifactReflectionBuilder', 'org.eclipse.jdt.core.javabuilder'];
	projectMetadataRequest.builderOutputLibs = ["class-gen"];
	projectMetadataRequest.natures  = ['org.eclipse.jdt.core.javanature'];
}

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest);
sourceControlConfigRequest.ignoredFiles = ['/' + projectMetadataRequest.outputDirectory, "class-gen"];

def testRequest = null;
if ('plain'.equalsIgnoreCase(request.test)) {
	testRequest = CreateUnitTest.T.create();
} else if ('service'.equalsIgnoreCase(request.test)) {
	testRequest = CreateServiceTest.T.create();
} else if (request.test !=  null) {
	throw new RuntimeException("Unknown test option specified. Only 'plain' and 'service' (case insensitive) options are available.");
}

def requests = [buildSystemConfigRequest, projectMetadataRequest, sourceControlConfigRequest];

if (testRequest != null) {
	support.mapFromTo(request, testRequest);
	testRequest.artifactId = testRequest.artifactId + '-test';
	testRequest.directoryName = testRequest.artifactId;
	def libDep = Dependency.T.create();
	libDep.groupId = request.groupId;
	libDep.artifactId = request.artifactId;
	testRequest.dependencies += libDep;
	requests += testRequest;
}

requests
