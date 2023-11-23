import { reason } from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import { NotFound } from "../com.braintribe.gm.essential-reason-model-2.0~/ensure-essential-reason-model.js";
var Maybe = reason.Maybe;
export function hxApplication() {
    return $tf.hydrux.application;
}
export class IHxTools {
    static view(lambda) {
        return { htmlElement: () => lambda() };
    }
    static sessionFactory(lambda) {
        return { newSession: () => lambda() };
    }
    static domainSupplier(lambda) {
        return { getDomain: () => lambda() };
    }
    static notFoundMaybe(text) {
        return Maybe.empty(IHxTools.notFound(text));
    }
    static notFound(msg) {
        return reason.build(NotFound).text(msg).toReason();
    }
}
