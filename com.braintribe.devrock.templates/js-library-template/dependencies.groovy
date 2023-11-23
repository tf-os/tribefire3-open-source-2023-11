import com.braintribe.devrock.templates.model.artifact.CreateBuildSystemConfig;
import com.braintribe.devrock.templates.model.artifact.CreateProjectMetadata;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;

def buildSystemConfigRequest = CreateBuildSystemConfig.T.create();
support.mapFromTo(request, buildSystemConfigRequest);
buildSystemConfigRequest.artifactType = 'js-library';

def projectMetadataRequest = CreateProjectMetadata.T.create();
support.mapFromTo(request, projectMetadataRequest);
projectMetadataRequest.projectName = request.artifactId + " - " + request.groupId;
projectMetadataRequest.sourceDirectory = 'src';
if ('eclipse'.equals(request.ide)) {
	projectMetadataRequest.builders = ['org.eclipse.wst.validation.validationbuilder'];
	projectMetadataRequest.natures = ['org.eclipse.wst.jsdt.core.jsNature'];
}

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest);
sourceControlConfigRequest.ignoredFiles = ['/' + projectMetadataRequest.outputDirectory];

[buildSystemConfigRequest, projectMetadataRequest, sourceControlConfigRequest]