// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build.context;

import static com.braintribe.model.generic.reflection.TypeCode.floatType;
import static com.braintribe.model.generic.reflection.TypeCode.integerType;

import java.util.EnumSet;
import java.util.Set;

import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.itw.InitializerTools;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;

import jsinterop.context.JsKeywords;

public class PropertyDesc {

	public String typeRef;
	public String returnType;
	public String originalType;
	public String name;
	public String Name;
	public JType jType;
	public EntityDesc ownerTypeDesc;
	public JClassType declaringType; // Type where we want to declare our property, i.e. might be an overlay
	public EntityDesc declaringTypeDesc;
	public boolean isOverlay; // true iff declaringTypeDesc != ownerTypeDesc
	public String defaultLiteral;
	public boolean isPrimitive;
	public boolean isConfidential;
	public String initializerString;
	public String initializerLiteralOrSupplier; 
	public TypeCode collectionTypeCode = null;
	public TypeCode elementTypeCode;

	public String getSingletonInstanceRef() {
		return getPropertyWrapperClassName() + "." + getLiteralName();
	}

	public String getSingletonInstanceJsniRef() {
		return "@" + getPropertyWrapperClassName() + "::" + getLiteralName();
	}

	private String getPropertyWrapperClassName() {
		return declaringTypeDesc.getEnhancedClassFullName() + "._Properties";
	}

	public String virtualPropertyName() {
		return JsKeywords.javaIdentifierToJs(name);
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public String getNullableFlag() {
		return boolToString(!isPrimitive);
	}

	public String getConfidentialFlag() {
		return boolToString(isConfidential);
	}
	
	private String boolToString(boolean b) {
		return b ? "true" : "false";
	}
	
	public String getDefaultLiteral() {
		return defaultLiteral;
	}

	public JType getDeclaringType() {
		return declaringType;
	}

	public EntityDesc getDeclaringTypeDesc() {
		return declaringTypeDesc;
	}

	public boolean getIsInheritedFromSuperclass() {
		return ownerTypeDesc.isInheritedFromSuperclass(name);
	}

	public boolean getIsOverlay() {
		return isOverlay;
	}

	public JType getJType() {
		return jType;
	}

	public String getTypeRef() {
		return typeRef;
	}

	public void setOriginalType(String originalType) {
		this.originalType = originalType;
		String elementType = originalType;
		String[] s = this.originalType.split("<");
		if (s.length > 1) {
			elementType = s[1];
			String collectionType = s[0];
			if (collectionType.startsWith("java.util.List")) {
				collectionTypeCode = TypeCode.listType;
			} else if (collectionType.startsWith("java.util.Set")) {
				collectionTypeCode = TypeCode.setType;
			} else if (collectionType.startsWith("java.util.Map")) {
				collectionTypeCode = TypeCode.mapType;
			}
		}

		if (elementType.startsWith("java.lang.Integer")|| elementType.startsWith("int")) {
			elementTypeCode = TypeCode.integerType;
		} else if (elementType.startsWith("java.lang.Float")|| elementType.startsWith("float")) {
			elementTypeCode = TypeCode.floatType;
		} else if (elementType.startsWith("java.lang.Double")|| elementType.startsWith("double")) {
			elementTypeCode = TypeCode.doubleType;
		} else if (elementType.startsWith("java.lang.Long") || elementType.startsWith("long")) {
			elementTypeCode = TypeCode.longType;
		} else if (elementType.startsWith("java.math.BigDecimal")) {
			elementTypeCode = TypeCode.decimalType;
		}
	}

	public String getOriginalType() {
		return originalType;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getName() {
		return name;
	}

	public String getLiteralName() {
		return Name;
	}

	public String getSetterName() {
		return "set" + Name;
	}

	public String getGetterName() {
		return "get" + Name;
	}

	/** Might also be "null", i.e. this can be used always in the code. */
	public String getInitializerLiteralOrSupplier() {
		return initializerLiteralOrSupplier;
	}

	public boolean getHasNonNullInitializer() {
		return initializerString != null && !InitializerTools.NULL_STRING.equals(initializerString);
	}

	public static String firstLetterToUpperCase(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static final Set<TypeCode> jsAutoboxingTypeCodes = EnumSet.of(integerType, floatType);

	private boolean isJsAutoboxingProperty() {
		return !isPrimitive() && !isCollectionProperty() && jsAutoboxingTypeCodes.contains(elementTypeCode);
	}

	public boolean isCollectionProperty() {
		return collectionTypeCode != null;
	}

	@SuppressWarnings("unused")
	public String getGetterPrototype(String sourceName, String getterName, String propertyName) {
		if (isJsAutoboxingProperty())
			return "get:function(){return @com.braintribe.gwt.genericmodel.client.itw.GwtScriptProperty::unboxJavaNumberToJsPrimitive(Ljava/lang/Number;)("+
				"this.@" + sourceName + "::" + getterName + "()());}";
		else if (isPrimitiveLong()) 
			return "get:function(){return @java.lang.Long::valueOf(J)(this.@" + sourceName + "::" + getterName + "()());}";
		else
			return "get:prototype.@" + sourceName + "::" + getterName + "()";
	}

	private boolean isPrimitiveLong() {
		return isPrimitive() && elementTypeCode == TypeCode.longType;
	}

	public String getSetterPrototype(String sourceName, String setterName, String typeSignature, String propertyName) {
		if (isJsAutoboxingProperty())
			return getBoxingSetterPrototype(sourceName, setterName, typeSignature, propertyName);
		else if (isPrimitiveLong())
			return "set:function(v){" + //
					"@com.braintribe.gwt.genericmodel.client.itw.GwtScriptProperty::exceptionIfNumber(*)(v, '" + propertyName + "');" + //
					"this.@" + sourceName + "::" + setterName + "(" + typeSignature + ")(v.@java.lang.Long::longValue()());}";
		else
			return "set:prototype.@" + sourceName + "::" + setterName + "(" + typeSignature + ")";
	}

	@SuppressWarnings("unused")
	private String getBoxingSetterPrototype(String sourceName, String setterName, String typeSignature, String propertyName) {
		StringBuilder b = new StringBuilder();
		b.append("set:function(v){");
		b.append("this.@");
		b.append(sourceName);
		b.append("::");
		b.append(setterName);
		b.append("(");
		b.append(typeSignature);
		b.append(")(@com.braintribe.gwt.genericmodel.client.itw.GwtScriptProperty::boxJsNumberToJava");

		switch (elementTypeCode) {
			case integerType:
				b.append("Integer");
				break;
			case floatType:
				b.append("Float");
				break;
			default:
				throw new IllegalStateException("unsupported js boxing for " + elementTypeCode);
		}
		
		b.append("(Ljava/lang/Double;)(v));}");
		
		return b.toString();
	}

}
