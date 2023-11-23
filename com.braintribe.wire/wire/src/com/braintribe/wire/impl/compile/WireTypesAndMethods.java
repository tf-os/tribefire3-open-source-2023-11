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
package com.braintribe.wire.impl.compile;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.wire.api.EnrichedWireSpace;
import com.braintribe.wire.api.ImportFieldRecorder;
import com.braintribe.wire.api.annotation.Bean;
import com.braintribe.wire.api.annotation.Beans;
import com.braintribe.wire.api.annotation.Enriched;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.InternalWireContext;
import com.braintribe.wire.api.scope.BeanConfiguration;
import com.braintribe.wire.api.scope.DefaultScope;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.InstanceParameterization;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.scope.caller.CallerScope;
import com.braintribe.wire.impl.scope.prototype.PrototypeScope;
import com.braintribe.wire.impl.scope.referee.AggregateScope;
import com.braintribe.wire.impl.scope.singleton.SingletonScope;

public interface WireTypesAndMethods {
	TypeInfo objectType = new TypeInfo(Object.class);
	
	TypeInfo voidType = new TypeInfo(void.class);
	
	TypeInfo booleanType = new TypeInfo(boolean.class);
	TypeInfo byteType = new TypeInfo(byte.class);
	TypeInfo shortType = new TypeInfo(short.class);
	TypeInfo intType = new TypeInfo(int.class);
	TypeInfo longType = new TypeInfo(long.class);
	TypeInfo floatType = new TypeInfo(float.class);
	TypeInfo doubleType = new TypeInfo(double.class);
	TypeInfo charType = new TypeInfo(char.class);
	
	TypeInfo booleanBoxType = new TypeInfo(Boolean.class);
	TypeInfo byteBoxType = new TypeInfo(Byte.class);
	TypeInfo shortBoxType = new TypeInfo(Short.class);
	TypeInfo intBoxType = new TypeInfo(Integer.class);
	TypeInfo longBoxType = new TypeInfo(Long.class);
	TypeInfo floatBoxType = new TypeInfo(Float.class);
	TypeInfo doubleBoxType = new TypeInfo(Double.class);
	TypeInfo charBoxType = new TypeInfo(Character.class);

	MethodDescriptor booleanBoxMethod = booleanBoxType.method("valueOf").par(booleanType).ret(booleanBoxType);
	MethodDescriptor byteBoxMethod = byteBoxType.method("valueOf").par(byteType).ret(byteBoxType);
	MethodDescriptor shortBoxMethod = shortBoxType.method("valueOf").par(shortType).ret(shortBoxType);
	MethodDescriptor intBoxMethod = intBoxType.method("valueOf").par(intType).ret(intBoxType);
	MethodDescriptor longBoxMethod = longBoxType.method("valueOf").par(longType).ret(longBoxType);
	MethodDescriptor floatBoxMethod = floatBoxType.method("valueOf").par(floatType).ret(floatBoxType);
	MethodDescriptor doubleBoxMethod = doubleBoxType.method("valueOf").par(doubleType).ret(doubleBoxType);
	MethodDescriptor charBoxMethod = charBoxType.method("valueOf").par(charType).ret(charBoxType);

	TypeInfo stringType = new TypeInfo(String.class);
	TypeInfo classType = new TypeInfo(Class.class);
	TypeInfo throwableType = new TypeInfo(Throwable.class);
	
	MethodDescriptor objectConstructor = objectType.constructor().ret(voidType);

	TypeInfo wireSpaceType = new TypeInfo(WireSpace.class);

	TypeInfo enrichedType = new TypeInfo(Enriched.class);

	TypeInfo listType = new TypeInfo(List.class);
	TypeInfo arrayListType = new TypeInfo(ArrayList.class);

	TypeInfo instanceConfigurationType = new TypeInfo(InstanceConfiguration.class);
	MethodDescriptor currentInstanceMethod = instanceConfigurationType.staticMethod("currentInstance")
			.ret(instanceConfigurationType);
	
	TypeInfo instanceHolderType = new TypeInfo(InstanceHolder.class);
	
	MethodDescriptor wovenCurrentInstanceMethod = instanceHolderType.method("config")
			.ret(instanceConfigurationType);

	TypeInfo instanceHolderSupplierType = new TypeInfo(InstanceHolderSupplier.class);

	TypeInfo instanceParameterizationType = new TypeInfo(InstanceParameterization.class);


