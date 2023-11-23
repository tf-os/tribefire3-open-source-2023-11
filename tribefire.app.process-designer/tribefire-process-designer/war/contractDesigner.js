import GenericView from "./contentViewDesigner.js";

function log(message) {
  console.log("contractDesigner: " + message);
}

class Contract extends $tf.module.TribefireUxModuleContract {
  bindServiceProcessors(context) {
    //NOP
  }

  bindWindow(window) {
    //NOP
  }

  createComponentAsync(context, denotation) {
    let future = new $tf.session.Future();
    let appUrl = "";
    let rootUrl;
    let scriptPath = "";
    let accessId = "";
    if (context) {
      if (context.rootUrl)
        rootUrl = context.rootUrl;

      if (context.modulePath) {
        appUrl = context.modulePath.replace(/[^\/]*$/, ""); //extract path
        let path = appUrl.substring(0, appUrl.lastIndexOf("/"));
        scriptPath = path.substring(path.lastIndexOf("/") + 1);
      }
      if (context.accessId)
        accessId = context.accessId;
    }

    var callbackLoad = function () {
      log("injectGWT LOADED for ProcessDesigner Component!");
      setTimeout(() => {
        //Create ProcessDesigner Component Factory
        let factory = new $pd.module.UxComponentFactory();

        let viewWidget;
        try {
          //Get ProcessDesigner Widget
          viewWidget = factory.provideComponent("JsProcessDesignerPanel");

          //Create JavaScript GmContentView Wrapper
          const view = new GenericView(viewWidget, accessId);
          future.onSuccess(view);
        } catch (err) {
          log("ERROR factory.provideComponent: " + err);
          future.onError(null);
        }
      }, 3500);
    };

    var callbackError = function () {
      log("injectGWT ERROR");
      future.onError(null);
    };

    if (rootUrl)
        scriptPath = rootUrl + scriptPath;

    let scriptFile =
      scriptPath + "/BtClientCustomization/BtClientCustomization.nocache.js";

    let elementScript = document.getElementById("externalProcessDesignerScript");
    if (!elementScript) {
      this.injectScript(
        scriptFile,
        document.head,
        "externalProcessDesignerScript",
        callbackLoad,
        callbackError
      );
      return future;
    }

    let factory;
    try {
      factory = new $pd.module.UxComponentFactory();
    } catch (error) {
      log("Script externalProcessDesignerScript Error: " + error);
      log("Script externalProcessDesignerScript Try Inject Script Again");
      this.injectScript(
        scriptFile,
        document.head,
        "externalProcessDesignerScript",
        callbackLoad,
        callbackError
      );
      return future;
    }

    let viewWidget = factory.provideComponent("JsProcessDesignerPanel");

    //need create GmContentView wrapper
    const view = new GenericView(viewWidget, accessId);
    future.onSuccess(view);

    return future;
  }

  createComponent(context, denotation) {
    //NOP - For ProcessDesigner needed Async version
    return null;
  }

  injectScript(src, wrapper, id, callbackLoad, callbackError) {
    let element = document.createElement("script");
    element.id = id;
    element.setAttribute("src", src);
    element.addEventListener("load", callbackLoad);
    element.addEventListener("error", callbackError);
    wrapper.appendChild(element);
  }

  injectScriptAdditional(src, wrapper, id) {
    var element = document.createElement("script");
    if (id)
        element.id = id;
    wrapper.appendChild(element);
    element.setAttribute("src", src);
  }

  injectLink(id, src, type, rel) {
    var element = document.createElement("link");
    if (id != "")
        element.setAttribute("id", id);
    element.setAttribute("href", src);
    element.setAttribute("type", type);
    element.setAttribute("rel", rel);
    document.head.appendChild(element);
  }
}

export default Contract;
