<#if !request.serviceProcessorSample>
    ${template.ignore()}
</#if>
<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.requestProcessorFull))}
package ${context.processingPackage};

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;

import ${context.abstractRequestFull};
import ${context.transformRequestFull};

/**
 * {@link ${context.abstractRequestSimple}} processor.
 */
public class ${context.requestProcessorSimple} extends AbstractDispatchingServiceProcessor<${context.abstractRequestSimple}, Object> {

	@Override
	protected void configureDispatching(DispatchConfiguration<${context.abstractRequestSimple}, Object> dispatching) {
		dispatching.registerReasoned(${context.transformRequestSimple}.T, (c, r) -> transformToUpperCase(r));
	}

	private Maybe<String> transformToUpperCase(${context.transformRequestSimple} request) {
		String text = request.getText();

		if (text == null || text.isEmpty())
			// NOTE this is obviously not reasonable, it's just here to demonstrate error handling with Maybe and Reasons (InvalidArgument)
			return InvalidArgument.create("Cannot convert null or empty text to uppercase.").asMaybe();

		return Maybe.complete(text.toUpperCase());
	}

}
