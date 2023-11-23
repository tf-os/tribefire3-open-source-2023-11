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
var _ErrorsApp_denotation, _ErrorsApp_evaluator;
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
import { HxDemoThrowException } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
export function bindErrors(context) {
    context.componentBinder().bindView(hxDemoDepM.HxDemoErrorsView, createErrorsView);
}
function createErrorsView(denotation, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const evaluator = context.application()
            .getEvaluator()
            .setDefaultDomain('hx-demo-services')
            .build();
        const app = new ErrorsApp(denotation, evaluator);
        return hx.IHxTools.view(() => app.div);
    });
}
class ErrorsApp {
    constructor(denotation, evaluator) {
        _ErrorsApp_denotation.set(this, void 0);
        _ErrorsApp_evaluator.set(this, void 0);
        __classPrivateFieldSet(this, _ErrorsApp_denotation, denotation, "f");
        __classPrivateFieldSet(this, _ErrorsApp_evaluator, evaluator, "f");
        this.initDivs();
    }
    initDivs() {
        const div = document.createElement("div");
        div.classList.add("hx-demo-errors", "main");
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
        serverErrorButton.addEventListener("click", () => this.onServerErrorClick());
        clientErrorButton.addEventListener("click", () => this.onClientErrorClick());
        clientPromiseErrorButton.addEventListener("click", () => this.onClientPromiseErrorClick());
    }
    onServerErrorClick() {
        return __awaiter(this, void 0, void 0, function* () {
            const request = HxDemoThrowException.create();
            yield request.EvalAndGet(__classPrivateFieldGet(this, _ErrorsApp_evaluator, "f"));
        });
    }
    onClientErrorClick() {
        const request = HxDemoThrowException.create();
        request.EvalAndGet(null);
    }
    onClientPromiseErrorClick() {
        return __awaiter(this, void 0, void 0, function* () {
            const request = HxDemoThrowException.create();
            yield request.EvalAndGet(null);
        });
    }
}
_ErrorsApp_denotation = new WeakMap(), _ErrorsApp_evaluator = new WeakMap();
