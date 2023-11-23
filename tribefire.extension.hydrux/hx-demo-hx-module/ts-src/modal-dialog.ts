import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
import * as hxDemoApiM from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";

import { Canceled } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";

import { ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { HxWindowCustomizability } from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";

export function bindModalDialog(context: hx.IHxModuleBindingContext): void {
    const spb = context.serviceProcessorBinder();
    const componentBinder = context.componentBinder();

    // Modal Dialog
    componentBinder.bindView(hxDemoDepM.HxDemoModalDialogView, createModalDialogView);

    // HxDemoPickTextProcessMethod request is resolved via an instance of HxDemoChooseTextProcessingMethodRequestDialog
    spb.bindDialogProcessor(hxDemoApiM.HxDemoPickMethod_None, chooseMethodDialog(context, true, null));
    spb.bindDialogProcessor(hxDemoApiM.HxDemoPickMethod_Draggable, chooseMethodDialog(context, true, HxWindowCustomizability.draggable));
    spb.bindDialogProcessor(hxDemoApiM.HxDemoPickMethod_Resizable, chooseMethodDialog(context, true, HxWindowCustomizability.resizable));
    spb.bindDialogProcessor(hxDemoApiM.HxDemoPickMethod_NonModalDraggable, chooseMethodDialog(context, false, HxWindowCustomizability.draggable));
}

function chooseMethodDialog(context: hx.IHxModuleBindingContext, modal: boolean, windowCustomizability: HxWindowCustomizability): hxDemoDepM.HxDemoChooseTextProcessingMethodRequestDialog {
    const result = hxDemoDepM.HxDemoChooseTextProcessingMethodRequestDialog.create();
    result.module = context.currentUxModule();
    result.modal = modal;
    result.windowCustomizability = windowCustomizability;
    result.title = "Chose Text Processing Method";

    return result;
}

async function createModalDialogView(denotation: hxDemoDepM.HxDemoModalDialogView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    context.application().loadCss(denotation.module, "modal-dialog.css");

    const localEvaluator = context.application().getLocalEvaluator();

    const app = new ModalDialogApp(denotation, localEvaluator);

    return hx.IHxTools.view(() => app.div);
}

class ModalDialogApp {

    private readonly denotation: hxDemoDepM.HxDemoModalDialogView;
    private readonly evaluator: tf.eval_.Evaluator<ServiceRequest>;

    public div: HTMLDivElement;
    private input: HTMLInputElement;
    private result: HTMLDivElement;

    constructor(denotation: hxDemoDepM.HxDemoModalDialogView, evaluator: $tf.eval.Evaluator<ServiceRequest>) {
        this.denotation = denotation;
        this.evaluator = evaluator;

        this.initDivs();
    }

    private initDivs(): void {
        const div = document.createElement("div");
        div.classList.add("hx-demo-modal-dialog", "main")

        div.innerHTML =
            '<div class="flex-row">' +
            /**/'<input id="input" type="text">' +
            /**/'<button id="nonModalButton" type="button" class="button">NON-MODAL</button>' +
            /**/'<button id="noneButton" type="button" class="button">NONE</button>' +
            /**/'<button id="draggableButton" type="button" class="button">DRAGGABLE</button>' +
            /**/'<button id="resizableButton" type="button" class="button">RESIZABLE</button>' +
            '</div>' +
            '<div class="flex-row"><div id="result">...</div></div>' +
            '<div class="flex-row">' + this.descriptionDiv() + '</div>';

        this.div = div;
        this.input = div.querySelector("#input");
        this.result = div.querySelector("#result");

        const computeButton = div.querySelector("#nonModalButton");
        const modalButton = div.querySelector("#noneButton");
        const draggableButton = div.querySelector("#draggableButton");
        const resizableButton = div.querySelector("#resizableButton");

        computeButton.addEventListener("click", () => this.onProcessClick(false, null));
        modalButton.addEventListener("click", () => this.onProcessClick(true, null));
        draggableButton.addEventListener("click", () => this.onProcessClick(true, "draggable"));
        resizableButton.addEventListener("click", () => this.onProcessClick(true, "resizable"));

        this.div = div;
    }

    private descriptionDiv(): string {

        return "" +
            "<div class='hx-demo-description'>" +
            "<div>This shows how to evaluate a service-request using a request dialog. The processor is bound via:" +
            "<codeblock>serviceProcessorBinder.bindDialogProcessor(SomeRequest, someRequestDialogInstance)</codeblock>" +
            "where <code>SomeRequest</code> is the type of a requst and " +
            "<code>someRequestDialogInstance</code> is an instance of <code>SomeRequestDialog</code>, which extends <code>HxRequestDialog</code> (" +
            "which is a special type of <code>HxView</code> that is also a service processor.<br/><br/>In other words, we bind this <code>SomeRequestDialog</code> " +
            "as a view and a processor for <code>SomeRequest</code>." +
            "</div>" +
            "<div class='hx-demo-see'>HxDemoInitializerSpace.modalDialogHxApplication()</div>" +
            "</div>";
    }

    private async onProcessClick(modal: boolean, windowCustomizability: string): Promise<void> {
        const request = hxDemoApiM.HxDemoProcessText.create();
        request.showModal = modal;
        request.windowCustomizability = windowCustomizability;
        request.text = this.input.value;

        const processedText = await request.EvalAndGetReasoned(this.evaluator);

        if (processedText.isSatisfied()) {
            this.result.textContent = processedText.get();
            return;
        }

        if (processedText.isUnsatisfiedBy(Canceled))
            return;

        const reason = processedText.whyUnsatisfied();
        this.result.textContent = "[There was an error, see console]";
        console.log("Error when processing text '" + request.text + "':\n=> " + reason.text);
    }

}

