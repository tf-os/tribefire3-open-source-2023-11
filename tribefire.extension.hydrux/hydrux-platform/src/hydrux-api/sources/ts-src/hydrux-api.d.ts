import { eval_, service, session, modelpath, remote, reason, reflection } from "../tribefire.js.tf-js-api-2.3~/tf-js-api.js";
import * as hxM from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import * as jsM from "../tribefire.extension.js.js-deployment-model-2.1~/ensure-js-deployment-model.js";
import { DispatchableRequest, ServiceRequest, CallbackPushAddressing } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
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
    componentBinder(): IHxComponentBinder;
    serviceProcessorBinder(): IHxServiceProcessorBinder;
}
export interface IHxComponentBinder {
    /** Hint: you can simply reference a method with proper signature as a ComponentFactory.
     *  Example: bindComponent(myModel.MyCustomComponent, this.createMyCustomComponent) */
    bindComponent<T extends hxM.HxComponent>(componentType: EntityType<T>, factory: IHxComponentFactory<T>): void;
    bindView<T extends hxM.HxView>(componentType: EntityType<T>, factory: IHxViewFactory<T>): void;
}
export declare type IHxComponentFactory<T extends hxM.HxComponent> = (denotation: T, context: IHxComponentCreationContext) => Promise<IHxComponent>;
export declare type IHxViewFactory<T extends hxM.HxView> = (denotation: T, context: IHxComponentCreationContext) => Promise<IHxView>;
export interface IHxComponentCreationContext {
    application(): IHxApplication;
    scope(): IHxScope;
}
export declare type IHxServiceProcessor<SR extends ServiceRequest, R> = (context: service.ServiceRequestContext, request: SR) => Promise<Maybe<R>>;
export interface IHxServiceProcessorBinder {
    /** Binds a service processsor for given ServiceRequest type */
    bind<T extends ServiceRequest>(denotationType: EntityType<T>, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    /** Binds a service processsor for given ServiceRequest type and serviceId. */
    bindDispatching<T extends DispatchableRequest>(denotationType: EntityType<T>, serviceId: string, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
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
    getRootScope(): Promise<IHxScope>;
    getEvaluator(): remote.EvaluatorBuilder;
    getLocalEvaluator(): eval_.Evaluator<ServiceRequest>;
    getHostSettings(): IHxHostSettings;
}
export declare type IHxSessionSupplier = () => PersistenceGmSession;
export interface IHxScope {
    getApplication(): IHxApplication;
    getDomain(): Promise<string>;
    resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string>;
    resolveComponent<R extends IHxComponent>(component: hxM.HxComponent): Promise<R>;
    resolveView<R extends IHxView>(view: hxM.HxView): Promise<R>;
    resolveController<R extends IHxController>(controller: hxM.HxController): Promise<R>;
    resolveDataConsumer<R extends IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<R>;
    resolveSelectionEventSource<R extends IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<R>;
    resolveSession(sessionDenotation: hxM.HxSession): Promise<PersistenceGmSession>;
    resolveSessionFactory(sessionFactory: hxM.HxSessionFactory): Promise<IHxSessionSupplier>;
    resolveDomainSupplier(domainIdSupplier: hxM.HxDomainSupplier): Promise<IHxDomainSupplier>;
}
export declare type IHxDomainSupplier = () => Promise<Maybe<string>>;
export interface IHxComponent {
}
export interface IHxView extends IHxComponent {
    htmlElement(): HTMLElement;
}
export interface IHxRequestDialog extends IHxView {
    dialogResult(): Promise<any>;
}
export declare class IHxViews {
    static create(htmlElementFactory: () => HTMLElement): IHxView;
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
