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
package com.braintribe.gwt.customization.client;

import com.braintribe.gwt.customization.client.tests.BasicItwTest;
import com.braintribe.gwt.customization.client.tests.DefaultMethodsTest;
import com.braintribe.gwt.customization.client.tests.EssentialMdTest;
import com.braintribe.gwt.customization.client.tests.EvaluatesToTest;
import com.braintribe.gwt.customization.client.tests.InitializerTest;
import com.braintribe.gwt.customization.client.tests.InstanceOfTest;
import com.braintribe.gwt.customization.client.tests.KeywordTest;
import com.braintribe.gwt.customization.client.tests.MethodsMultiInheritanceTest;
import com.braintribe.gwt.customization.client.tests.PartialModelTest;
import com.braintribe.gwt.customization.client.tests.SingleCharEnumTest;
import com.braintribe.gwt.customization.client.tests.ToStringTest;
import com.braintribe.gwt.customization.client.tests.TransientPropertyTest;
import com.braintribe.gwt.customization.client.tests.VirtualPropertyTest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.dom.client.Style.Unit;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StartupEntryPoint implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		new BasicItwTest().run();
		new VirtualPropertyTest().run();
		new SingleCharEnumTest().run();
		new EssentialMdTest().run();
		new InitializerTest().run();
		new MethodsMultiInheritanceTest().run();
		new DefaultMethodsTest().run();
		new ToStringTest().run();
		new EvaluatesToTest().run();
		new InstanceOfTest().run();
		new TransientPropertyTest().run();
		new PartialModelTest().run();
		new KeywordTest().run();

		// new GrindleboneTest().run();
		// new JseReadTest().run();
	}

	// @Override
	// public void onModuleLoad() {
	// ColorPicker colorPicker = new ColorPicker();
	// Picker[] pickers = new Picker[5]; // if this is IPicker, GWT does not work
	// pickers[0] = new Picker(123);
	// log("Hello World" + colorPicker.pickColor("peter") + pickers.length + " " + colorPicker);
	//
	// testPicker();
	//
	// JavascriptMemberReflection<IPicker> ipickerReflection = GWT.create(IPickerJsMemberReflection.class);
	// log("JsMethodNames: " + ipickerReflection.getJsMethodNames());
	//
	// }

	// @SuppressWarnings("cast")
	// private void testPicker() {
	// JavaScriptObject entityType = foo();
	//
	// IPicker ipicker = getDynamicPicker(entityType);
	// log("dynamic picker type: " + ipicker.pickerType());
	//
	// log("is dynamic instance of iface: " + (ipicker instanceof IPicker));
	//
	// Class<?> clazz = ipicker.getClass();
	//
	// log("class name: " + clazz.getName());
	// log("super class name: " + clazz.getSuperclass().getName());
	// }
	//
	// private IPicker getColorPicker() {
	// return new ColorPicker();
	// }

	public static void logSeparator() {
		Document document = Document.get();
		document.getBody().appendChild(document.createHRElement());
	}

	public static void log(String msg) {
		Document document = Document.get();
		PreElement preElement = document.createPreElement();
		preElement.appendChild(document.createTextNode(msg));
		preElement.getStyle().setMargin(0, Unit.PX);
		document.getBody().appendChild(preElement);
	}

	public static void logError(String msg) {
		Document document = Document.get();
		PreElement preElement = document.createPreElement();
		preElement.getStyle().setColor("red");
		preElement.getStyle().setMargin(0, Unit.PX);
		preElement.appendChild(document.createTextNode(msg));
		document.getBody().appendChild(preElement);
	}

// @formatter:off
//	private static native JavaScriptObject foo()
//	/*-{
//	    var empty_Constructor = @com.braintribe.gwt.customization.client.api.IPicker_Empty::new(); // just to make sure the class exists with all it's methods
//	    
//	    var clazz = @com.braintribe.gwt.customization.client.api.IPicker_Empty::class;
//	    var proto = @Class::getPrototypeForClass(Ljava/lang/Class;)(clazz);
//	    
//		var colorPickerConstructor = @ColorPicker::new();
//
//	 	var method = proto.@com.braintribe.gwt.customization.client.api.IPicker::pickerType();
//		console.log("method: " + method);
//		
//		var methodName;
//		for (var name in proto) {
//			if (method === proto[name]) {
//				methodName = name;
//				break;
//			}
//		}
//
//
//		var objClazz = @Object::class;
//	    var objProto = @Class::getPrototypeForClass(Ljava/lang/Class;)(objClazz);
//	    
//		//(String packageName, String compoundClassName,  JavaScriptObject typeId, Class<? super T> superclass)
//		var subClazz = @Class::createForClass(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/Class;)("com.braintribe.foo", "DynamicPicker", null, objClazz);
//		
//		var subProto = @com.google.gwt.lang.JavaClassHierarchySetupUtil::portableObjCreate(Lcom/google/gwt/core/client/JavaScriptObject;)(objProto);
//		subProto[methodName] = function() {
//			return "overridden picker type";
//		};
//		subProto.@Object::castableTypeMap = proto.@Object::castableTypeMap;
//		
//		subProto.@Object::___clazz = subClazz;  
//		
//		// TODO override toString 
//
//		var superConstructor = @StartupEntryPoint::extractConstructor(Lcom/google/gwt/core/client/JavaScriptObject;)(@Object::new());
//
//		var result = {};
//		result.clazz = subClazz;
//		
//		result.factory = function() {
//			superConstructor.call(this);
//		}
//		result.factory.prototype = subProto;
//		 
//		
//		//function() {
//			// return @com.google.gwt.lang.JavaClassHierarchySetupUtil::portableObjCreate(Lcom/google/gwt/core/client/JavaScriptObject;)(subProto);
//		//}
//		
//		return result; 
//	}-*/;
//
//	private static native IPicker getDynamicPicker(JavaScriptObject entityType)
//	/*-{
//	 	return new entityType.factory();
//	}-*/;
//
//	private static final String RETURN_NEW = "return new";
//
//	private static JavaScriptObject extractConstructor(JavaScriptObject superConstructorFunction) {
//		String source = superConstructorFunction.toString();
//
//		/* assert source like "function() { return new ${package}.${SuperclassName};}" (potentially with round brackets
//		 * following the constructor function) */
//
//		int s = source.indexOf(RETURN_NEW);
//		int e = source.lastIndexOf(';');
//
//		String superConstructor = source.substring(s + RETURN_NEW.length(), e).trim();
//
//		return resolveObjectPath(superConstructor);
//	}
//
//	private static native JavaScriptObject resolveObjectPath(String objectPath)
//	/*-{
//	 	return new Function("return " + objectPath)();
//	}-*/;
// @formatter:on

}
