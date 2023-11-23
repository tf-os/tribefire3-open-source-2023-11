var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
import { HxDemoComputeTextLength } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
export function bindServiceProcessor(context) {
    context.componentBinder().bindView(hxDemoDepM.HxDemoServiceProcessorView, createServiceProcessorView);
}
function createServiceProcessorView(denotation, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const evaluator = context.application()
            .getEvaluator()
            .setDefaultDomain('hx-demo-services')
            .build();
        const app = new ServiceApp(denotation, evaluator);
        return hx.IHxTools.view(() => app.div);
    });
}
class ServiceApp {
    constructor(denotation, evaluator) {
        this.denotation = denotation;
        this.evaluator = evaluator;
        this.initDivs();
    }
    initDivs() {
        const div = document.createElement("div");
        div.classList.add("main");
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
        computeButton.addEventListener("click", () => this.onComputeTextLengthClick());
    }
    onComputeTextLengthClick() {
        return __awaiter(this, void 0, void 0, function* () {
            const request = HxDemoComputeTextLength.create();
            request.text = this.input.value;
            const length = yield request.EvalAndGet(this.evaluator);
            this.result.textContent = "Length is " + length;
        });
    }
}
