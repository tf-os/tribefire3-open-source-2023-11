import com.braintribe.devrock.templates.model.artifact.CreateParent;
import com.braintribe.devrock.templates.model.artifact.CreateGroupBuildScript;
import com.braintribe.devrock.templates.model.artifact.CreateSourceControlConfig;

def parentRequest = CreateParent.T.create();
support.mapFromTo(request, parentRequest);
parentRequest.directoryName = 'parent';

def groupBuildScriptRequest = CreateGroupBuildScript.T.create();
support.mapFromTo(request, groupBuildScriptRequest);
groupBuildScriptRequest.builtArtifactIds = ['parent'];

def sourceControlConfigRequest = CreateSourceControlConfig.T.create();
support.mapFromTo(request, sourceControlConfigRequest, ["buildSystem"]);
sourceControlConfigRequest.ignoredFiles = ['**/.DS_Store', '**/.DS_Store?', '**/.Spotlight-V100', '**/.Trashes', '**/ehthumbs.db', '**/Thumbs.db'];

[parentRequest, groupBuildScriptRequest, sourceControlConfigRequest]