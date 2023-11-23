<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.wireModuleFull))}
package ${context.wirePackage};

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import ${context.wireContractFull};
import com.braintribe.gm.service.access.wire.common.CommonAccessProcessingWireModule;
import com.braintribe.gm.service.wire.common.CommonServiceProcessingWireModule;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum ${context.wireModuleSimple} implements WireTerminalModule<${context.wireContractSimple}> {
	INSTANCE;

	@Override
	public List<WireModule> dependencies() {
		return asList(CommonServiceProcessingWireModule.INSTANCE, CommonAccessProcessingWireModule.INSTANCE);
	}

}
