import { eval_, service, session, modelpath, remote, reason, reflection } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import * as jsM from "../tribefire.extension.js.js-deployment-model-3.0~/ensure-js-deployment-model.js";
import { DispatchableRequest, ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { NotFound } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
import EntityType = reflection.EntityType;
import Maybe = reason.Maybe;
import PersistenceGmSession = session.PersistenceGmSession;
export declare function hxApplication(): IHxApplication;
/**
 * Information based on the URL: ${servicesUrl}/hydrux/${domainId}/${usecases[0]}+${usecases[1]}+...
 * Notethe usecases part is optional.
 */
export interface IHxHostSettings {
    servicesUrl(): string;
    webSocketUrl(): string;
    domainId(): string;
    usecases(): string[];
    queryString(): string;
}
export interface IHxModuleContract {
    bind(context: IHxModuleBindingContext): void;
}
export interface IHxModuleBindingContext {
    loadCss(uxModule: jsM.UxModule, cssResourcePath: string): void;
    currentUxModule(): jsM.UxModule;
    componentBinder(): IHxComponentBinder;
    serviceProcessorBinder(): IHxServiceProcessorBinder;
}
export interface IHxComponentBinder {
    /** Binds a Component factory to given component type. */
    bindComponent<T extends hxM.HxComponent>(componentType: EntityType<T>, factory: IHxComponentFactory<T>): void;
    /** Same as this.bindComponent(...), but type-safe for HxView. */
    bindView<T extends hxM.HxView>(viewType: EntityType<T>, factory: IHxViewFactory<T>): void;
    /** Same as this.bindComponent(...), but type-safe for HxRequestDialog. */
    bindRequestDialog<T extends hxM.HxRequestDialog>(dialogType: EntityType<T>, factory: IHxRequestDialogFactory<T>): void;
}
export declare type IHxComponentFactory<T extends hxM.HxComponent> = (denotation: T, context: IHxComponentCreationContext) => Promise<IHxComponent>;
export declare type IHxViewFactory<T extends hxM.HxView> = (denotation: T, context: IHxComponentCreationContext) => Promise<IHxView>;
export declare type IHxRequestDialogFactory<T extends hxM.HxView> = (denotation: T, context: IHxComponentCreationContext) => Promise<IHxRequestDialog<any, any>>;
export interface IHxComponentCreationContext {
    application(): IHxApplication;
    scope(): IHxScope;
}
declare type IHxServiceProcessorFunction<SR extends ServiceRequest, R> = (context: service.ServiceRequestContext, request: SR) => Promise<Maybe<R>>;
export interface IHxServiceProcessor<SR extends ServiceRequest, R> {
    process(context: service.ServiceRequestContext, request: SR): Promise<Maybe<R>>;
}
export interface IHxServiceProcessorBinder {
    /** Binds a service processsor for given ServiceRequest type */
    bind<T extends ServiceRequest>(requestType: EntityType<T>, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    /** Binds a service processsor for given ServiceRequest type */
    bindFunction<T extends ServiceRequest>(requestType: EntityType<T>, processor: IHxServiceProcessorFunction<T, any>): IHxServiceProcessorBinding;
    /** Binds a service processsor for given ServiceRequest type and serviceId. */
    bindDispatching<T extends DispatchableRequest>(requestType: EntityType<T>, serviceId: string, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    /** Binds a service processsor for given ServiceRequest type and serviceId. */
    bindDispatchingFunction<T extends DispatchableRequest>(requestType: EntityType<T>, serviceId: string, processor: IHxServiceProcessorFunction<T, any>): IHxServiceProcessorBinding;
    /** Specifies that given requestType is handled according to given HxRequestDialog instance.
     * Such a dialog is rendered based on the binding on it's type via IHxComponentBinder.bindRequestDialog(...) */
    bindDialogProcessor<T extends ServiceRequest>(requestType: EntityType<T>, requestDialog: hxM.HxRequestDialog): IHxServiceProcessorBinding;
}
export interface IHxServiceProcessorBinding {
    unbind(): void;
}
export interface IHxApplication {
    /** Loads CSS file from given UxModule using cssResourcePath as a relative path within that module. More specifically,
     * a UxModule.path is a URL for the module's main js file (index.js). The cssResourcePath is relative to the main file's parent.
     * Calling this method with the same arguments more than once has no additional effect.*/
    loadCss(uxModule: jsM.UxModule, cssResourcePath: string): void;
    /** HxApplication.title */
    getTitle(): string;
    /** HxApplication.clientId */
    getApplicationId(): string;
    /** Random gibberish string which is used as identifier of this HX Application instance for push-requests via WebSocket.
     * See also "this.newPushRequest(...)"*/
    getPushChannelId(): string;
    /** Returns a CallbackPushAddressing instance which has its pushChannelId (and maybe also it's serviceId) set. */
    newPushAddress(serviceId?: string): CallbackPushAddressing;
    getServiceProcessorBinder(): IHxServiceProcessorBinder;
    getServicesSession(): remote.ServicesSession;
    getRootScope(): IHxScope;
    getEvaluator(): remote.EvaluatorBuilder;
    getLocalEvaluator(): eval_.Evaluator<ServiceRequest>;
    getHostSettings(): IHxHostSettings;
    /** Removes the component from the cache of it's IHxScope, and also removes such scope if it has no component left. */
    releaseComponent(component: IHxComponent): Maybe<boolean>;
    /** Returns the scope of given component if it wasn't closed. Otherwise: NotFound*/
    getScope(component: IHxComponent): Maybe<IHxScope>;
    printComponent(component: IHxComponent, includeScope: boolean): string;
}
export interface IHxScope {
    getApplication(): IHxApplication;
    getDomain(): Promise<string>;
    resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string>;
    resolveComponent<C extends IHxComponent>(component: hxM.HxComponent): Promise<C>;
    resolveView<V extends IHxView>(view: hxM.HxView): Promise<V>;
    resolveRequestDialog<R, D extends IHxRequestDialog<any, R>>(requestDialog: hxM.HxRequestDialog): Promise<D>;
    resolveController<C extends IHxController>(controller: hxM.HxController): Promise<C>;
    resolveDataConsumer<DC extends IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<DC>;
    resolveSelectionEventSource<SES extends IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<SES>;
    resolveSession(sessionDenotation: hxM.HxSession): Promise<IHxSessionHolder>;
    resolveSessionFactory(sessionFactory: hxM.HxSessionFactory): Promise<IHxSessionFactory>;
    resolveDomainSupplier(domainIdSupplier: hxM.HxDomainSupplier): Promise<IHxDomainSupplier>;
}
export interface IHxComponent {
    /** [optional] Closes this component.*/
    close?(): void;
}
export interface IHxDomainSupplier extends IHxComponent {
    getDomain(): Promise<Maybe<string>>;
}
export interface IHxSessionHolder extends IHxComponent {
    getSession(): PersistenceGmSession;
}
export interface IHxSessionFactory extends IHxComponent {
    newSession(): PersistenceGmSession;
}
export interface IHxView extends IHxComponent {
    htmlElement(): HTMLElement;
}
export interface IHxRequestDialog<SR extends ServiceRequest, R> extends IHxView, IHxServiceProcessor<SR, R> {
}
export declare class IHxTools {
    static view(lambda: () => HTMLElement): IHxView;
    static sessionFactory(lambda: () => PersistenceGmSession): IHxSessionFactory;
    static domainSupplier(lambda: () => Promise<Maybe<string>>): IHxDomainSupplier;
    static notFoundMaybe<T>(text: string): Maybe<T>;
    static notFound(msg: string): NotFound;
}
export interface IHxController extends IHxComponent {
}
export interface IHxSelection {
    firstSelectedItem(): modelpath.ModelPath;
    selectedItems(): modelpath.ModelPath[];
}
export interface IHxSelectionEventSource extends IHxComponent, IHxSelection {
    addSelectionListener(listener: IHxSelectionListener): void;
    removeSelectionListener(listener: IHxSelectionListener): void;
}
export interface IHxSelectionListener {
    onSelectionChanged(manager: IHxSelection): void;
}
export interface IHxDataConsumer extends IHxComponent {
    acceptData(modelpath: modelpath.ModelPath): void;
}
export {};
