<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.wireContractFull))}
package ${context.wireContractPackage};

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.space.WireSpace;

public interface ${context.wireContractSimple} extends WireSpace {

	Evaluator<ServiceRequest> evaluator();

	PersistenceGmSessionFactory sessionFactory();

}