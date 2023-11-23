import { eval_, reason, reflection, remote, session, util } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";

import { HxLocalEvaluator } from "./ddsa.js";
import { NotFound } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import { ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { Reason } from "../com.braintribe.gm.gm-core-api-2.0~/ensure-gm-core-api.js";
import { UxModule } from "../tribefire.extension.js.js-deployment-model-3.0~/ensure-js-deployment-model.js";

import Maybe = reason.Maybe;
import EntityType = reflection.EntityType;
import PersistenceGmSession = session.PersistenceGmSession;

export class HostSettings {
    servicesUrl: string;
    webSocketUrl: string;
    domainId: string;
    /* usecase to use when resolving the top-level application (HxApplication MD) */
    /* but is not used when resolving entity-type specific components (HxViewWith MD) */
    usecases: string[];
    /* If prototypoing, this value is set in the URL, the servlet writes it as HostSettings */
    /* and Hydrux then sents it again to server when resolving HxApplication */
    prototypingModule: string;
    queryString: string;
}

declare var $tfUxHostSettings: HostSettings;

class IHxHostSettingsImpl implements hx.IHxHostSettings {
    servicesUrl(): string { return $tfUxHostSettings.servicesUrl; }
    webSocketUrl(): string { return $tfUxHostSettings.webSocketUrl; }
    domainId(): string { return $tfUxHostSettings.domainId; }
    usecases(): string[] { return $tfUxHostSettings.usecases; }
    queryString(): string { return $tfUxHostSettings.queryString; }
}

export class HxApplicationImpl implements hx.IHxApplication {

    private readonly hxApplication: hxM.HxApplication;
    private readonly servicesSession: remote.ServicesSession;
    private readonly hostSettings = new IHxHostSettingsImpl();

    private readonly platformModule: HxModuleImpl;
    private readonly moduleMap = new Map<UxModule, Promise<HxModuleImpl>>();
    private readonly cssResources = new Set<string>();

    private readonly hxEvaluator = new HxLocalEvaluator(this);

    private readonly pushChannelId: string;

    private rootScope: hx.IHxScope;

    /** protected */ componentToScope = new Map<hx.IHxComponent, HxScopeImpl>();


    /* We assume hxApplication.defaultScope.defaultDomain is defined */
    constructor(hxApplication: hxM.HxApplication, servicesSession: remote.ServicesSession) {
        this.hxApplication = hxApplication;
        this.servicesSession = servicesSession;
        this.platformModule = this.createPlatformModule();
        this.pushChannelId = util.newUuid();

        this.validateApp();
        this.openWebSocket();
    }

    private createPlatformModule(): HxModuleImpl {
        const jsModule = this.createPlatformJsModule();

        const uxModule = UxModule.create();
        uxModule.name = "hydrux-platform";

        return new HxModuleImpl(this, jsModule, uxModule);
    }

    private createPlatformJsModule(): JsModule {
        const appManager = this;
        return {
            contract: {
                bind(context: hx.IHxModuleBindingContext): void {
                    const componentBinder = context.componentBinder();

                    componentBinder.bindComponent(hxM.HxSession, appManager.newSession.bind(appManager));

                    componentBinder.bindComponent(hxM.HxAccessSessionFactory, appManager.newAccessSessionFactory.bind(appManager));
                    componentBinder.bindComponent(hxM.HxServiceSessionFactory, appManager.newServiceSessionFactory.bind(appManager));

                    componentBinder.bindComponent(hxM.HxApplicationDomainSupplier, appManager.newApplicationDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxStaticDomainSupplier, appManager.newStaticDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxUrlDomainSupplier, appManager.newUrlDomainSupplier.bind(appManager));
                    componentBinder.bindComponent(hxM.HxFallbackDomainSupplier, appManager.newFallbackDomainSupplier.bind(appManager));
                }
            }
        }
    }

    private validateApp(): void {
        if (!this.hxApplication)
            throw new Error("HxApplication cannot be null!!!");
        if (!this.hxApplication.rootScope)
            throw new Error("HxApplication.defaultScope is mandatory for now!!!");
    }

    private openWebSocket(): void {
        const wsUrl = this.wsUrl();
        console.log("WS URL: " + wsUrl);

        const webSocket = new WebSocket(wsUrl);

        webSocket.onopen = event => {
            console.log("WebSocket connection established to: " + wsUrl);
        }
        webSocket.onerror = error => {
            console.log("Error with WebSocket connection to: " + wsUrl);
        }
        webSocket.onclose = closeEvent => {
            console.log(`Lost WebSocket connection to: ${wsUrl}. Reason: ${closeEvent.reason}`);
        }
        webSocket.onmessage = messageEvent => {
            this.handleWsMessage(messageEvent)
        }
    }

    private wsUrl() {
        const params = new URLSearchParams();
        params.set("sessionId", this.servicesSession.sessionId());
        params.set("clientId", this.getApplicationId());
        params.set("pushChannelId", this.pushChannelId);
        params.set("accept", "gm/jse")

        return this.hostSettings.webSocketUrl() + "?" + params.toString();
    }

    private async handleWsMessage(messageEvent: MessageEvent<any>): Promise<void> {
        const entity = await this.servicesSession.decodeJse(messageEvent.data);
        if (ServiceRequest.isInstance(entity)) {
            const maybeResult = await (entity as ServiceRequest).EvalAndGetReasoned(this.getLocalEvaluator());
            if (!maybeResult.isSatisfied())
                console.error("[ERROR] Processing PushNotification failed because: " + maybeResult.whyUnsatisfied().text);
        } else {
            console.log("[WARNING] Received push notification that is not a ServiceRequest: " + entity);
        }
    }

    /** Performs the async part of initialization, which cannot be done in the constructor */
    async initAsync(): Promise<void> {
        this.rootScope = await this.createScope(this.hxApplication.rootScope, null);
    }

    getTitle(): string { return this.hxApplication.title; }
    getApplicationId(): string { return this.hxApplication.applicationId; }
    getLocalEvaluator(): eval_.Evaluator<ServiceRequest> { return this.hxEvaluator; }
    getServiceProcessorBinder(): hx.IHxServiceProcessorBinder { return this.hxEvaluator; }
    getServicesSession(): remote.ServicesSession { return this.servicesSession; }
    getEvaluator(): remote.EvaluatorBuilder { return this.servicesSession.evaluatorBuilder(); }
    getHostSettings(): hx.IHxHostSettings { return this.hostSettings; }
    getPushChannelId(): string { return this.pushChannelId; }

    newPushAddress(serviceId?: string): CallbackPushAddressing {
        const result = CallbackPushAddressing.create();
        result.pushChannelId = this.pushChannelId;
        result.serviceId = serviceId;
        return result;
    }

    loadCss(uxModule: UxModule, cssResourcePath: string): void {
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

    private async newSession(denotation: hxM.HxSession, context: hx.IHxComponentCreationContext): Promise<PersistenceGmSession> {
        const sessionFactory = await context.scope().resolveSessionFactory(denotation.factory);
        return sessionFactory.newSession();
    }

    /* ********************* */
    /* Access SessionFactory */
    /* ********************* */

    private async newAccessSessionFactory(denotation: hxM.HxAccessSessionFactory, context: hx.IHxComponentCreationContext): Promise<hx.IHxSessionFactory> {
        const accessId = await this.resolveSessionDomainId(denotation, context);

        // const dataModel = await this.getModel(GetAccessModel, accessId);

        // const accessDescriptor = new session.AccessDescriptor(accessId, dataModel, null /* accessDenotationType*/);
        // const sessionFactory = this.servicesSession.sessionFactory(accessDescriptor).build();

        const sessionFactory = await this.servicesSession.accessSessionFactory(accessId);

        return hx.IHxTools.sessionFactory(() => sessionFactory.get());
    }

    /* ********************** */
    /* Service SessionFactory */
    /* ********************** */

    private async newServiceSessionFactory(denotation: hxM.HxServiceSessionFactory, context: hx.IHxComponentCreationContext): Promise<hx.IHxSessionFactory> {
        const externalId = await this.resolveSessionDomainId(denotation, context);
        const sessionFactory = await this.servicesSession.serviceSessionFactory(externalId);

        return hx.IHxTools.sessionFactory(() => sessionFactory.get());
    }

    private async resolveSessionDomainId(denotation: hxM.HxServiceSessionFactory, context: hx.IHxComponentCreationContext): Promise<string> {
        return context.scope().resolveDomain(denotation.domain);
    }

    /* ***************** */
    /* Domain resolution */
    /* ***************** */

    private async newApplicationDomainSupplier(denotation: hxM.HxApplicationDomainSupplier, context: hx.IHxComponentCreationContext): Promise<hx.IHxDomainSupplier> {
        const result = this.valueOrReasoned($tfUxHostSettings.domainId, () => "DomainId not confiured in the internal $tfUxHostSettings variable.");
        return hx.IHxTools.domainSupplier(() => Promise.resolve(result));
    }

    private async newStaticDomainSupplier(denotation: hxM.HxStaticDomainSupplier, context: hx.IHxComponentCreationContext): Promise<hx.IHxDomainSupplier> {
        const result = this.valueOrReasoned(denotation.externalId, () => "Mandatory property HxStaticDomainSupplier.externalId is somehow null.");
        return hx.IHxTools.domainSupplier(() => Promise.resolve(result));
    }

    private async newUrlDomainSupplier(denotation: hxM.HxUrlDomainSupplier, context: hx.IHxComponentCreationContext): Promise<hx.IHxDomainSupplier> {
        return hx.IHxTools.domainSupplier(() => this.readDomainFromUrl(denotation.paramName));
    }

    private async readDomainFromUrl(paramName: string): Promise<Maybe<string>> {
        const urlParams = new URL(document.URL).searchParams;
        return this.valueOrReasoned(urlParams.get(paramName), () => "URL parameter not found: " + paramName);
    }

    private async newFallbackDomainSupplier(denotation: hxM.HxFallbackDomainSupplier, context: hx.IHxComponentCreationContext): Promise<hx.IHxDomainSupplier> {
        return hx.IHxTools.domainSupplier(() => this.resolveDomainFromSuppliers(denotation.suppliers, context.scope()));
    }

    private async resolveDomainFromSuppliers(suppliers: $tf.List<hxM.HxDomainSupplier>, scope: hx.IHxScope): Promise<Maybe<string>> {
        const subReasons = $tf.list(Reason.getTypeSignature());

        for (const supplierDenotation of suppliers.iterable()) {
            const supplier = await scope.resolveDomainSupplier(supplierDenotation);
            const maybeResult = await supplier.getDomain();

            if (maybeResult.isSatisfied())
                return maybeResult;

            subReasons.add(maybeResult.whyUnsatisfied());
        }

        const wrapperReason = hx.IHxTools.notFound("No supplier in the fallback chain provided a domain.");
        // TODO the nested messages are not read - maybe concatenate them on top level instead?
        wrapperReason.reasons.addAll(subReasons);

        return Maybe.empty(wrapperReason);
    }

    private valueOrReasoned<T>(value: T, msg: () => string): Maybe<T> {
        return value ? Maybe.complete(value) : hx.IHxTools.notFoundMaybe(msg());
    }

    /* ***************** */
    /*      Scopes       */
    /* ***************** */

    getRootScope(): hx.IHxScope {
        if (!this.rootScope)
            throw new Error("Root scope was not yet initialized. Weird, it should be done automatically via this.initAsync()");

        return this.rootScope;
    }

    async createScope(scopeDenotation: hxM.HxScope, parent: HxScopeImpl): Promise<HxScopeImpl> {
        const scope = new HxScopeImpl(this, scopeDenotation, parent)

        for (const controller of scopeDenotation.controllers.iterable())
            await scope.resolveComponent(controller);

        return scope;
    }

    getScope(component: hx.IHxComponent): Maybe<hx.IHxScope> {
        const scope = this.componentToScope.get(component);
        return scope ? Maybe.complete(scope) : Maybe.empty(NotFound.create());
    }

    releaseComponent(component: hx.IHxComponent): Maybe<boolean> {
        const scope = this.componentToScope.get(component);
        if (!scope)
            return hx.IHxTools.notFoundMaybe("Scope not found for component: " + component);

        this.componentToScope.delete(component);

        return scope.releaseComponent(component);
    }

    printComponent(component: hx.IHxComponent, includeScope: boolean): string {
        const scope = this.componentToScope.get(component);
        if (!scope)
            return "Unknown component, whose scope cannot be found.";

        const fromScope = (includeScope ? " from " + scope : "");

        const denotation = scope.findDenotationForImpl(component);
        if (!scope)
            return "Unknown component, whose denotation cannot be found" + fromScope + ".";

        return denotation + fromScope
    }


    /* ***************** */
    /* Module resolution */
    /* ***************** */

    /* Resolves JS UxMOdule to Promise<HxModule> */
    async resolveModule(uxModule: UxModule): Promise<HxModuleImpl> {
        if (uxModule == null)
            return this.platformModule;

        let result = this.moduleMap.get(uxModule);
        if (!result)
            this.moduleMap.set(uxModule, result = this.loadModule(uxModule));

        return result;
    }

    private async loadModule(uxModule: UxModule): Promise<HxModuleImpl> {
        const url = this.moduleUrl(uxModule);
        const jsModule = await import(url);
        return new HxModuleImpl(this, jsModule, uxModule);
    }

    private moduleUrl(uxModule: UxModule): string {
        const path = uxModule.path;
        if (path.startsWith("/") || path.includes(":"))
            return path
        else
            return addToPath(this.servicesSession.servicesUrl(), path);
    }

}

class HxModuleImpl {
    jsModule: JsModule;
    contract: hx.IHxModuleContract;
    componentRegistry: ComponentRegistry;

    constructor(app: HxApplicationImpl, jsModule: JsModule, uxModule: UxModule) {
        this.jsModule = jsModule;
        this.contract = jsModule.contract;
        this.componentRegistry = new ComponentRegistry(uxModule);

        const moduleBindingContext = {
            loadCss: app.loadCss.bind(app),
            currentUxModule: () => uxModule,
            componentBinder: () => this.componentRegistry,
            serviceProcessorBinder: () => app.getServiceProcessorBinder()
        }

        this.contract.bind(moduleBindingContext);
    }
}

interface JsModule {
    contract: hx.IHxModuleContract;
}

class ComponentRegistry implements hx.IHxComponentBinder {

    readonly uxModule: UxModule;
    readonly factoryMap = util.newDenotationMap<hxM.HxComponent, hx.IHxComponentFactory<hxM.HxComponent>>();

    constructor(uxModule: UxModule) {
        this.uxModule = uxModule;
    }
    bindView<T extends hxM.HxView>(componentType: reflection.EntityType<T>, factory: hx.IHxViewFactory<T>): void {
        this.bindComponent(componentType, factory);
    }

    bindRequestDialog<T extends hxM.HxRequestDialog>(componentType: reflection.EntityType<T>, factory: hx.IHxRequestDialogFactory<T>): void {
        this.bindComponent(componentType, factory);
    }

    bindComponent<T extends hxM.HxComponent>(componentType: EntityType<T>, factory: hx.IHxComponentFactory<T>): void {
        if (!componentType)
            throw new Error(`UxModule [${this.uxModule.name}] is trying to bind a component but given componentType is null.`);
        if (!factory)
            throw new Error(`UxModule [${this.uxModule.name}] is trying to bind null factory for component type: ${componentType.getTypeSignature()}`);

        this.factoryMap.put(componentType, factory);
    }

    resolveFactory<T extends hxM.HxComponent>(component: T): hx.IHxComponentFactory<T> {
        const result = this.factoryMap.find(component);
        if (!result)
            throw new Error(`No factory bound for component type '${component.EntityType().getTypeSignature()}' in UxModule: ${this.uxModule.name}`);

        return result as hx.IHxComponentFactory<T>;
    }

}

// Scope

export class HxScopeImpl implements hx.IHxScope {

    private readonly denotationToComponentImpl = new Map<hxM.HxComponent, hx.IHxComponent>();
    private readonly application: HxApplicationImpl;
    private readonly scopeDenotation: hxM.HxScope;
    private readonly parent: HxScopeImpl;

    private domainPromise: Promise<string>;

    constructor(applicationManager: HxApplicationImpl, scopeDenotation: hxM.HxScope, parent: HxScopeImpl) {
        this.application = applicationManager;
        this.scopeDenotation = scopeDenotation;
        this.parent = parent;
    }
    getApplication(): hx.IHxApplication {
        return this.application;
    }

    async getDomain(): Promise<string> {
        if (this.domainPromise)
            return this.domainPromise;

        if (this.scopeDenotation.defaultDomain) {
            const domainSupplier = await this.resolveDomainSupplier(this.scopeDenotation.defaultDomain);
            const maybeDomain = await domainSupplier.getDomain();

            if (!maybeDomain.isSatisfied())
                throw new Error(maybeDomain.whyUnsatisfied().text);

            return this.domainPromise = Promise.resolve(maybeDomain.get());
        }

        const rootScope = this.application.getRootScope();
        if (this != rootScope)
            return this.domainPromise = rootScope.getDomain();

        return this.domainPromise = Promise.resolve($tfUxHostSettings.domainId);
    }

    async resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string> {
        if (!supplierDenotation)
            return this.getDomain();

        const supplier = await this.resolveDomainSupplier(supplierDenotation);
        const maybeDomainId = await supplier.getDomain();

        if (!maybeDomainId.isSatisfied())
            throw new Error(`Unable to resolve domain id for domain supplier [${supplierDenotation}]. Reason: ${maybeDomainId.whyUnsatisfied().text}`)

        return maybeDomainId.get();
    }

    async resolveSession(sessionDenotation: hxM.HxSession): Promise<hx.IHxSessionHolder> {
        if (!sessionDenotation.factory)
            throw new Error("No factory configured for session: " + sessionDenotation);

        const factory = await this.resolveSessionFactory(sessionDenotation.factory);
        const session = factory.newSession();
        return { getSession: () => session };
    }
    async resolveSessionFactory(supplier: hxM.HxSessionFactory): Promise<hx.IHxSessionFactory> {
        return this.resolveComponent(supplier);
    }
    async resolveView<R extends hx.IHxView>(view: hxM.HxView): Promise<R> {
        return this.resolveComponent(view);
    }
    async resolveRequestDialog<R, D extends hx.IHxRequestDialog<any, R>>(requestDialog: hxM.HxRequestDialog): Promise<D> {
        return this.resolveComponent(requestDialog);
    }
    async resolveController<R extends hx.IHxController>(controller: hxM.HxController): Promise<R> {
        return this.resolveComponent(controller);
    }
    async resolveDataConsumer<R extends hx.IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<R> {
        return this.resolveComponent(dataConsumer);
    }
    async resolveSelectionEventSource<R extends hx.IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<R> {
        return this.resolveComponent(selectionEventSource);
    }
    async resolveDomainSupplier(domainSupplier: hxM.HxDomainSupplier): Promise<hx.IHxDomainSupplier> {
        return this.resolveComponent(domainSupplier);
    }
    async resolveComponent<R extends hx.IHxComponent>(componentDenotation: hxM.HxComponent): Promise<R> {
        const componentScope = await this.resolveComponentScope(componentDenotation.scope);
        return componentScope.resolveYourComponent(componentDenotation);
    }

    private async resolveYourComponent<R extends hx.IHxComponent>(componentDenotation: hxM.HxComponent): Promise<R> {
        let result = this.denotationToComponentImpl.get(componentDenotation);
        if (!result) {
            result = await this.createComponent(componentDenotation);
            this.denotationToComponentImpl.set(componentDenotation, result);
            this.application.componentToScope.set(result, this);
        }

        return result as R;
    }

    private async createComponent(componentDenotation: hxM.HxComponent): Promise<hx.IHxComponent> {
        // TODO make more lenient - if no, go through all modules and resolve the only module that actually binds our component type?

        const hxModule = await this.application.resolveModule(componentDenotation.module);
        const factory = hxModule.componentRegistry.resolveFactory(componentDenotation);
        return factory(componentDenotation, this.creationContext());
    }

    private creationContext(): hx.IHxComponentCreationContext {
        return {
            application: () => this.getApplication(),
            scope: () => this
        };
    }

    private async resolveComponentScope(scopeDenotation: hxM.HxScope): Promise<HxScopeImpl> {
        if (!scopeDenotation)
            return this;

        const predecessorScope = this.resolvePredecessorScope(scopeDenotation);
        if (predecessorScope)
            return predecessorScope;

        return this.application.createScope(scopeDenotation, this);
    }

    private resolvePredecessorScope(scopeDenotation: hxM.HxScope): HxScopeImpl {
        if (this.scopeDenotation == scopeDenotation)
            return this;
        else if (this.parent)
            return this.parent.resolvePredecessorScope(scopeDenotation);
        else
            return null;
    }

    releaseComponent(component: hx.IHxComponent): Maybe<boolean> {
        const maybeDenotation = this.findDenotationForImpl(component);
        if (!maybeDenotation.isSatisfied())
            return maybeDenotation.cast();

        // since [findDenotationForImpl] finds denotation via denotationToComponentImpl map, it must be there
        this.denotationToComponentImpl.delete(maybeDenotation.get())
        return Maybe.complete(true);
    }

    findDenotationForImpl(component: hx.IHxComponent): Maybe<hxM.HxComponent> {
        const entry = [...this.denotationToComponentImpl.entries()].find(({ 1: value }) => value === component);
        if (entry)
            return Maybe.complete(entry[0]);
        else
            return hx.IHxTools.notFoundMaybe("Component not found in what is allegedly it's scope. Component: " + component);
    }

    toString(): string {
        return "HxScope[" + this.scopePath() + "]";
    }

    scopePath(): string {
        return (this.parent ? this.parent.scopePath() + "/" : "") + this.scopeDenotation.name
    }

} /* HxScope */

export function addToPath(a: string, b: string): string {
    return a.endsWith("/") ? a + b : a + "/" + b;
}
