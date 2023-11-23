import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";

import { bindServiceProcessor } from "./serviceProcessor.js";
import { bindAccess } from "./access.js";
import { bindErrors } from "./errors.js";
import { bindPushNotifications } from "./push-notifications.js";
import { bindTextFormattingWithModalDialog } from "./text-formatting-via-modal-dialog.js";
import { bindModalDialog } from "./modal-dialog.js";

export const contract: hx.IHxModuleContract = {
    bind(context: hx.IHxModuleBindingContext): void {

        context.loadCss(context.currentUxModule(), "hx-demo-common.css");

        const componentBinder = context.componentBinder();
        componentBinder.bindView(hxDemoDepM.HxDemoDefaultView, createDefaultView);
        componentBinder.bindView(hxDemoDepM.HxDemoStaticPageView, createStaticView);

        bindServiceProcessor(context);
        bindAccess(context);
        bindErrors(context);
        bindPushNotifications(context);

        bindTextFormattingWithModalDialog(context);
        bindModalDialog(context);
    }
}

/***********/
/* Default  */
/***********/

async function createDefaultView(denotation: hxDemoDepM.HxDemoStaticPageView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const application = context.application();
    const settings = application.getHostSettings();

    const buttonWidth = 400; /**/

    const div = document.createElement("div");
    div.classList.add("hx-demo-default", "main");

    const innerDiv = document.createElement("div");
    innerDiv.innerHTML = "<h1 style='text-align:center'>Hydrux Demo Applications</h1>";
    innerDiv.style.position = "absolute";
    innerDiv.style.left = "50%";
    innerDiv.style.marginLeft = -(buttonWidth / 2) + "px";

    div.appendChild(innerDiv);

    appendLink("static");
    appendLink("service");
    appendLink("access");
    appendLink("errors");
    appendLink("pushNotifications");
    appendLink("modalDialog");

    function appendLink(usecase: string): void {
        const link = document.createElement("a")
        link.classList.add("button", "hx-demo-big-button", "hx-demo-button");
        link.textContent = "/" + usecase;
        link.href = settings.servicesUrl() + "/hydrux/" + settings.domainId() + "/" + usecase;
        if (settings.queryString())
            link.href += "?" + settings.queryString();

        innerDiv.appendChild(link);
    }

    return hx.IHxTools.view(() => div);
}


/***********/
/* Static  */
/***********/

async function createStaticView(denotation: hxDemoDepM.HxDemoStaticPageView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const div = document.createElement("div");
    div.style.backgroundColor = denotation.backgroundColor;
    div.style.color = denotation.textColor;
    div.style.padding = "10px";
    div.innerHTML = "<h1>HX Demo Static Page</h1>" +
        "<p>Check out this " + denotation.textColor + " text on " + denotation.backgroundColor + " background!</p>" +
        descriptionDiv();


    return hx.IHxTools.view(() => div);
}

function descriptionDiv(): string {
    return "" +
        "<div class='hx-demo-description'>" +
        "<div>This use-case demonstrates the most basic HX view  HxDemoStaticPageView, only parameterzided by the two colors.<br/>" +
        "These colors are taken from the denotation instance and the corresponding style is set via code.</p>" +
        "<div class='hx-demo-see'>HxDemoInitializerSpace.staticHxApplication()</div>" +
        "</div>";

}