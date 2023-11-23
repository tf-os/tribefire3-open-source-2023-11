import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDepM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";

// *************************************************************************************************************
// URL: http://localhost:8080/tribefire-services/hydrux/hydrux-prototyping?hxproto=hx-prototyping-demo-hx-module
// *************************************************************************************************************

export const contract: hx.IHxModuleContract = {
    bind(context: hx.IHxModuleBindingContext): void {

        context.loadCss(context.currentUxModule(), "hx-prototypoing-demo-common.css");

        const componentBinder = context.componentBinder();
        componentBinder.bindView(hxDepM.HxMainView, createMainView);

    }
}

/***********/
/* Default  */
/***********/

async function createMainView(denotation: hxDepM.HxMainView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const div = document.createElement("div");
    //div.style.backgroundColor = denotation.backgroundColor;
    //div.style.color = denotation.textColor;
    div.style.padding = "10px";
    div.innerHTML = "<h1>Hydrux Prototyping Demo Application</h1>" + descriptionDiv();

    return hx.IHxTools.view(() => div);
}

function descriptionDiv(): string {
    return "" +
        "<div>" +
        "<div>This use-case demonstrates the most basic HX view  HxDemoStaticPageView, only parameterzided by the two colors.<br/>" +
        "These colors are taken from the denotation instance and the corresponding style is set via code.</p>" +
        "<div>HxDemoInitializerSpace.staticHxApplication()</div>" +
        "</div>";

}