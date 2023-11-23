import com.braintribe.devrock.templates.model.artifact.CreateJsLibrary;

def createJsLibrary = CreateJsLibrary.T.create();
support.mapFromTo(request, createJsLibrary);

return [createJsLibrary]