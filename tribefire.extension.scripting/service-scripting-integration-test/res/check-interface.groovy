import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.security.reason.SessionNotFound;

$tools.getLogger().info("Hello ScriptingIntegrationTest!");

String domain_id = $context.getDomainId();
String script_info = $tools.getScript().getSource().getName();
String deployable_info = $tools.getDeployable().getName();
String env_test = $tools.getRuntimeProperty("TRIBEFIRE_SERVICES_URL");

Map<String,String> info = new HashMap<>();
info["domain_id"] = domain_id;
info["script_name"] = script_info;
info["deployable_info"] = deployable_info;
info["env_test"] = env_test;
info["system_session"] = $tools.getSystemSessionFactory().newSession("cortex").getSessionAuthorization().getUserName();
if ($tools.isAuthenticated()) {
  info["session_factory"] = $tools.getSessionFactory().newSession("cortex").getSessionAuthorization().getUserName();
} else {
  $tools.abortWithReason(Reasons.build(SessionNotFound.T).text("request should be authorized").toReason());
}
info["created_entity"] = $tools.create("com.braintribe.gm.model.reason.essential.InvalidArgument").stringify();
info["type_signature"] = $tools.getTypeReflection().getType(String).getTypeSignature();
info["is_authenticated_via_AC"] = $tools.getAttributeContext().findOrNull(com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect.class) != null;

if ($request.mode == 0) {
  return info;
}

if ($request.mode == 1) {
  $tools.abortWithReason(Reasons.build(InvalidArgument.T).text("test error").toReason());
}

// other $request.mode -> Maybe

$tools.abortWithMaybe(Reasons.build(InvalidArgument.T).text("test error").toMaybe());

