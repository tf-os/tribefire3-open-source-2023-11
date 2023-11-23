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

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.processing.template.preprocessing.TemplatePreprocessing;
import com.braintribe.model.template.Template;

public class TemplateEvaluationContext {

	private TemplatePreprocessing templatePreprocessing;
	private TemplateEvaluation templateEvaluation;
	private Template template;
	private boolean useFormular;
	private boolean cloneToPersistenceSession;

	public TemplatePreprocessing getTemplatePreprocessing() {
		return templatePreprocessing;
	}
	public void setTemplatePreprocessing(TemplatePreprocessing templatePreprocessing) {
		this.templatePreprocessing = templatePreprocessing;
	}
	public TemplateEvaluation getTemplateEvaluation() {
		return templateEvaluation;
	}
	public void setTemplateEvaluation(TemplateEvaluation templateEvaluation) {
		this.templateEvaluation = templateEvaluation;
	}
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	public boolean getUseFormular() {
		return useFormular;
	}
	public void setUseFormular(boolean useFormular) {
		this.useFormular = useFormular;
	}
	public void setCloneToPersistenceSession(boolean cloneToPersistenceSession) {
		this.cloneToPersistenceSession = cloneToPersistenceSession;
	}

	public <T> Future<T> evaluateTemplate() {
		return templateEvaluation.evaluateTemplate(cloneToPersistenceSession);
	}
}
