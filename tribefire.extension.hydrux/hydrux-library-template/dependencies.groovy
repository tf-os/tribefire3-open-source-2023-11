import com.braintribe.devrock.templates.model.artifact.CreateTsLibrary;
import com.braintribe.devrock.templates.model.Dependency;

def hxApiDep = Dependency.T.create();
hxApiDep.groupId = 'tribefire.extension.hydrux';
hxApiDep.artifactId = 'hydrux-api';

def createTsLibrary = CreateTsLibrary.T.create();
support.mapFromTo(request, createTsLibrary);
createTsLibrary.dependencies = support.distinctDependencies([hxApiDep] + createTsLibrary.dependencies);

return [createTsLibrary]