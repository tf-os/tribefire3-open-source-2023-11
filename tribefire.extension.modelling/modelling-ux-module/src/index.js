/// <reference path="../com.braintribe.gm.gwt.gm-view-api-1.0~/gm-view-api.d.ts"/>
/// <reference path="../tribefire.extension.modelling.modelling-project-model-1.0~/modelling-project-model.d.ts"/>
/// <reference path="../tribefire.extension.modelling.modelling-deployment-model-1.0~/modelling-deployment-model.d.ts"/>
var deploymentModel = $T.tribefire.extension.modelling.model.deployment;
class ModellingUxModuleContract extends $tf.module.TribefireUxModuleContract {
    createComponent(context, denotation) {
        if (deploymentModel.Modeller.isInstance(denotation)) {
            // create component
            return new Modeller();
        }
    }
}
class Modeller extends $tf.view.AbstractGmContentView {
    // constructor
    constructor() {
        super();
        this.mainElement = document.createElement("div");
    }
    setContent(modelPath) {
        let pathEl = modelPath.last();
        let diagram = pathEl.getValue();
        var text = document.createTextNode(diagram.model.name);
        this.mainElement.appendChild(text);
    }
    getUxElement() {
        return this.mainElement;
    }
}
export let contract = new ModellingUxModuleContract();
