import "../com.braintribe.gm.service-api-model-2.0~/ensure-service-api-model.js";

export const meta = {
	groupId: "tribefire.cortex",
	artifactId: "job-api-model",
	version: "3.0.5-pc",
}

function modelAssembler($, P, _) {
//JSE version=4.0
//BEGIN_TYPES
P.a=$.T("com.braintribe.model.meta.GmMetaModel");
P.b=$.T("com.braintribe.model.meta.GmEntityType");
//END_TYPES
P.c=$.P(P.a,'name');P.d=$.P(P.a,'types');P.e=$.P(P.a,'version');P.f=$.P(P.b,'declaringModel');P.g=$.P(P.b,'globalId');P.h=$.P(P.b,'isAbstract');P.i=$.P(P.b,'superTypes');
P.j=$.P(P.b,'typeSignature');P.k=$.P(P.b,'evaluatesTo');
P.l=$.C(P.a);P.m=$.C(P.b);P.n=$.C(P.b);P.o=$.C(P.a);P.p=$.C(P.b);P.q=$.C(P.b);P.r=$.C(P.b);
_=P.l;
$.s(_,P.c,"tribefire.cortex:job-api-model");
$.s(_,P.d,$.S([P.m,P.n]));
$.s(_,P.e,"3.0.5-pc");
_=P.m;
$.s(_,P.f,P.o);
$.s(_,P.g,"type:com.braintribe.model.job.api.JobResponse");
$.s(_,P.h,$.n);
$.s(_,P.i,$.L([P.p]));
$.s(_,P.j,"com.braintribe.model.job.api.JobResponse");
_=P.n;
$.s(_,P.f,P.o);
$.s(_,P.k,P.m);
$.s(_,P.g,"type:com.braintribe.model.job.api.JobRequest");
$.s(_,P.h,$.n);
$.s(_,P.i,$.L([P.q,P.r]));
$.s(_,P.j,"com.braintribe.model.job.api.JobRequest");
_=P.o;
$.s(_,P.d,$.S([P.m,P.n]));
_=P.p;
$.s(_,P.h,$.n);
$.s(_,P.j,"com.braintribe.model.generic.GenericEntity");
_=P.q;
$.s(_,P.h,$.n);
$.s(_,P.j,"com.braintribe.model.service.api.PlatformRequest");
_=P.r;
$.s(_,P.h,$.n);
$.s(_,P.j,"com.braintribe.model.service.api.StandardRequest");
return P.l;
[1236];
}

$tf.reflection.internal.ensureModel(modelAssembler)

export const JobRequest = $T.com.braintribe.model.job.api.JobRequest;
export const JobResponse = $T.com.braintribe.model.job.api.JobResponse;
