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
package com.braintribe.devrock.zed.context;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.zed.analyze.BasicSignatureAnalyzer;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerProcessContext;
import com.braintribe.devrock.zed.api.context.ZedAnalyzerTypeReferenceContext;
import com.braintribe.devrock.zed.api.core.ZedSignatureAnalyzer;
import com.braintribe.devrock.zed.scan.asm.AnnotationTuple;
import com.braintribe.devrock.zed.scan.asm.ClassData;
import com.braintribe.devrock.zed.scan.asm.InheritanceData;
import com.braintribe.devrock.zed.scan.asm.MethodData;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

public class BasicZedAnalyzerProcessContext implements ZedAnalyzerProcessContext{
	private ZedAnalyzerContext context;
	private ZedEntity entity;
	private ClassData classData;
	private InheritanceData inheritanceData;
	private URL resourceURL;	
	private List<MethodData> methods;
	private List<AnnotationTuple> annotations;
	private ZedSignatureAnalyzer signatureExpert = new BasicSignatureAnalyzer();
	private Stack< ZedAnalyzerTypeReferenceContext> typeReferenceContext = new Stack<>();
	private ZedAnalyzerTypeReferenceContext classTypeReferenceContext;
	private Stack<String> selfReferenceTemplateKeyContext = new Stack<>();
	//private List<String> simpleDependencies;
	
	@Override
	public ZedAnalyzerContext context() {
		return context;
	}
	@Configurable
	public void setContext(ZedAnalyzerContext context) {
		this.context = context;
	}
	@Override
	public ZedEntity entity() {
		return entity;
	}
	@Configurable
	public void setEntity(ZedEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public ClassData classData() {
		return classData;
	}
	@Configurable
	public void setClassData(ClassData classData) {
		this.classData = classData;
	}
	
	@Override
	public InheritanceData inheritanceData() {
		return inheritanceData;
	}
	@Configurable
	public void setInheritanceData(InheritanceData inheritanceData) {
		this.inheritanceData = inheritanceData;
	}
	
	@Override
	public URL resourceURL() {
		return resourceURL;
	}
	@Configurable
	public void setResourceURL(URL resourceURL) {
		this.resourceURL = resourceURL;
	}	
	@Override
	public List<MethodData> methods() {	
		return methods;
	}
	@Configurable
	public void setMethods(List<MethodData> methods) {
		this.methods = methods;
	}
	
	@Override
	public ZedSignatureAnalyzer signatures() {
		if (signatureExpert == null) {
			signatureExpert = new BasicSignatureAnalyzer();
		}
		return signatureExpert;
	}
	@Configurable
	public void setSignatureExpert(ZedSignatureAnalyzer signatureExpert) {
		this.signatureExpert = signatureExpert;
	}
	
	@Override
	public List<AnnotationTuple> annotations() {
		return annotations;
	}
	@Configurable
	public void setAnnotations(List<AnnotationTuple> annotations) {
		this.annotations = annotations;
	}
	
	@Override
	public Map<String, TypeReferenceEntity> templateTypes() {		
		if (typeReferenceContext.size() > 0)
			return typeReferenceContext.peek().templateTypes();
		return Collections.emptyMap();						
	}
	@Override
	public void pushTypeReferences(Map<String, TypeReferenceEntity> references) {
		BasicZedAnalyzerTypeReferenceContext ct = new BasicZedAnalyzerTypeReferenceContext(references);
		typeReferenceContext.push( ct);		
	}
	@Override
	public void popTypeReferences() {
		if (!typeReferenceContext.isEmpty()) {
			typeReferenceContext.pop();
		}
		
	}
	@Override
	public Map<String, TypeReferenceEntity> classTemplateTypes() {
		if (classTypeReferenceContext == null) {
			return Collections.emptyMap();
		}
		return classTypeReferenceContext.templateTypes();
	}
	
	public void setClassTemplateTypes(Map<String, TypeReferenceEntity> references) {
		this.classTypeReferenceContext = new BasicZedAnalyzerTypeReferenceContext(references);		
	}
	@Override
	public String selfReferenceTemplateKey() {
		if (!selfReferenceTemplateKeyContext.isEmpty()) {
			return selfReferenceTemplateKeyContext.peek();
		}
		return null;
	}
	@Override
	public void pushSelfReferenceTemplateKey(String key) {
		selfReferenceTemplateKeyContext.push(key);
		
	}
	@Override
	public void popSelfReferenceTemplateKey() {
		if (!selfReferenceTemplateKeyContext.isEmpty()) {
			selfReferenceTemplateKeyContext.pop();
		}		
	}
	
	
	
	
	
}
