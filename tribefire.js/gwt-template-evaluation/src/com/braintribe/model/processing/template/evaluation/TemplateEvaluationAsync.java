// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.template.evaluation;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.vde.clone.async.DeferredExecutorAspect;
import com.braintribe.model.processing.vde.clone.async.SkipCloningPredicateAspect;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.UserNameAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.vd.ResolveVariables;

public class TemplateEvaluationAsync {

	private PersistenceGmSession targetSession;
	private Template template;
	private Map<String, Object> variableValues;
	private List<ModelPath> modelPaths;
	private ModelPath rootModelPath;
	private Supplier<String> userNameProvider = () -> null;
	private DeferredExecutor deferredExecutor = DeferredExecutor.gwtDeferredExecutor();

	public void setModelPaths(List<ModelPath> modelPaths) {
		this.modelPaths = modelPaths;
	}

	public void setRootModelPath(ModelPath rootModelPath) {
		this.rootModelPath = rootModelPath;
	}

	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}

	public void setTargetSession(PersistenceGmSession targetSession) {
		this.targetSession = targetSession;
	}

	public Template getTemplate() {
		return template;
	}
	
	public void setTemplate(Template template) {
		this.template = template;
	}

	public void setVariableValues(Map<String, Object> variableValues) {
		this.variableValues = variableValues;
	}

	public void setDeferredExecutor(DeferredExecutor deferredExecutor) {
		this.deferredExecutor = deferredExecutor;
	}

	public <T> Future<T> evaluateTemplate(boolean cloneToPersistenceSession) {
		Future<T> invalidStateResult = validate(cloneToPersistenceSession);
		if (invalidStateResult != null)
			return invalidStateResult;
		else
			return evaluateTemplateVds() //
					.andThenMap(evaluatedTempalte -> evaluateTemplate(evaluatedTempalte, cloneToPersistenceSession));
	}

	private Future<Template> evaluateTemplateVds() {
		Template templateToEval = Template.T.create();
		templateToEval.setPrototype(template.getPrototype());
		templateToEval.setScript(template.getScript());

		return resolveVd(templateToEval);
	}

	private <T> Future<T> validate(boolean cloneToPersistenceSession) {
		if (targetSession == null && (cloneToPersistenceSession || template.getScript() != null))
			return Future.fromError(new IllegalStateException("Cannot evaluate teplate. "));
		else
			return null;
	}

	private <T> Future<T> resolveVd(T vd) {
		VdeRegistry registry = VDE.extendedRegistry() //
				.withConcreteExpert(ResolveVariables.class, this::resolveVariables) //
				.done();

		return Future.fromAsyncCallbackConsumer(asyncCallback -> VDE.evaluate() //
				.withRegistry(registry) //
				.withEvaluationMode(VdeEvaluationMode.Preliminary) //
				.with(UserNameAspect.class, userNameProvider) //
				.with(SelectedModelPathsAspect.class, modelPaths) //
				.with(RootModelPathAspect.class, rootModelPath) //
				.with(VariableProviderAspect.class, this::resolveVariable) //
				.with(DeferredExecutorAspect.class, deferredExecutor) //
				.with(SkipCloningPredicateAspect.class, e -> e instanceof Variable) //
				.with(DeferredExecutorAspect.class, deferredExecutor) //
				.forValue(vd, asyncCallback));
	}

	private VdeResult resolveVariables(VdeContext context, ResolveVariables vd) {
		Object value = context.evaluate(vd.getValue());

		//@formatter:off
		ConfigurableCloningContext cc = 
			ConfigurableCloningContext.build()
			.withClonedValuePostProcesor(this::postProcessClonedVariable)
			.done();
		//@formatter:on
		Object result = BaseType.INSTANCE.clone(cc, value, null);

		return new VdeResultImpl(result, false);
	}

	private <T> T evaluateTemplate(Template evaluatedTemplate, boolean cloneToPersistenceSession) {
		T prototype = (T) evaluatedTemplate.getPrototype();

		Manipulation script = evaluatedTemplate.getScript();
		if (script != null)
			// Because the manipulations in the script are local ones, this modifies the "prototype" instance
			new TransientManipulatorContext().apply(script);

		if (targetSession != null && cloneToPersistenceSession)
			prototype = cloneToSession(prototype);

		return prototype;
	}

	private <T> T cloneToSession(T prototype) {
		ConfigurableCloningContext cc = ConfigurableCloningContext.build().supplyRawCloneWith(targetSession).done();
		return BaseType.INSTANCE.clone(cc, prototype, StrategyOnCriterionMatch.skip /*should never be relevant*/);
	}

	private Object postProcessClonedVariable(@SuppressWarnings("unused") GenericModelType propertyOrElementType, Object clonedValue) {
		return (clonedValue instanceof Variable) ? resolveVariable((Variable) clonedValue) : clonedValue;
	}

	private Object resolveVariable(Variable variable) {
		Object defaultValue = variable.getDefaultValue();
		return variableValues != null ? variableValues.getOrDefault(variable.getName(), defaultValue) : defaultValue;
	}

}