	TypeInfo wireContextType = new TypeInfo(InternalWireContext.class);

	MethodDescriptor lockCreationMethod = wireContextType.method("lockCreation").par(instanceHolderType)
			.ret(booleanType);
	MethodDescriptor unlockCreationMethod = wireContextType.method("unlockCreation").par(instanceHolderType)
			.ret(voidType);

	TypeInfo wireScopeType = new TypeInfo(WireScope.class);
	MethodDescriptor createHolderSupplierMethod = wireScopeType.method("createHolderSupplier")
			.par(wireSpaceType)
			.par(stringType)
			.par(instanceParameterizationType)
			.ret(instanceHolderSupplierType);

	MethodDescriptor getScopeMethod = wireContextType.method("getScope").par(classType).ret(wireScopeType);
	
	MethodDescriptor arrayListConstructorMethod = arrayListType.constructor().par(intType).ret(voidType);
	MethodDescriptor addMethod = listType.method("add").par(objectType).ret(booleanType);
	MethodDescriptor getHolderMethod = instanceHolderSupplierType.method("getHolder").par(objectType).ret(instanceHolderType);

	TypeInfo defaultScopeType = new TypeInfo(DefaultScope.class);
	TypeInfo singletonScopeType = new TypeInfo(SingletonScope.class);
	TypeInfo prototypeScopeType = new TypeInfo(PrototypeScope.class);
	TypeInfo aggregateScopeType = new TypeInfo(AggregateScope.class);
	TypeInfo callerScopeType = new TypeInfo(CallerScope.class);

	MethodDescriptor wireSpaceConstructor = wireSpaceType.constructor().par(wireContextType).ret(voidType);

	TypeInfo managedType = new TypeInfo(Managed.class);

	MethodDescriptor onPostConstructMethod = instanceHolderType.method("onPostConstruct").par(objectType)
			.ret(void.class);
	MethodDescriptor publishMethod = instanceHolderType.method("publish").par(objectType).ret(voidType);
	MethodDescriptor onCreationFailureMethod = instanceHolderType.method("onCreationFailure").par(throwableType).ret(voidType);
	MethodDescriptor getMethod = instanceHolderType.method("get").ret(objectType);

	// legacy types and methods
	TypeInfo beanType = new TypeInfo(Bean.class);
	TypeInfo beansType = new TypeInfo(Beans.class);

	TypeInfo beanConfigurationType = new TypeInfo(BeanConfiguration.class);
	MethodDescriptor currentBeanMethod = beanConfigurationType.staticMethod("currentBean").ret(beanConfigurationType);
	MethodDescriptor currentBeanAdaptMethod = beanConfigurationType.staticMethod("adapt").par(instanceConfigurationType)
			.ret(beanConfigurationType);
	
	TypeInfo importType = new TypeInfo(Import.class);
	
	TypeInfo enrichedWireSpaceType = new TypeInfo(EnrichedWireSpace.class);
	TypeInfo classArrayType = new TypeInfo(Class[].class);
	TypeInfo importFieldRecorderType = new TypeInfo(ImportFieldRecorder.class);
	MethodDescriptor recordMethod = importFieldRecorderType.method("record").par(classType).par(classType).par(intType).ret(voidType);
	
//	MethodDescriptor listImportFieldsMethod = enrichedWireSpaceType.method("__listImportFields").opcode(Opcodes.INVOKESPECIAL).par(importFieldRecorderType).ret(voidType);
//	MethodDescriptor setImportFieldMethod = enrichedWireSpaceType.method("__setImportField").opcode(Opcodes.INVOKESPECIAL).par(classType).par(intType).par(objectType).ret(voidType);
	MethodDescriptor listImportFieldsMethod = enrichedWireSpaceType.method("__listImportFields").par(importFieldRecorderType).ret(voidType);
	MethodDescriptor setImportFieldMethod = enrichedWireSpaceType.method("__setImportField").par(classType).par(intType).par(objectType).ret(voidType);
	
	MethodDescriptor getSuperclassMethod = classType.method("getSuperclass").ret(classType);
	MethodDescriptor isAssignableFromMethod = classType.method("isAssignableFrom").par(classType).ret(booleanType);
	
	TypeInfo illegalArgumentExceptionType = new TypeInfo(IllegalArgumentException.class);
	MethodDescriptor illegalArgumentExceptionConstructor = illegalArgumentExceptionType.constructor().par(stringType).ret(voidType);
}
