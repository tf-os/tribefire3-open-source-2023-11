import { eval_, reason, reflection, remote, util } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import { ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { UxModule } from "../tribefire.extension.js.js-deployment-model-3.0~/ensure-js-deployment-model.js";
import Maybe = reason.Maybe;
import EntityType = reflection.EntityType;
export declare class HostSettings {
    servicesUrl: string;
    webSocketUrl: string;
    domainId: string;
    usecases: string[];
    prototypingModule: string;
    queryString: string;
}
export declare class HxApplicationImpl implements hx.IHxApplication {
    private readonly hxApplication;
    private readonly servicesSession;
    private readonly hostSettings;
    private readonly platformModule;
    private readonly moduleMap;
    private readonly cssResources;
    private readonly hxEvaluator;
    private readonly pushChannelId;
    private rootScope;
    /** protected */ componentToScope: Map<hx.IHxComponent, HxScopeImpl>;
    constructor(hxApplication: hxM.HxApplication, servicesSession: remote.ServicesSession);
    private createPlatformModule;
    private createPlatformJsModule;
    private validateApp;
    private openWebSocket;
    private wsUrl;
    private handleWsMessage;
    /** Performs the async part of initialization, which cannot be done in the constructor */
    initAsync(): Promise<void>;
    getTitle(): string;
    getApplicationId(): string;
    getLocalEvaluator(): eval_.Evaluator<ServiceRequest>;
    getServiceProcessorBinder(): hx.IHxServiceProcessorBinder;
    getServicesSession(): remote.ServicesSession;
    getEvaluator(): remote.EvaluatorBuilder;
    getHostSettings(): hx.IHxHostSettings;
    getPushChannelId(): string;
    newPushAddress(serviceId?: string): CallbackPushAddressing;
    loadCss(uxModule: UxModule, cssResourcePath: string): void;
    private newSession;
    private newAccessSessionFactory;
    private newServiceSessionFactory;
    private resolveSessionDomainId;
    private newApplicationDomainSupplier;
    private newStaticDomainSupplier;
    private newUrlDomainSupplier;
    private readDomainFromUrl;
    private newFallbackDomainSupplier;
    private resolveDomainFromSuppliers;
    private valueOrReasoned;
    getRootScope(): hx.IHxScope;
    createScope(scopeDenotation: hxM.HxScope, parent: HxScopeImpl): Promise<HxScopeImpl>;
    getScope(component: hx.IHxComponent): Maybe<hx.IHxScope>;
    releaseComponent(component: hx.IHxComponent): Maybe<boolean>;
    printComponent(component: hx.IHxComponent, includeScope: boolean): string;
    resolveModule(uxModule: UxModule): Promise<HxModuleImpl>;
    private loadModule;
    private moduleUrl;
}
declare class HxModuleImpl {
    jsModule: JsModule;
    contract: hx.IHxModuleContract;
    componentRegistry: ComponentRegistry;
    constructor(app: HxApplicationImpl, jsModule: JsModule, uxModule: UxModule);
}
interface JsModule {
    contract: hx.IHxModuleContract;
}
declare class ComponentRegistry implements hx.IHxComponentBinder {
    readonly uxModule: UxModule;
    readonly factoryMap: util.MutableDenotationMap<hxM.HxComponent, hx.IHxComponentFactory<hxM.HxComponent>>;
    constructor(uxModule: UxModule);
    bindView<T extends hxM.HxView>(componentType: reflection.EntityType<T>, factory: hx.IHxViewFactory<T>): void;
    bindRequestDialog<T extends hxM.HxRequestDialog>(componentType: reflection.EntityType<T>, factory: hx.IHxRequestDialogFactory<T>): void;
    bindComponent<T extends hxM.HxComponent>(componentType: EntityType<T>, factory: hx.IHxComponentFactory<T>): void;
    resolveFactory<T extends hxM.HxComponent>(component: T): hx.IHxComponentFactory<T>;
}
export declare class HxScopeImpl implements hx.IHxScope {
    private readonly denotationToComponentImpl;
    private readonly application;
    private readonly scopeDenotation;
    private readonly parent;
    private domainPromise;
    constructor(applicationManager: HxApplicationImpl, scopeDenotation: hxM.HxScope, parent: HxScopeImpl);
    getApplication(): hx.IHxApplication;
    getDomain(): Promise<string>;
    resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string>;
    resolveSession(sessionDenotation: hxM.HxSession): Promise<hx.IHxSessionHolder>;
    resolveSessionFactory(supplier: hxM.HxSessionFactory): Promise<hx.IHxSessionFactory>;
    resolveView<R extends hx.IHxView>(view: hxM.HxView): Promise<R>;
    resolveRequestDialog<R, D extends hx.IHxRequestDialog<any, R>>(requestDialog: hxM.HxRequestDialog): Promise<D>;
    resolveController<R extends hx.IHxController>(controller: hxM.HxController): Promise<R>;
    resolveDataConsumer<R extends hx.IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<R>;
    resolveSelectionEventSource<R extends hx.IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<R>;
    resolveDomainSupplier(domainSupplier: hxM.HxDomainSupplier): Promise<hx.IHxDomainSupplier>;
    resolveComponent<R extends hx.IHxComponent>(componentDenotation: hxM.HxComponent): Promise<R>;
    private resolveYourComponent;
    private createComponent;
    private creationContext;
    private resolveComponentScope;
    private resolvePredecessorScope;
    releaseComponent(component: hx.IHxComponent): Maybe<boolean>;
    findDenotationForImpl(component: hx.IHxComponent): Maybe<hxM.HxComponent>;
    toString(): string;
    scopePath(): string;
}
export declare function addToPath(a: string, b: string): string;
export {};
