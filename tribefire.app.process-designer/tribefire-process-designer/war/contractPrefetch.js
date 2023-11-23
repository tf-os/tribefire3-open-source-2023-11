function log(message) {
    console.log("contractPrefetch: " + message); 
}

class Contract extends $tf.module.TribefireUxModuleContract { 
    bindServiceProcessors(context) {
        //NOP - not used on prefetch
    }

    bindWindow(window) {
        //NOP - not used on prefetch
    }

    createComponent(context, denotation) {
        let appUrl = "";
        let rootUrl;
        let scriptPath = "";
        if (context) {
            log("context: " + context); 
        	if (context.rootUrl) {
        		rootUrl = context.rootUrl;
                log("rootUrl: " + rootUrl);         		
        	}
            if (context.modulePath) {
                appUrl = context.modulePath.replace(/[^\/]*$/, '');  //extract path
                log("appUrl: " + appUrl);
                let path = appUrl.substring(0, appUrl.lastIndexOf("/"));
				scriptPath = path.substring(path.lastIndexOf("/")+1);
                log("scriptPath: " + scriptPath); 
        	}        	
        }
  
		var callbackLoad = function ()
		{
	        log("injectGWT LOADED for ProcessDesigner Component!");				    	
		}

		var callbackError = function ()
		{
	        log("injectGWT ERROR");	        		
		}

        log("check Script externalProcessDesignerScript");
		let elementScript = document.getElementById("externalProcessDesignerScript");
		if (!elementScript) {
        	log("Add Script externalProcessDesignerScript: " + rootUrl + scriptPath + "/BtClientCustomization/BtClientCustomization.nocache.js");		
        	if (rootUrl) 
        		this.injectScript(rootUrl + scriptPath + "/BtClientCustomization/BtClientCustomization.nocache.js", document.head, "externalProcessDesignerScript",callbackLoad, callbackError);
        } 
        
        return null;
    }

    injectScript(src, wrapper, id, callbackLoad, callbackError) {
        let element = document.createElement("script");
        element.id = id;
        element.setAttribute("src", src);
		element.addEventListener('load', callbackLoad);
		element.addEventListener('error', callbackError);
        wrapper.appendChild(element);
    }
}

export default Contract;