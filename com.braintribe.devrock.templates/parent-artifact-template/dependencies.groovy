import com.braintribe.devrock.templates.model.artifact.CreateBuildSystemConfig;
import com.braintribe.devrock.templates.model.artifact.CreateProjectMetadata;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;
import com.braintribe.devrock.templates.model.Property;

def javaVersion = '1.17';
def buildSystemConfigRequest = CreateBuildSystemConfig.T.create();
support.mapFromTo(request, buildSystemConfigRequest);
buildSystemConfigRequest.artifactType = 'parent';
def props = [];
if ('bt-ant'.equals(request.buildSystem)) {
	buildSystemConfigRequest.packaging = 'pom';
	def javaVersionProp = Property.T.create();
	javaVersionProp.name = 'java.version';
	javaVersionProp.value = javaVersion;
	props += [javaVersionProp];
} else if ('maven'.equals(request.buildSystem)) {
	buildSystemConfigRequest.packaging = 'pom';
	def mavenCompilerTargetProp = Property.T.create();
	mavenCompilerTargetProp.name = 'maven.compiler.target';
	mavenCompilerTargetProp.value = javaVersion;
	def mavenCompilerSourceProp = Property.T.create();
	mavenCompilerSourceProp.name = 'maven.compiler.source';
	mavenCompilerSourceProp.value = javaVersion;
	def javaVersionProp = Property.T.create();
	javaVersionProp.name = 'java.version';
	javaVersionProp.value = javaVersion;
	props += [mavenCompilerTargetProp, mavenCompilerSourceProp, javaVersionProp];
}
def versionProp = Property.T.create();
versionProp.name = 'V.' + request.groupId;
versionProp.value = '[1.0,1.1)';
props += [versionProp];
buildSystemConfigRequest.properties = support.distinctProperties(props + buildSystemConfigRequest.properties);
buildSystemConfigRequest.managedDependencies = request.dependencies;
buildSystemConfigRequest.dependencies = [];

def projectMetadataRequest = CreateProjectMetadata.T.create();
support.mapFromTo(request, projectMetadataRequest);
projectMetadataRequest.projectName = request.artifactId + " - " + request.groupId;

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest);

[buildSystemConfigRequest, projectMetadataRequest, sourceControlConfigRequest]