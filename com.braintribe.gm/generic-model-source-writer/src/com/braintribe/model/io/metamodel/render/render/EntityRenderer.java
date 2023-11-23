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
package com.braintribe.model.io.metamodel.render.render;

import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.io.metamodel.render.context.EntityTypeContext;
import com.braintribe.model.io.metamodel.render.context.ImportManager;
import com.braintribe.model.io.metamodel.render.context.JavaType;
import com.braintribe.model.io.metamodel.render.context.PropertyDescriptor;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public class EntityRenderer extends CustomTypeRenderer {

	private final EntityTypeContext context;
	private final ImportManager im;

	public EntityRenderer(EntityTypeContext context) {
		this.context = context;
		this.im = context.importManager;
	}

	public String render() {
		printPackage(context.typeInfo.packageName);
		printImportGroups(context.importManager.getTypesToImportInGroups());
		printAnnotations(context.annotations);

		printTypeHeader();
		printTypeBody();
		printTypeEnd();

		return builder.toString();
	}

	private void printTypeHeader() {
		print("public interface ");
		print(context.typeInfo.simpleName);

		if (!context.superInterfaces.isEmpty()) {
			String superTypes = context.superInterfaces.stream() //
					.map(this::resolveTypeRef) //
					.collect(Collectors.joining(", "));
			print(" extends ", superTypes);
		}

		println(" {");
		println();
	}

	private void printTypeBody() {
		levelUp();

		printTypeLiteral();
		printPropertyNameLiterals();
		printGettersSetters();
		printEvaluatesTo();

		levelDown();
	}

	private void printTypeLiteral() {
		println(im.getTypeRef(EntityType.class), "<", context.typeInfo.simpleName, "> T = ", im.getTypeRef(EntityTypes.class), ".T(",
				context.typeInfo.simpleName, ".class);");
		println();
	}

	private void printPropertyNameLiterals() {
		boolean printed = false;

		for (PropertyDescriptor pd : context.properties)
			if (!pd.isInherited) {
				println("String ", escapePropertyName(pd.name), " = \"", pd.name, "\";");
				printed = true;
			}

		if (printed)
			println();
	}

	private void printGettersSetters() {
		for (PropertyDescriptor pd : context.properties)
			printGetterAndSetterFor(pd);
	}

	private void printGetterAndSetterFor(PropertyDescriptor pd) {
		for (String annotation : pd.annotations)
			println("@", im.getTypeRef(annotation));

		String typeRef = resolveTypeRef(pd.type);

		println(typeRef, " get", pd.getNameStartingWithUpperCase(), "();");
		println("void set", pd.getNameStartingWithUpperCase(), "(", typeRef, " ", escapePropertyNameIfKeyword(pd.name), ");");
		println();
	}

	private void printEvaluatesTo() {
		if (context.evaluatesTo == null)
			return;

		println("@", im.getTypeRef(Override.class));
		println(im.getTypeRef(EvalContext.class), "<", resolveTypeRef(context.evaluatesTo), "> eval(", im.getTypeRef(Evaluator.class), "<",
				im.getTypeRef(ServiceRequest.class), "> evaluator);");
		println();
	}

	private String resolveTypeRef(JavaType type) {
		if (type.isPrimitive)
			return type.rawType;

		if (type.keyType != null)
			return im.getTypeRef(type.rawType) + "<" + im.getTypeRef(type.keyType) + ", " + im.getTypeRef(type.valueType) + ">";

		if (type.valueType != null)
			return im.getTypeRef(type.rawType) + "<" + im.getTypeRef(type.valueType) + ">";

		return context.importManager.getTypeRef(type.rawType);
	}

	private static String escapePropertyName(String name) {
		return name.endsWith("_") ? name + "_" : escapePropertyNameIfKeyword(name);
	}

	private static String escapePropertyNameIfKeyword(String name) {
		return SourceVersion.isKeyword(name) ? name + "_" : name;
	}

}
