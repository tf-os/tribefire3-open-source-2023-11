import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";

import { HxDemoThrowException } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";

export function bindErrors(context: hx.IHxModuleBindingContext): void {
    context.componentBinder().bindView(hxDemoDepM.HxDemoErrorsView, createErrorsView);
}

async function createErrorsView(denotation: hxDemoDepM.HxDemoErrorsView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const evaluator = context.application()
        .getEvaluator()
        .setDefaultDomain('hx-demo-services')
        .build();

    const app = new ErrorsApp(denotation, evaluator);

    return hx.IHxTools.view(() => app.div);
}

class ErrorsApp {

    readonly #denotation: hxDemoDepM.HxDemoErrorsView;
    readonly #evaluator: tf.eval_.Evaluator<ServiceRequest>;

    public div: HTMLDivElement;

    constructor(denotation: hxDemoDepM.HxDemoErrorsView, evaluator: $tf.eval.Evaluator<ServiceRequest>) {
        this.#denotation = denotation;
        this.#evaluator = evaluator;

        this.initDivs();
    }

    private initDivs(): void {
        const div = document.createElement("div");
        div.classList.add("hx-demo-errors", "main")
        div.style.padding = "10px";
        div.innerHTML =
            '<div style="padding: 10px 0 20px 0">' +
            'Hydrux can show you all unhandles errors, which you\'d otherwise only see in the console.<br/><br/>' +
            'Make sure to add <span style="background-color: #555; padding: 4px">?hxdebug=true</span> to your URL.' +
            '</div>' +
            '<button id="serverErrorButton" type="button" class="button">Server Exception!</button>' +
            '<button id="clientErrorButton" type="button" class="button">Client Exception!</button>' +
            '<button id="clientPromiseErrorButton" type="button" class="button">Client Promise Exception!</button>';

        this.div = div;

        const serverErrorButton = div.querySelector("#serverErrorButton");
        const clientErrorButton = div.querySelector("#clientErrorButton");
        const clientPromiseErrorButton = div.querySelector("#clientPromiseErrorButton");

        serverErrorButton.addEventListener("click", () => this.onServerErrorClick())
        clientErrorButton.addEventListener("click", () => this.onClientErrorClick())
        clientPromiseErrorButton.addEventListener("click", () => this.onClientPromiseErrorClick())
    }

    private async onServerErrorClick(): Promise<void> {
        const request = HxDemoThrowException.create();
        await request.EvalAndGet(this.#evaluator);
    }

    private onClientErrorClick(): void {
        const request = HxDemoThrowException.create();
        request.EvalAndGet(null);
    }

    private async onClientPromiseErrorClick(): Promise<void> {
        const request = HxDemoThrowException.create();
        await request.EvalAndGet(null);
    }
}

