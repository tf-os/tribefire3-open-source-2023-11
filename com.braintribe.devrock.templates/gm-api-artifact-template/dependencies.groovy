import com.braintribe.devrock.templates.model.artifact.CreateBuildSystemConfig;
import com.braintribe.devrock.templates.model.artifact.CreateProjectMetadata;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;

def buildSystemConfigRequest = CreateBuildSystemConfig.T.create();
support.mapFromTo(request, buildSystemConfigRequest);
buildSystemConfigRequest.artifactType = 'gm-api';

def projectMetadataRequest = CreateProjectMetadata.T.create();
support.mapFromTo(request, projectMetadataRequest);
projectMetadataRequest.projectName = request.artifactId + " - " + request.groupId;
projectMetadataRequest.sourceDirectory = 'src';
if ('eclipse'.equals(request.ide)) {
	projectMetadataRequest.classPathEntries = ["org.eclipse.jdt.launching.JRE_CONTAINER"];
	projectMetadataRequest.builders = ['com.braintribe.devrock.arb.builder.ArtifactReflectionBuilder', 'org.eclipse.jdt.core.javabuilder', 'com.braintribe.eclipse.model.nature.ModelBuilder'];
	projectMetadataRequest.builderOutputLibs = ["class-gen"];
	projectMetadataRequest.natures  = ['org.eclipse.jdt.core.javanature'];
}

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest);
sourceControlConfigRequest.ignoredFiles = ['/' + projectMetadataRequest.outputDirectory, "class-gen"];

[buildSystemConfigRequest, projectMetadataRequest, sourceControlConfigRequest]
