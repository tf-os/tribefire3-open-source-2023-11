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
var _HxEvaluator_processors, _HxEvaluator_dispatchingRegistries, _HxEvalContext_hxEvaluator, _HxEvalContext_request, _HxEvalContext_mapAttrContext;
import { attr, eval_, reason, service, util } from "../tribefire.js.tf-js-api-2.3~/tf-js-api.js";
import { DispatchableRequest } from "../com.braintribe.gm.service-api-model-1.0~/ensure-service-api-model.js";
import { UnsupportedOperation } from "../com.braintribe.gm.essential-reason-model-1.0~/ensure-essential-reason-model.js";
var Maybe = reason.Maybe;
export class HxEvaluator extends eval_.EmptyEvaluator {
    constructor() {
        super(...arguments);
        _HxEvaluator_processors.set(this, util.newDenotationMap());
        _HxEvaluator_dispatchingRegistries.set(this, new Map());
    }
    eval(request) {
        return new HxEvalContext(this, request);
    }
    /*
     * ServiceProcessorBinder
     */
    bind(denotationType, processor) {
        __classPrivateFieldGet(this, _HxEvaluator_processors, "f").put(denotationType, processor);
        return {
            unbind: () => {
                __classPrivateFieldGet(this, _HxEvaluator_processors, "f").remove(denotationType);
            }
        };
    }
    bindDispatching(denotationType, serviceId, processor) {
        let dispatchingRegistry = this.acquireDispatchingRegistry(serviceId);
        if (!dispatchingRegistry)
            __classPrivateFieldGet(this, _HxEvaluator_dispatchingRegistries, "f").set(serviceId, dispatchingRegistry = new DispatchingRegistry());
        dispatchingRegistry.bind(denotationType, processor);
        return {
            unbind: () => {
                dispatchingRegistry.processors.remove(denotationType);
                if (dispatchingRegistry.processors.isEmpty())
                    __classPrivateFieldGet(this, _HxEvaluator_dispatchingRegistries, "f").delete(serviceId);
            }
        };
    }
    acquireDispatchingRegistry(serviceId) {
        let dispatchingRegistry = __classPrivateFieldGet(this, _HxEvaluator_dispatchingRegistries, "f").get(serviceId);
        if (!dispatchingRegistry)
            __classPrivateFieldGet(this, _HxEvaluator_dispatchingRegistries, "f").set(serviceId, dispatchingRegistry = new DispatchingRegistry());
        return dispatchingRegistry;
    }
    resolveProcessor(request) {
        let result;
        if (DispatchableRequest.isInstance(request))
            result = this.resolveDispatchingProcessor(request);
        if (!result)
            result = __classPrivateFieldGet(this, _HxEvaluator_processors, "f").find(request);
        return result ? Maybe.complete(result) : Maybe.empty(this.noProcessorFoundFor(request));
    }
    resolveDispatchingProcessor(request) {
        if (!request.serviceId)
            return null;
        const registry = __classPrivateFieldGet(this, _HxEvaluator_dispatchingRegistries, "f").get(request.serviceId);
        if (!registry)
            return null;
        return registry.processors.find(request);
    }
    noProcessorFoundFor(request) {
        let msg = "No ServiceProcessor found for request of type [" + request.EntityType().getTypeSignature() + "]";
        if (DispatchableRequest.isInstance(request)) {
            const dr = request;
            if (dr.serviceId)
                msg += " with serviceId [" + request.serviceId + "]";
            else
                msg += ", a DispatchableRequest with no serviceId (maybe that's the problem?)";
        }
        msg += " -- Request: " + request;
        return reason.build(UnsupportedOperation).text(msg).toReason();
    }
}
_HxEvaluator_processors = new WeakMap(), _HxEvaluator_dispatchingRegistries = new WeakMap();
class DispatchingRegistry {
    constructor() {
        this.processors = util.newDenotationMap();
    }
    bind(denotationType, processor) {
        this.processors.put(denotationType, processor);
    }
}
class HxEvalContext extends eval_.EmptyEvalContext {
    constructor(evaluator, request) {
        super();
        _HxEvalContext_hxEvaluator.set(this, void 0);
        _HxEvalContext_request.set(this, void 0);
        _HxEvalContext_mapAttrContext.set(this, new attr.MapAttributeContext(null));
        __classPrivateFieldSet(this, _HxEvalContext_hxEvaluator, evaluator, "f");
        __classPrivateFieldSet(this, _HxEvalContext_request, request, "f");
    }
    andGet() {
        return __awaiter(this, void 0, void 0, function* () {
            const maybe = yield this.andGetReasoned();
            return maybe.get();
        });
    }
    andGetReasoned() {
        const maybeProcessor = __classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f").resolveProcessor(__classPrivateFieldGet(this, _HxEvalContext_request, "f"));
        if (!maybeProcessor.isSatisfied())
            return Promise.resolve(maybeProcessor.cast());
        const processor = maybeProcessor.get();
        const context = this.newServiceRequestContext();
        return processor(context, __classPrivateFieldGet(this, _HxEvalContext_request, "f"));
    }
    getReasoned(callback) {
        this.andGetReasoned()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e));
    }
    get(callback) {
        this.andGet()
            .then(value => callback.onSuccess(value))
            .catch(e => callback.onFailure(e));
    }
    newServiceRequestContext() {
        const context = new service.StandardServiceRequestContext(this.parentAttributeContext(), __classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f"));
        context.setEvaluator(__classPrivateFieldGet(this, _HxEvalContext_hxEvaluator, "f"));
        for (const entry of this.streamAttributes().iterable())
            context.setAttribute(entry.attribute(), entry.value());
        return context;
    }
    parentAttributeContext() {
        const pac = eval_.ParentAttributeContextAspect.$.find(this);
        return pac.isPresent() ? pac.get() : attr.AttributeContexts.peek();
    }
    getReasonedSynchronous() {
        throw new Error("Synchronous evaluation is not supported!");
    }
    getSynchronous() {
        throw new Error("Synchronous evaluation is not supported!");
    }
    setAttribute(attribute, value) {
        __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").setAttribute(attribute, value);
    }
    findAttribute(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findAttribute(attribute);
    }
    getAttribute(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").getAttribute(attribute);
    }
    findOrDefault(attribute, defaultValue) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrDefault(attribute, defaultValue);
    }
    findOrNull(attribute) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrNull(attribute);
    }
    findOrSupply(attribute, defaultValueSupplier) {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").findOrSupply(attribute, defaultValueSupplier);
    }
    streamAttributes() {
        return __classPrivateFieldGet(this, _HxEvalContext_mapAttrContext, "f").streamAttributes();
    }
}
_HxEvalContext_hxEvaluator = new WeakMap(), _HxEvalContext_request = new WeakMap(), _HxEvalContext_mapAttrContext = new WeakMap();
// for local-only case (for now just push notifications) we set a RequestedEndpointAspect to imply no fallback via remotifier is desired
// otherwise, build an evaluator that tries locally first, if no processor found, either return reason (in case push notification) or
