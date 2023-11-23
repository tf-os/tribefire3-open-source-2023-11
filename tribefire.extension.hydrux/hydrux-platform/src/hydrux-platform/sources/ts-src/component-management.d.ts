import { eval_, reflection, remote, session, util } from "../tribefire.js.tf-js-api-2.3~/tf-js-api.js";
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import { ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { UxModule } from "../tribefire.extension.js.js-deployment-model-2.1~/ensure-js-deployment-model.js";
import EntityType = reflection.EntityType;
import PersistenceGmSession = session.PersistenceGmSession;
export declare class HostSettings {
    servicesUrl: string;
    webSocketUrl: string;
    domainId: string;
    usecases: string[];
    queryString: string;
}
export declare class ApplicationManager implements hx.IHxApplication {
    #private;
    private readonly hxApplication;
    private readonly servicesSession;
    private readonly hostSettings;
    private readonly platformModule;
    private readonly moduleMap;
    private readonly cssResources;
    private readonly hxEvaluator;
    private readonly pushChannelId;
    constructor(hxApplication: hxM.HxApplication, servicesSession: remote.ServicesSession);
    private createPlatformModule;
    private createPlatformJsModule;
    private validateApp;
    private openWebSocket;
    private wsUrl;
    private handleWsMessage;
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
    private reason;
    getRootScope(): Promise<hx.IHxScope>;
    createScope(scopeDenotation: hxM.HxScope, parent: HxScopeImpl): Promise<HxScopeImpl>;
    resolveModule(uxModule: UxModule): Promise<HxModuleImpl>;
    private loadModule;
    private moduleUrl;
}
export declare class HxModuleImpl {
    jsModule: JsModule;
    contract: hx.IHxModuleContract;
    componentRegistry: ComponentRegistry;
    constructor(jsModule: JsModule, uxModule: UxModule);
}
interface JsModule {
    contract: hx.IHxModuleContract;
}
declare class ComponentRegistry implements hx.IHxComponentBinder {
    readonly uxModule: UxModule;
    readonly factoryMap: util.MutableDenotationMap<hxM.HxComponent, hx.IHxComponentFactory<hxM.HxComponent>>;
    constructor(uxModule: UxModule);
    bindView<T extends hxM.HxView>(componentType: reflection.EntityType<T>, factory: hx.IHxViewFactory<T>): void;
    bindComponent<T extends hxM.HxComponent>(componentType: EntityType<T>, factory: hx.IHxComponentFactory<T>): void;
    resolveFactory<T extends hxM.HxComponent>(component: T): hx.IHxComponentFactory<T>;
}
export declare class HxScopeImpl implements hx.IHxScope {
    private readonly components;
    private readonly applicationManager;
    private readonly scopeDenotation;
    private readonly parent;
    private domainPromise;
    constructor(applicationManager: ApplicationManager, scopeDenotation: hxM.HxScope, parent: HxScopeImpl);
    getApplication(): hx.IHxApplication;
    getDomain(): Promise<string>;
    resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string>;
    resolveSession(session: hxM.HxSession): Promise<PersistenceGmSession>;
    resolveSessionFactory(supplier: hxM.HxSessionFactory): Promise<hx.IHxSessionSupplier>;
    resolveView<R extends hx.IHxView>(view: hxM.HxView): Promise<R>;
    resolveController<R extends hx.IHxController>(controller: hxM.HxController): Promise<R>;
    resolveDataConsumer<R extends hx.IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<R>;
    resolveSelectionEventSource<R extends hx.IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<R>;
    resolveDomainSupplier(domainSupplier: hxM.HxDomainSupplier): Promise<hx.IHxDomainSupplier>;
    resolveComponent<R extends hx.IHxComponent>(componentDenotation: hxM.HxComponent): Promise<R>;
    private resolveYourComponent;
    private createComponent;
    private resolveComponentScope;
    private resolvePredecessorScope;
}
export declare function addToPath(a: string, b: string): string;
export {};
