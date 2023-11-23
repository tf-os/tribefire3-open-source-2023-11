var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxDemoDepM from "../tribefire.extension.hydrux.hx-demo-hx-deployment-model-2.1~/ensure-hx-demo-hx-deployment-model.js";
import { HxDemoStartComplexProcessOnServer } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
import { HxDemoNotifyProgress, HxDemoSendProcessResult } from "../tribefire.extension.hydrux.hx-demo-api-model-2.1~/ensure-hx-demo-api-model.js";
var Maybe = tf.reason.Maybe;
export function bindPushNotifications(context) {
    context.componentBinder().bindView(hxDemoDepM.HxDemoPushNotificationsView, createPushNotificationsView);
}
function createPushNotificationsView(denotation, context) {
    return __awaiter(this, void 0, void 0, function* () {
        const application = context.application();
        const evaluator = application
            .getEvaluator()
            .setDefaultDomain('hx-demo-services')
            .build();
        application.loadCss(denotation.module, "push-notifications.css");
        const app = new NotificationsApp(denotation, application, evaluator);
        return hx.IHxTools.view(() => app.div);
    });
}
class NotificationsApp {
    constructor(denotation, application, evaluator) {
        this.denotation = denotation;
        this.application = application;
        this.evaluator = evaluator;
        this.application.getServiceProcessorBinder().bindFunction(HxDemoNotifyProgress, (ctx, req) => this.onNotifyProgress(req));
        this.application.getServiceProcessorBinder().bindFunction(HxDemoSendProcessResult, (ctx, req) => this.onProcessResult(req));
        this.initDivs();
    }
    initDivs() {
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
    onCallServerClick() {
        return __awaiter(this, void 0, void 0, function* () {
            if (this.notificationDiv)
                return;
            this.createNotificationDiv();
            this.showMessage("Sending request our beloved server!");
            const request = HxDemoStartComplexProcessOnServer.create();
            request.message = this.input.value;
            request.pushAddress = this.application.newPushAddress("pushNotifications-123");
            request.EvalAndGet(this.evaluator);
        });
    }
    createNotificationDiv() {
        const messageParentDiv = document.createElement("div");
        messageParentDiv.classList.add("notification-layer");
        const messageDiv = document.createElement("div");
        messageDiv.classList.add("message-div");
        messageParentDiv.appendChild(messageDiv);
        this.notificationDiv = messageDiv;
        this.div.appendChild(messageParentDiv);
    }
    onNotifyProgress(request) {
        return __awaiter(this, void 0, void 0, function* () {
            this.showMessage(request.message);
            return Maybe.complete("OK");
        });
    }
    showMessage(message) {
        this.notificationDiv.textContent = message;
    }
    onProcessResult(request) {
        return __awaiter(this, void 0, void 0, function* () {
            const messageParentDiv = this.notificationDiv.parentElement;
            this.div.removeChild(messageParentDiv);
            this.notificationDiv = null;
            return Maybe.complete("OK");
        });
    }
}
