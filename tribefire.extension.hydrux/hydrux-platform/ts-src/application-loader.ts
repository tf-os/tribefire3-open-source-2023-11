import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as securityApiM from "../com.braintribe.gm.security-service-api-model-2.0~/ensure-security-service-api-model.js";
import * as hxApiM from "../tribefire.extension.hydrux.hydrux-api-model-2.1~/ensure-hydrux-api-model.js";

import { HxApplicationImpl, HostSettings, addToPath } from "./component-management.js";

declare var $tfUxHostSettings: HostSettings;

const sessionIdCookieName = "tfsessionId";

(async () => {
    // TODO tfSettings should allow non-authorized applications
    const cookies = getCookies();

    const sessionId = cookies.get(sessionIdCookieName);
    if (!sessionId)
        return redirectToLogin();

    const connection = tf.remote.connect($tfUxHostSettings.servicesUrl);
    const sessionResponse = await openSession();

    if (!sessionResponse.successful) {
        deleteCookie(sessionIdCookieName);
        return redirectToLogin();
    }

    const servicesSession = connection.newSession(sessionResponse.userSession);

    const hxApplication = await resolveHxApplication();
    if (!hxApplication)
        return;

    const theApp = new HxApplicationImpl(hxApplication, servicesSession);
    await theApp.initAsync();

    ($tf as any).hydrux = { application: theApp };

    const mainView = await theApp.getRootScope().resolveView(hxApplication.view);

    const mainHtmlElement = mainView.htmlElement();

    mainHtmlElement.style.top = '0';
    mainHtmlElement.style.left = '0';
    mainHtmlElement.style.height = '100%';
    mainHtmlElement.style.width = '100%';
    mainHtmlElement.style.boxSizing = 'border-box';
    mainHtmlElement.style.position = 'relative';

    const body = document.body;

    body.appendChild(mainHtmlElement);

    /** Evaluates OpenUserSession for an existing sessionId from cookies and promises a response. */
    async function openSession(): Promise<securityApiM.OpenUserSessionResponse> {
        const credentials = securityApiM.ExistingSessionCredentials.create();
        credentials.existingSessionId = sessionId;

        const openSession = securityApiM.OpenUserSession.create();
        openSession.credentials = credentials;
        openSession.noExceptionOnFailure = true;

        return openSession.EvalAndGet(connection.evaluator());
    }

    /** Evaluates ResolveHxApplication for domainId and useCases from the settings. */
    async function resolveHxApplication(): Promise<$T.tribefire.extension.hydrux.model.deployment.HxApplication> {
        const request = hxApiM.ResolveHxApplication.create();
        request.targetDomainId = $tfUxHostSettings.domainId;
        request.prototypingModule = $tfUxHostSettings.prototypingModule;

        if ($tfUxHostSettings.usecases)
            request.useCases.addAllJs(...$tfUxHostSettings.usecases);

        const maybe = await request.EvalAndGetReasoned(servicesSession.evaluator());
        if (!maybe.isSatisfied()) {
            console.error("No Hydrux app found for settings: " + JSON.stringify($tfUxHostSettings) + ". Reason: " + maybe.whyUnsatisfied().text);
            return null;
        }

        return maybe.value();
    }

})().catch(reason => {
    const body = document.body;
    body.innerHTML = "<span style='color:red'>Could not load Hydrux application. Error: " + reason + "</span>";
})

//
// Helpers
//

function redirectToLogin() {
    window.location.replace(addToPath($tfUxHostSettings.servicesUrl, "login?continue=" + encodeURIComponent(window.location.href)));
}

function getCookies(): Map<string, string> {
    const result = new Map();

    const ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        const c = ca[i].trim();
        const keyValue = c.split('=');
        const key = decodeURIComponent(keyValue[0]);
        const value = decodeURIComponent(keyValue[1]);

        result.set(key, value);
    }

    return result;
}

function deleteCookie(name: string) {
    // no idea if the path is correct
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}