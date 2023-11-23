import { eval_, reason, reflection, service } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import { IHxApplication, IHxServiceProcessor, IHxServiceProcessorBinder, IHxServiceProcessorBinding } from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import { DispatchableRequest, ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { HxRequestDialog } from "../tribefire.extension.hydrux.hydrux-deployment-model-2.1~/ensure-hydrux-deployment-model.js";
import Maybe = reason.Maybe;
export declare class HxLocalEvaluator extends eval_.EmptyEvaluator<ServiceRequest> implements IHxServiceProcessorBinder {
    #private;
    constructor(application: IHxApplication);
    eval<T>(request: ServiceRequest): eval_.JsEvalContext<T>;
    bindFunction<T extends ServiceRequest>(denotationType: reflection.EntityType<T>, processor: (context: service.ServiceRequestContext, request: T) => Promise<reason.Maybe<any>>): IHxServiceProcessorBinding;
    bind<T extends ServiceRequest>(denotationType: reflection.EntityType<T>, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    bindDispatchingFunction<T extends DispatchableRequest>(denotationType: reflection.EntityType<T>, serviceId: string, processor: (context: service.ServiceRequestContext, request: T) => Promise<reason.Maybe<any>>): IHxServiceProcessorBinding;
    bindDispatching<T extends DispatchableRequest>(denotationType: reflection.EntityType<T>, serviceId: string, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    private serviceProcessor;
    private acquireDispatchingRegistry;
    bindDialogProcessor<T extends ServiceRequest>(requestType: reflection.EntityType<T>, dialogDenotation: HxRequestDialog): IHxServiceProcessorBinding;
    cssClassNamesFor(dialogDenotation: HxRequestDialog): string[];
    resolveProcessor(request: ServiceRequest): Maybe<IHxServiceProcessor<ServiceRequest, any>>;
    private resolveDispatchingProcessor;
    private noProcessorFoundFor;
}
