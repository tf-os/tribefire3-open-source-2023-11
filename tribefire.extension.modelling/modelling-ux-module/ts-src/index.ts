/// <reference path="../com.braintribe.gm.gwt.gm-view-api-1.0~/gm-view-api.d.ts"/>
/// <reference path="../tribefire.extension.modelling.modelling-project-model-1.0~/modelling-project-model.d.ts"/>
/// <reference path="../tribefire.extension.modelling.modelling-deployment-model-1.0~/modelling-deployment-model.d.ts"/>

import deploymentModel = $T.tribefire.extension.modelling.model.deployment;
import projectModel = $T.tribefire.extension.modelling.model.diagram;

class ModellingUxModuleContract extends $tf.module.TribefireUxModuleContract {
    createComponent(context: $tf.module.ComponentCreateContext, denotation: $T.tribefire.extension.js.model.deployment.JsUxComponent): Modeller {
        if (deploymentModel.Modeller.isInstance(denotation)) {
            // create component
            return new Modeller();
        }
    }
}

class Modeller extends $tf.view.AbstractGmContentView {

    private mainElement: HTMLElement;

    // constructor
    constructor() {
        super();
        this.mainElement = document.createElement("div");
    }

    setContent(modelPath: $tf.modelpath.ModelPath): void {
        let pathEl = modelPath.last();
        let diagram = pathEl.getValue() as projectModel.ModellingDiagram;
        var text = document.createTextNode(diagram.model.name);
        this.mainElement.appendChild(text);
    }

    getUxElement(): HTMLElement {
        return this.mainElement;
    }

    
}

export let contract = new ModellingUxModuleContract();
