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
import * as hxDepM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
// *************************************************************************************************************
// URL: http://localhost:8080/tribefire-services/hydrux/hydrux-prototyping?hxproto=hx-prototyping-demo-hx-module
// *************************************************************************************************************
export const contract = {
    bind(context) {
        context.loadCss(context.currentUxModule(), "hx-prototypoing-demo-common.css");
        const componentBinder = context.componentBinder();
        componentBinder.bindView(hxDepM.HxMainView, createMainView);
    }
};
/***********/
/* Default  */
/***********/
function createMainView(denotation, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const div = document.createElement("div");
        //div.style.backgroundColor = denotation.backgroundColor;
        //div.style.color = denotation.textColor;
        div.style.padding = "10px";
        div.innerHTML = "<h1>Hydrux Prototyping Demo Application</h1>" + descriptionDiv();
        return hx.IHxTools.view(() => div);
    });
}
function descriptionDiv() {
    return "" +
        "<div>" +
        "<div>This use-case demonstrates the most basic HX view  HxDemoStaticPageView, only parameterzided by the two colors.<br/>" +
        "These colors are taken from the denotation instance and the corresponding style is set via code.</p>" +
        "<div>HxDemoInitializerSpace.staticHxApplication()</div>" +
        "</div>";
}
