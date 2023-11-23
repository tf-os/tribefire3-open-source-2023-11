var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __classPrivateFieldSet = (this && this.__classPrivateFieldSet) || function (receiver, state, value, kind, f) {
    if (kind === "m") throw new TypeError("Private method is not writable");
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a setter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot write private member to an object whose class did not declare it");
    return (kind === "a" ? f.call(receiver, value) : f ? f.value = value : state.set(receiver, value)), value;
};
var __classPrivateFieldGet = (this && this.__classPrivateFieldGet) || function (receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
};
var _DialogImpl_denotation, _DialogImpl_dialogDiv, _DialogImpl_resolvePromise, _DialogImpl_promise;
import { reason } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hxDemoApiM from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { Canceled, InternalError } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import { HxDemoChooseTextProcessingMethodRequestDialog } from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
var Maybe = reason.Maybe;
export function bindTextFormattingWithModalDialog(context) {
    context.serviceProcessorBinder().bind(hxDemoApiM.HxDemoProcessText, new TextFormatter());
    context.componentBinder().bindRequestDialog(HxDemoChooseTextProcessingMethodRequestDialog, createChooseTextProcessingMethodRequestDialog);
}
class TextFormatter {
    process(ctx, request) {
        return __awaiter(this, void 0, void 0, function* () {
            if (!request.text)
                return Maybe.complete("<N/A>");
            const pickMethod = this.newPickMethodRequest(request).create();
            const maybeMethod = yield pickMethod.EvalAndGetReasoned(ctx);
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
        });
    }
    newPickMethodRequest(request) {
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
function createChooseTextProcessingMethodRequestDialog(denotation, context) {
    return __awaiter(this, void 0, void 0, function* () {
        return new DialogImpl(denotation);
    });
}
class DialogImpl {
    constructor(denotation) {
        _DialogImpl_denotation.set(this, void 0);
        _DialogImpl_dialogDiv.set(this, document.createElement("div"));
        // FYI: this field must be declared first, otherwise error: Cannot write private member to an object whose class did not declare it
        /*    */ _DialogImpl_resolvePromise.set(this, void 0);
        _DialogImpl_promise.set(this, new Promise((resolve) => { __classPrivateFieldSet(this, _DialogImpl_resolvePromise, resolve, "f"); }));
        __classPrivateFieldSet(this, _DialogImpl_denotation, denotation, "f");
    }
    htmlElement() {
        __classPrivateFieldGet(this, _DialogImpl_dialogDiv, "f").classList.add("dialog-options");
        this.addOption("To UpperCase", "TO_UPPERCASE");
        this.addOption("To LowerCase", "TO_LOWERCASE");
        this.addOptionButton("CANCEL", Maybe.empty(Canceled.create()), "cancel");
        return __classPrivateFieldGet(this, _DialogImpl_dialogDiv, "f");
    }
    addOption(label, methodCode) {
        this.addOptionButton(label, Maybe.complete(methodCode));
    }
    addOptionButton(label, value, cssClass) {
        const optionDiv = document.createElement("div");
        optionDiv.classList.add("dialogOption", "button");
        if (cssClass)
            optionDiv.classList.add(cssClass);
        optionDiv.textContent = label;
        optionDiv.onclick = (e) => { __classPrivateFieldGet(this, _DialogImpl_resolvePromise, "f").call(this, value); };
        __classPrivateFieldGet(this, _DialogImpl_dialogDiv, "f").appendChild(optionDiv);
    }
    process(ctx, request) {
        return __classPrivateFieldGet(this, _DialogImpl_promise, "f");
    }
}
_DialogImpl_denotation = new WeakMap(), _DialogImpl_dialogDiv = new WeakMap(), _DialogImpl_resolvePromise = new WeakMap(), _DialogImpl_promise = new WeakMap();
