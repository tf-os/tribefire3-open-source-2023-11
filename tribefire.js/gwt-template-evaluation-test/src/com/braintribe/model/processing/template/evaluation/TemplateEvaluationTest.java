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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.testing.Futures;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.template.building.api.TemplateRecordingContext;
import com.braintribe.model.processing.template.building.impl.Templates;
import com.braintribe.model.processing.template.model.TestTemplate;
import com.braintribe.model.processing.template.preprocessing.TemplatePreprocessing;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.AsyncEvaluation;

public class TemplateEvaluationTest {

	private Template template;
	private TestTemplate evaluatedPrototype;

	private List<ModelPath> modelPaths;
	private ModelPath rootModelPath;
	private final Supplier<String> userNameProvider = () -> null;
	private final boolean cloneToPersistenceSession = true;
	private final PersistenceGmSession session = persistenceGmSession();

	private static final DeferredExecutor deferredExecutor = Futures.singleThreadedDeferredExecutor;

	private static boolean aysncEvaluation = true;

	@Test
	public void testProtoptypeWithVariable() throws Exception {
		prototypeWithVariableTemplate();

		doEvaluation();

		assertThat(evaluatedPrototype).isNotNull();
		assertThat(evaluatedPrototype.getObject()).isNotNull().isInstanceOf(Date.class);
	}

	private void prototypeWithVariableTemplate() {
		template = Templates.template(LocalizedString.create("test template")) //
				.prototype(ctx -> {
					Variable nowVar = Variable.T.create();
					nowVar.setName("nowVar");
					nowVar.setDefaultValue(Now.T.create());

					TestTemplate template = ctx.create(TestTemplate.T);
					template.setObject(nowVar);
					return template;
				}).build();
	}

	@Test
	public void testProtoptypeWithAbsenceInfo() throws Exception {
		prototypeWithAbsenceInfo();

		doEvaluation();

		assertThat(evaluatedPrototype).isNotNull();

		ValueDescriptor vd = evaluatedPrototype.entityType().getProperty("date").getVd(evaluatedPrototype);
		assertThat(vd).isInstanceOf(AbsenceInformation.class);

	}

	private void prototypeWithAbsenceInfo() {
		template = Templates.template(LocalizedString.create("test template")) //
				.prototype(ctx -> {
					TestTemplate template = ctx.create(TestTemplate.T);
					TestTemplate.T.getProperty("date").setVdDirect(template, GMF.absenceInformation());

					return template;
				}).build();
	}

	@Test
	public void testScriptWithVariable() throws Exception {
		dateTemplateWithScript();

		doEvaluation();

		assertThat(evaluatedPrototype).isNotNull();
	}

	private void dateTemplateWithScript() {
		template = Templates.template(LocalizedString.create("test template")) //
				.prototype(ctx -> ctx.create(TestTemplate.T)) //
				.record(this::recordScript) //
				.build();
	}

	// ###############################################
	// ## . . . . . . . . . Helpers . . . . . . . . ##
	// ###############################################

	private void doEvaluation() {
		evaluatedPrototype = Futures.waitForValue( //
				prepareTemplateEvaluationContext() //
						.andThenMapAsync(TemplateEvaluationContext::evaluateTemplate));
	}

	private void recordScript(TemplateRecordingContext<TestTemplate> ctx) {
		Variable nowVar = Variable.T.create();
		nowVar.setName("nowVar");
		nowVar.setDefaultValue(Now.T.create());

		ctx.pushVd(nowVar);
		ctx.getPrototype().setDate(null);
	}

	private Future<TemplateEvaluationContext> prepareTemplateEvaluationContext() {
		if (aysncEvaluation)
			template.getMetaData().add(AsyncEvaluation.T.create());

		return Future.fromSupplier(() -> templPreproc()) //
				.andThenMapAsync(TemplatePreprocessing::run) //
				.andThenMap(templPreproc -> {
					boolean useForm = true;

					TemplateEvaluation te = new TemplateEvaluation();
					te.setTemplate(template);
					te.setVariableValues(templPreproc.getVariableValues());
					te.setTargetSession(session);
					te.setModelPaths(modelPaths);
					te.setRootModelPath(rootModelPath);
					te.setUserNameProvider(userNameProvider);
					te.setDeferredExecutor(deferredExecutor);

					TemplateEvaluationContext result = new TemplateEvaluationContext();
					result.setUseFormular(useForm);
					result.setTemplatePreprocessing(templPreproc);
					result.setTemplateEvaluation(te);
					result.setTemplate(template);
					result.setCloneToPersistenceSession(cloneToPersistenceSession);

					return result;
				});

	}

	private TemplatePreprocessing templPreproc() {
		TemplatePreprocessing result = new TemplatePreprocessing();
		result.setModelPaths(modelPaths);
		result.setRootModelPath(rootModelPath);
		result.setTemplate(template);
		result.setDeferredExecutor(deferredExecutor);

		return result;
	}

	private PersistenceGmSession persistenceGmSession() {
		return new BasicPersistenceGmSession(new Smood(EmptyReadWriteLock.INSTANCE));
	}

}
