import { eval_, reason, reflection } from "../tribefire.js.tf-js-api-2.3~/tf-js-api.js";
import { DispatchableRequest, ServiceRequest } from "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";
import { IHxServiceProcessor, IHxServiceProcessorBinder, IHxServiceProcessorBinding } from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import Maybe = reason.Maybe;
export declare class HxEvaluator extends eval_.EmptyEvaluator<ServiceRequest> implements IHxServiceProcessorBinder {
    #private;
    eval<T>(request: ServiceRequest): eval_.JsEvalContext<T>;
    bind<T extends ServiceRequest>(denotationType: reflection.EntityType<T>, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    bindDispatching<T extends DispatchableRequest>(denotationType: reflection.EntityType<T>, serviceId: string, processor: IHxServiceProcessor<T, any>): IHxServiceProcessorBinding;
    private acquireDispatchingRegistry;
    resolveProcessor(request: ServiceRequest): Maybe<IHxServiceProcessor<ServiceRequest, any>>;
    private resolveDispatchingProcessor;
    private noProcessorFoundFor;
}
