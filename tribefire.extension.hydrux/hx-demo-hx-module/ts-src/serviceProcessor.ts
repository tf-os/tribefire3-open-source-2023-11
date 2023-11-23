import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";

import { HxDemoComputeTextLength } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";

export function bindServiceProcessor(context: hx.IHxModuleBindingContext): void {
    context.componentBinder().bindView(hxDemoDepM.HxDemoServiceProcessorView, createServiceProcessorView);
}

async function createServiceProcessorView(denotation: hxDemoDepM.HxDemoServiceProcessorView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const evaluator = context.application()
        .getEvaluator()
        .setDefaultDomain('hx-demo-services')
        .build();

    const app = new ServiceApp(denotation, evaluator);

    return hx.IHxTools.view(() => app.div);
}

class ServiceApp {

    private readonly denotation: hxDemoDepM.HxDemoServiceProcessorView;
    private readonly evaluator: tf.eval_.Evaluator<ServiceRequest>;

    public div: HTMLDivElement;
    private input: HTMLInputElement;
    private result: HTMLDivElement;

    constructor(denotation: hxDemoDepM.HxDemoServiceProcessorView, evaluator: $tf.eval.Evaluator<ServiceRequest>) {
        this.denotation = denotation;
        this.evaluator = evaluator;

        this.initDivs();
    }

    private initDivs(): void {
        const div = document.createElement("div");
        div.classList.add("main")
        div.innerHTML =
            '<div class="flex-row">' +
            '<input id="input" type="text"><button id="computeButton" type="button" class="button">Compute Length!</button>' +
            '</div>' +
            '<div class="flex-row">' +
            '<div id="result"></div>' +
            '</div>';

        this.div = div;
        this.input = div.querySelector("#input");
        this.result = div.querySelector("#result");

        const computeButton = div.querySelector("#computeButton");

        computeButton.addEventListener("click", () => this.onComputeTextLengthClick())
    }

    private async onComputeTextLengthClick(): Promise<void> {
        const request = HxDemoComputeTextLength.create();
        request.text = this.input.value;

        const length = await request.EvalAndGet(this.evaluator);
        this.result.textContent = "Length is " + length;
    }

}

