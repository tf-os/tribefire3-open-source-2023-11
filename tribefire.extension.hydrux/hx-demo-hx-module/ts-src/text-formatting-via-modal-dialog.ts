import { reason, service, reflection } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoApiM from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";

import { Canceled, InternalError } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import { HxDemoChooseTextProcessingMethodRequestDialog } from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";


import Maybe = reason.Maybe;

export function bindTextFormattingWithModalDialog(context: hx.IHxModuleBindingContext): void {

    context.serviceProcessorBinder().bind(hxDemoApiM.HxDemoProcessText, new TextFormatter());

    context.componentBinder().bindRequestDialog(HxDemoChooseTextProcessingMethodRequestDialog, createChooseTextProcessingMethodRequestDialog);
}

class TextFormatter implements hx.IHxServiceProcessor<hxDemoApiM.HxDemoProcessText, string> {

    async process(ctx: service.ServiceRequestContext, request: hxDemoApiM.HxDemoProcessText): Promise<Maybe<string>> {
        if (!request.text)
            return Maybe.complete("<N/A>");

        const pickMethod = this.newPickMethodRequest(request).create();
        const maybeMethod = await pickMethod.EvalAndGetReasoned(ctx);

        if (!maybeMethod.isSatisfied())
            return maybeMethod;

        const method = maybeMethod.get();

        if (method == "TO_UPPERCASE")
            return Maybe.complete(request.text.toUpperCase());

        if (method == "TO_LOWERCASE")
            return Maybe.complete(request.text.toLowerCase());

        const error = InternalError.create();
        error.text = "Unknown text processing method: " + method;

        return Maybe.empty(error);
    }

    private newPickMethodRequest(request: hxDemoApiM.HxDemoProcessText): reflection.EntityType<hxDemoApiM.HxDemoPickMethod> {
        if (!request.showModal)
            return hxDemoApiM.HxDemoPickMethod_NonModalDraggable;

        const wc = request.windowCustomizability;
        if (!wc)
            return hxDemoApiM.HxDemoPickMethod_None;

        if (wc === "draggable")
            return hxDemoApiM.HxDemoPickMethod_Draggable;

        if (wc === "resizable")
            return hxDemoApiM.HxDemoPickMethod_Resizable;

        throw new Error("Unknown windowCustomizability [" + wc + "] configured on: " + request);
    }

}

/** Arrow function instead of this method would be way simpler: async (denotation) => new DialogImpl(denotation)
 * 
 * This verbose version (with all types / unused parameters) is here for didactic purposes.*/
async function createChooseTextProcessingMethodRequestDialog(
    denotation: HxDemoChooseTextProcessingMethodRequestDialog,
    context: hx.IHxComponentCreationContext
): Promise<hx.IHxRequestDialog<hxDemoApiM.HxDemoPickMethod, string>> {

    return new DialogImpl(denotation);
}

class DialogImpl implements hx.IHxRequestDialog<hxDemoApiM.HxDemoPickMethod, string> {

    readonly #denotation: HxDemoChooseTextProcessingMethodRequestDialog;
    readonly #dialogDiv = document.createElement("div");

    // FYI: this field must be declared first, otherwise error: Cannot write private member to an object whose class did not declare it
    /*    */ #resolvePromise: ((value: Maybe<string>) => void);
    readonly #promise = new Promise((resolve) => { this.#resolvePromise = resolve });

    constructor(denotation: HxDemoChooseTextProcessingMethodRequestDialog) {
        this.#denotation = denotation;
    }

    htmlElement(): HTMLElement {
        this.#dialogDiv.classList.add("dialog-options")

        this.addOption("To UpperCase", "TO_UPPERCASE");
        this.addOption("To LowerCase", "TO_LOWERCASE");
        this.addOptionButton("CANCEL", Maybe.empty(Canceled.create()), "cancel");

        return this.#dialogDiv;
    }

    private addOption(label: string, methodCode: string): void {
        this.addOptionButton(label, Maybe.complete(methodCode));
    }

    private addOptionButton(label: string, value: Maybe<string>, cssClass?: string): void {
        const optionDiv = document.createElement("div");
        optionDiv.classList.add("dialogOption", "button");
        if (cssClass)
            optionDiv.classList.add(cssClass);
        optionDiv.textContent = label;
        optionDiv.onclick = (e) => { this.#resolvePromise(value); }

        this.#dialogDiv.appendChild(optionDiv);
    }

    process(ctx: unknown, request: hxDemoApiM.HxDemoPickMethod): Promise<Maybe<string>> {
        return this.#promise as Promise<Maybe<string>>;
    }

}

