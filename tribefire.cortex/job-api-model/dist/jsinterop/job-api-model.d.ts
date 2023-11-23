/// <reference path="../com.braintribe.gm.service-api-model-2.0~/service-api-model.d.ts" />

declare namespace $T.com.braintribe.model.job.api {

	const JobRequest: $tf.reflection.EntityType<JobRequest>;
	interface JobRequest extends $T.com.braintribe.model.service.api.PlatformRequest, $T.com.braintribe.model.service.api.StandardRequest {
		Eval(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): $tf.eval.JsEvalContext<JobResponse>;
		EvalAndGet(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<JobResponse>;
		EvalAndGetReasoned(evaluator: $tf.eval.Evaluator<$T.com.braintribe.model.service.api.ServiceRequest>): Promise<$tf.reason.Maybe<JobResponse>>;
	}

	const JobResponse: $tf.reflection.EntityType<JobResponse>;
	interface JobResponse extends $T.com.braintribe.model.generic.GenericEntity {
	}

}

