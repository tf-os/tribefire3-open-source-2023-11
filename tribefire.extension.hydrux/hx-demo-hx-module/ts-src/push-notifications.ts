import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";

import { HxDemoStartComplexProcessOnServer } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { HxDemoNotifyProgress, HxDemoSendProcessResult } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";

import Maybe = tf.reason.Maybe;

export function bindPushNotifications(context: hx.IHxModuleBindingContext): void {
    context.componentBinder().bindView(hxDemoDepM.HxDemoPushNotificationsView, createPushNotificationsView);
}

async function createPushNotificationsView(denotation: hxDemoDepM.HxDemoPushNotificationsView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const application = context.application();
    const evaluator = application
        .getEvaluator()
        .setDefaultDomain('hx-demo-services')
        .build();

    application.loadCss(denotation.module, "push-notifications.css");

    const app = new NotificationsApp(denotation, application, evaluator);

    return hx.IHxTools.view(() => app.div);
}

class NotificationsApp {

    private readonly denotation: hxDemoDepM.HxDemoPushNotificationsView;
    private readonly application: hx.IHxApplication;
    private readonly evaluator: tf.eval_.Evaluator<ServiceRequest>;

    public div: HTMLDivElement;
    private input: HTMLInputElement;
    private result: HTMLDivElement;

    private notificationDiv: HTMLDivElement;

    constructor(denotation: hxDemoDepM.HxDemoPushNotificationsView, application: hx.IHxApplication, evaluator: $tf.eval.Evaluator<ServiceRequest>) {
        this.denotation = denotation;
        this.application = application;
        this.evaluator = evaluator;

        this.application.getServiceProcessorBinder().bindFunction(HxDemoNotifyProgress, (ctx, req) => this.onNotifyProgress(req));
        this.application.getServiceProcessorBinder().bindFunction(HxDemoSendProcessResult, (ctx, req) => this.onProcessResult(req));

        this.initDivs();
    }

    private initDivs(): void {
        const div = document.createElement("div");
        div.classList.add("hx-demo-notifications");
        div.style.padding = "10px";

        div.innerHTML =
            '<input id="input" type="text"><button id="callServerButton" type="button">Call Server!</button>' +
            '<div id="result"></div>';

        this.div = div;
        this.input = div.querySelector("#input");
        this.result = div.querySelector("#result");

        const serverButton = div.querySelector("#callServerButton");
        serverButton.addEventListener("click", () => this.onCallServerClick());
    }

    private async onCallServerClick(): Promise<void> {
        if (this.notificationDiv)
            return;

        this.createNotificationDiv();
        this.showMessage("Sending request our beloved server!");

        const request = HxDemoStartComplexProcessOnServer.create();
        request.message = this.input.value;
        request.pushAddress = this.application.newPushAddress("pushNotifications-123");

        request.EvalAndGet(this.evaluator);
    }

    private createNotificationDiv(): void {
        const messageParentDiv = document.createElement("div");
        messageParentDiv.classList.add("notification-layer")

        const messageDiv = document.createElement("div");
        messageDiv.classList.add("message-div");

        messageParentDiv.appendChild(messageDiv);

        this.notificationDiv = messageDiv;
        this.div.appendChild(messageParentDiv);
    }

    private async onNotifyProgress(request: HxDemoNotifyProgress): Promise<Maybe<any>> {
        this.showMessage(request.message);

        return Maybe.complete("OK");
    }

    private showMessage(message: string) {
        this.notificationDiv.textContent = message;
    }

    private async onProcessResult(request: HxDemoSendProcessResult): Promise<Maybe<any>> {
        const messageParentDiv = this.notificationDiv.parentElement;

        this.div.removeChild(messageParentDiv);
        this.notificationDiv = null;

        return Maybe.complete("OK");
    }

}


