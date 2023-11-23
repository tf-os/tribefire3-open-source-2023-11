<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.testBaseFull))}
package ${context.testBasePackage};

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ${context.wireModuleFull};
import ${context.wireContractFull};
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public abstract class ${context.testBaseSimple} {

	protected static WireContext<${context.wireContractSimple}> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static ${context.wireContractSimple} testContract;

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(${context.wireModuleSimple}.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
	}

	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}

}
