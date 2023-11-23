var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __classPrivateFieldGet = (this && this.__classPrivateFieldGet) || function (receiver, state, kind, f) {
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a getter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot read private member from an object whose class did not declare it");
    return kind === "m" ? f : kind === "a" ? f.call(receiver) : f ? f.value : state.get(receiver);
};
var __classPrivateFieldSet = (this && this.__classPrivateFieldSet) || function (receiver, state, value, kind, f) {
    if (kind === "m") throw new TypeError("Private method is not writable");
    if (kind === "a" && !f) throw new TypeError("Private accessor was defined without a setter");
    if (typeof state === "function" ? receiver !== state || !f : !state.has(receiver)) throw new TypeError("Cannot write private member to an object whose class did not declare it");
    return (kind === "a" ? f.call(receiver, value) : f ? f.value = value : state.set(receiver, value)), value;
};
var _ApplicationManager_rootScopePromise;
import { reason, util } from "../tribefire.js.tf-js-api-2.3~/tf-js-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import { HxEvaluator } from "./ddsa.js";
import { ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-1.0~/ensure-service-api-model.js";
import { Reason } from "../com.braintribe.gm.gm-core-api-1.0~/ensure-gm-core-api.js";
import { NotFound } from "../com.braintribe.gm.essential-reason-model-1.0~/ensure-essential-reason-model.js";
import { UxModule } from "../tribefire.extension.js.js-deployment-model-2.1~/ensure-js-deployment-model.js";
var Maybe = reason.Maybe;
export class HostSettings {
}
class IHxHostSettingsImpl {
    servicesUrl() { return $tfUxHostSettings.servicesUrl; }
    webSocketUrl() { return $tfUxHostSettings.webSocketUrl; }
    domainId() { return $tfUxHostSettings.domainId; }
    usecases() { return $tfUxHostSettings.usecases; }
    queryString() { return $tfUxHostSettings.queryString; }
}
export class ApplicationManager {
    /* We assume hxApplication.defaultScope.defaultDomain is defined */
    constructor(hxApplication, servicesSession) {
        this.hostSettings = new IHxHostSettingsImpl();
        this.moduleMap = new Map();
        this.cssResources = new Set();
        this.hxEvaluator = new HxEvaluator();
        _ApplicationManager_rootScopePromise.set(this, void 0);
        this.hxApplication = hxApplication;
        this.servicesSession = servicesSession;
        this.platformModule = this.createPlatformModule();
        this.pushChannelId = util.newUuid();
        this.validateApp();
        this.openWebSocket();
    }
    createPlatformModule() {
        const jsModule = this.createPlatformJsModule();
        const uxModule = UxModule.create();
        uxModule.name = "hydrux-platform";
        return new HxModuleImpl(jsModule, uxModule);
    }
    createPlatformJsModule() {
        const appManager = this;
        return {
            contract: {
                bindComponents(componentBinder) {
                    componentBinder.bindComponent(hxM.HxSession, appManager.newSession.bind(appManager));
                    componentBinder.bindComponent(hxM.HxAccessSessionFactory, appManager.newAccessSessionFactory.bind(appManager));
                    componentBinder.bindComponent(hxM.HxServiceSessionFactory, appManager.newServiceSessionFactory.bind(appManager));
                    componentBinder.bindComponent(hxM.HxApplicationDomainSupplier, appManager.newApplicationDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxStaticDomainSupplier, appManager.newStaticDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxUrlDomainSupplier, appManager.newUrlDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxFallbackDomainSupplier, appManager.newFallbackDomainSupplier.bind(appManager));
                }
            }
        };
    }
    validateApp() {
        if (!this.hxApplication)
            throw new Error("HxApplication cannot be null!!!");
        if (!this.hxApplication.defaultScope)
            throw new Error("HxApplication.defaultScope is mandatory for now!!!");
    }
    openWebSocket() {
        const wsUrl = this.wsUrl();
        console.log("WS URL: " + wsUrl);
        const webSocket = new WebSocket(wsUrl);
        webSocket.onopen = event => {
            console.log("WebSocket connection established to: " + wsUrl);
        };
        webSocket.onerror = error => {
            console.log("Error with WebSocket connection to: " + wsUrl);
        };
        webSocket.onclose = closeEvent => {
            console.log(`Lost WebSocket connection to: ${wsUrl}. Reason: ${closeEvent.reason}`);
        };
        webSocket.onmessage = messageEvent => {
            this.handleWsMessage(messageEvent);
        };
    }
    wsUrl() {
        const params = new URLSearchParams();
        params.set("sessionId", this.servicesSession.sessionId());
        params.set("clientId", this.getApplicationId());
        params.set("pushChannelId", this.pushChannelId);
        params.set("accept", "gm/jse");
        return this.hostSettings.webSocketUrl() + "?" + params.toString();
    }
    handleWsMessage(messageEvent) {
        return __awaiter(this, void 0, void 0, function* () {
            const entity = yield this.servicesSession.decodeJse(messageEvent.data);
            if (ServiceRequest.isInstance(entity)) {
                const maybeResult = yield entity.EvalAndGetReasoned(this.getLocalEvaluator());
                if (!maybeResult.isSatisfied())
                    console.error("[ERROR] Processing PushNotification failed because: " + maybeResult.whyUnsatisfied().text);
            }
            else {
                console.log("[WARNING] Received push notification that is not a ServiceRequest: " + entity);
            }
        });
    }
    getTitle() { return this.hxApplication.title; }
    getApplicationId() { return this.hxApplication.applicationId; }
    getLocalEvaluator() { return this.hxEvaluator; }
    getServiceProcessorBinder() { return this.hxEvaluator; }
    getServicesSession() { return this.servicesSession; }
    getEvaluator() { return this.servicesSession.evaluatorBuilder(); }
    getHostSettings() { return this.hostSettings; }
    getPushChannelId() {
        return this.pushChannelId;
    }
    newPushAddress(serviceId) {
        const result = CallbackPushAddressing.create();
        result.pushChannelId = this.pushChannelId;
        result.serviceId = serviceId;
        return result;
    }
    loadCss(uxModule, cssResourcePath) {
        const moduleUrl = this.moduleUrl(uxModule);
        if (!moduleUrl.endsWith("index.js"))
            return;
        const baseUrl = moduleUrl.substr(0, moduleUrl.length - "index.js".length);
        const cssPath = baseUrl + cssResourcePath;
        if (this.cssResources.has(cssPath))
            return;
        this.cssResources.add(cssPath);
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = cssPath;
        link.media = 'all';
        document.head.appendChild(link);
    }
    /*
    FUTURE OPTIMIZATION:
    For now we simply retrieve full (service/data) models.

    Later we can resolve all HxSessionSuppliers with (yet to be added) flag saying the models should be preloaded. We do it with a single call,
    thus the result contains all needed instances. We can store these models in a persistence session, i.e. with identity management.

    For models that are not preloaded we can add tell the server which models we already know, thus it can send those back with absent properties.
    These will be filled when merged to the persistence session.
    */
    newSession(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            const sessionFactory = yield context.scope().resolveSessionFactory(denotation.factory);
            return sessionFactory();
        });
    }
    /* ********************* */
    /* Access SessionFactory */
    /* ********************* */
    newAccessSessionFactory(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            const accessId = yield this.resolveSessionDomainId(denotation, context);
            // const dataModel = await this.getModel(GetAccessModel, accessId);
            // const accessDescriptor = new session.AccessDescriptor(accessId, dataModel, null /* accessDenotationType*/);
            // const sessionFactory = this.servicesSession.sessionFactory(accessDescriptor).build();
            const sessionFactory = yield this.servicesSession.accessSessionFactory(accessId);
            console.log({ sessionFactory });
            return () => sessionFactory.get();
        });
    }
    /* ********************** */
    /* Service SessionFactory */
    /* ********************** */
    newServiceSessionFactory(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            const externalId = yield this.resolveSessionDomainId(denotation, context);
            const sessionFactory = yield this.servicesSession.serviceSessionFactory(externalId);
            return () => sessionFactory.get();
        });
    }
    resolveSessionDomainId(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            return context.scope().resolveDomain(denotation.domain);
        });
    }
    /* ***************** */
    /* Domain resolution */
    /* ***************** */
    newApplicationDomainSupplier(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            const result = this.valueOrReasoned($tfUxHostSettings.domainId, () => "DomainId not confiured in the internal $tfUxHostSettings variable.");
            return () => Promise.resolve(result);
        });
    }
    newStaticDomainSupplier(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            const result = this.valueOrReasoned(denotation.externalId, () => "Mandatory property HxStaticDomainSupplier.externalId is somehow null.");
            return () => Promise.resolve(result);
        });
    }
    newUrlDomainSupplier(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            return () => this.readDomainFromUrl(denotation.paramName);
        });
    }
    readDomainFromUrl(paramName) {
        return __awaiter(this, void 0, void 0, function* () {
            const urlParams = new URL(document.URL).searchParams;
            return this.valueOrReasoned(urlParams.get(paramName), () => "URL parameter not found: " + paramName);
        });
    }
    newFallbackDomainSupplier(denotation, context) {
        return __awaiter(this, void 0, void 0, function* () {
            return () => this.resolveDomainFromSuppliers(denotation.suppliers, context.scope());
        });
    }
    resolveDomainFromSuppliers(suppliers, scope) {
        return __awaiter(this, void 0, void 0, function* () {
            const subReasons = $tf.list(Reason.getTypeSignature());
            for (const supplierDenotation of suppliers.iterable()) {
                const supplier = yield scope.resolveDomainSupplier(supplierDenotation);
                const maybeResult = yield supplier();
                if (maybeResult.isSatisfied())
                    return maybeResult;
                subReasons.add(maybeResult.whyUnsatisfied());
            }
            const wrapperReason = this.reason("No supplier in the fallback chain provided a domain.");
            // TODO the nested messages are not read - maybe concatenate them on top level instead?
            wrapperReason.reasons.addAll(subReasons);
            return Maybe.empty(wrapperReason);
        });
    }
    valueOrReasoned(value, msg) {
        return value ? Maybe.complete(value) : Maybe.empty(this.reason(msg()));
    }
    reason(msg) {
        return reason.build(NotFound).text(msg).toReason();
    }
    /* ***************** */
    /*      Scopes       */
    /* ***************** */
    getRootScope() {
        if (__classPrivateFieldGet(this, _ApplicationManager_rootScopePromise, "f"))
            return __classPrivateFieldGet(this, _ApplicationManager_rootScopePromise, "f");
        /* Right now we insist  */
        return __classPrivateFieldSet(this, _ApplicationManager_rootScopePromise, this.createScope(this.hxApplication.defaultScope, null), "f");
    }
    createScope(scopeDenotation, parent) {
        return __awaiter(this, void 0, void 0, function* () {
            const scope = new HxScopeImpl(this, scopeDenotation, parent);
            for (const controller of scopeDenotation.controllers.iterable())
                yield scope.resolveComponent(controller);
            return scope;
        });
    }
    /* ***************** */
    /* Module resolution */
    /* ***************** */
    /* Resolves JS UxMOdule to Promise<HxModule> */
    resolveModule(uxModule) {
        return __awaiter(this, void 0, void 0, function* () {
            if (uxModule == null)
                return this.platformModule;
            let result = this.moduleMap.get(uxModule);
            if (!result)
                this.moduleMap.set(uxModule, result = this.loadModule(uxModule));
            return result;
        });
    }
    loadModule(uxModule) {
        return __awaiter(this, void 0, void 0, function* () {
            const url = this.moduleUrl(uxModule);
            const jsModule = yield import(url);
            return new HxModuleImpl(jsModule, uxModule);
        });
    }
    moduleUrl(uxModule) {
        const path = uxModule.path;
        if (path.startsWith("/") || path.includes(":"))
            return path;
        else
            return addToPath(this.servicesSession.servicesUrl(), path);
    }
}
_ApplicationManager_rootScopePromise = new WeakMap();
export class HxModuleImpl {
    constructor(jsModule, uxModule) {
        this.jsModule = jsModule;
        this.contract = jsModule.contract;
        this.componentRegistry = new ComponentRegistry(uxModule);
        this.contract.bindComponents(this.componentRegistry);
    }
}
class ModuleBindingContext {
}
class ComponentRegistry {
    constructor(uxModule) {
        this.factoryMap = util.newDenotationMap();
        this.uxModule = uxModule;
    }
    bindView(componentType, factory) {
        this.bindComponent(componentType, factory);
    }
    bindComponent(componentType, factory) {
        if (!componentType)
            throw new Error(`UxModule [${this.uxModule.name}] is trying to bind a component but given componentType is null.`);
        this.factoryMap.put(componentType, factory);
    }
    resolveFactory(component) {
        const result = this.factoryMap.find(component);
        if (!result)
            throw new Error(`No factory bound for component type '${component.EntityType().getTypeSignature}' in UxModule: ${this.uxModule.name}`);
        return result;
    }
}
// Scope
export class HxScopeImpl {
    constructor(applicationManager, scopeDenotation, parent) {
        this.components = new Map();
        this.applicationManager = applicationManager;
        this.scopeDenotation = scopeDenotation;
        this.parent = parent;
    }
    getApplication() {
        return this.applicationManager;
    }
    getDomain() {
        return __awaiter(this, void 0, void 0, function* () {
            if (this.domainPromise)
                return this.domainPromise;
            if (this.scopeDenotation.defaultDomain) {
                const domainSupplier = yield this.resolveDomainSupplier(this.scopeDenotation.defaultDomain);
                const maybeDomain = yield domainSupplier();
                if (!maybeDomain.isSatisfied())
                    throw new Error(maybeDomain.whyUnsatisfied().text);
                return this.domainPromise = Promise.resolve(maybeDomain.get());
            }
            const rootScope = yield this.applicationManager.getRootScope();
            if (this != rootScope)
                return this.domainPromise = rootScope.getDomain();
            return this.domainPromise = Promise.resolve($tfUxHostSettings.domainId);
        });
    }
    resolveDomain(supplierDenotation) {
        return __awaiter(this, void 0, void 0, function* () {
            if (!supplierDenotation)
                return this.getDomain();
            const supplier = yield this.resolveDomainSupplier(supplierDenotation);
            const maybeDomainId = yield supplier();
            if (!maybeDomainId.isSatisfied())
                throw new Error(`Unable to resolve domain id for domain supplier [${supplierDenotation}]. Reason: ${maybeDomainId.whyUnsatisfied().text}`);
            return maybeDomainId.get();
        });
    }
    resolveSession(session) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(session);
        });
    }
    resolveSessionFactory(supplier) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(supplier);
        });
    }
    resolveView(view) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(view);
        });
    }
    resolveController(controller) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(controller);
        });
    }
    resolveDataConsumer(dataConsumer) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(dataConsumer);
        });
    }
    resolveSelectionEventSource(selectionEventSource) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(selectionEventSource);
        });
    }
    resolveDomainSupplier(domainSupplier) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.resolveComponent(domainSupplier);
        });
    }
    resolveComponent(componentDenotation) {
        return __awaiter(this, void 0, void 0, function* () {
            const componentScope = yield this.resolveComponentScope(componentDenotation.scope);
            return componentScope.resolveYourComponent(componentDenotation);
        });
    }
    resolveYourComponent(componentDenotation) {
        return __awaiter(this, void 0, void 0, function* () {
            let result = this.components.get(componentDenotation);
            if (!result) {
                result = yield this.createComponent(componentDenotation);
                this.components.set(componentDenotation, result);
            }
            return result;
        });
    }
    createComponent(componentDenotation) {
        return __awaiter(this, void 0, void 0, function* () {
            const hxModule = yield this.applicationManager.resolveModule(componentDenotation.module);
            const creationContext = {
                application: () => this.getApplication(),
                scope: () => this
            };
            const factory = hxModule.componentRegistry.resolveFactory(componentDenotation);
            return factory(componentDenotation, creationContext);
        });
    }
    resolveComponentScope(scopeDenotation) {
        return __awaiter(this, void 0, void 0, function* () {
            if (!scopeDenotation)
                return this;
            const predecessorScope = this.resolvePredecessorScope(scopeDenotation);
            if (predecessorScope)
                return predecessorScope;
            return this.applicationManager.createScope(scopeDenotation, this);
        });
    }
    resolvePredecessorScope(scopeDenotation) {
        if (this.scopeDenotation == scopeDenotation)
            return this;
        else if (this.parent)
            return this.parent.resolvePredecessorScope(scopeDenotation);
        else
            return null;
    }
} /* HxScope */
export function addToPath(a, b) {
    return a.endsWith("/") ? a + b : a + "/" + b;
}
