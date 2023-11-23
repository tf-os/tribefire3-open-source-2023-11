<#if !request.serviceProcessorSample>
    ${template.ignore()}
</#if>
<#import '/imports/context.ftl' as context>
${template.relocate(context.relocation(context.transformToUpperCaseTestFull))}
package ${context.processingPackage};

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;

import ${context.transformRequestFull};
import ${context.requestProcessorFull};
import ${context.testBaseFull};

/**
 * Processor: {@link ${context.requestProcessorSimple}}.
 * <p>
 * Request: {@link ${context.transformRequestSimple}}
 */
public class ${context.transformToUpperCaseTestSimple} extends ${context.testBaseSimple} {

	@Test
	public void emptyInputIsInvalid() throws Exception {
		${context.transformRequestSimple} request = ${context.transformRequestSimple}.T.create();

		Maybe<String> maybeText = request.eval(evaluator).getReasoned();

		assertThat(maybeText.isUnsatisfied()).isTrue();
		assertThat(maybeText.isUnsatisfiedBy(InvalidArgument.T)).isTrue();
	}

	@Test
	public void convertsText() {
		${context.transformRequestSimple} request = ${context.transformRequestSimple}.T.create();
		request.setText("Quoth the Raven, Nevermore");

		Maybe<String> maybeText = request.eval(evaluator).getReasoned();

		assertThat(maybeText.isSatisfied()).isTrue();
		assertThat(maybeText.get()).isEqualTo("QUOTH THE RAVEN, NEVERMORE");
	}

}
