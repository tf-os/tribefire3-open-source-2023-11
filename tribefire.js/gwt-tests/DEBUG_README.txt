If you get something like
	
	'java.lang.SecurityException: Can not make a java.lang.Class constructor accessible'
	
during debugging using dev mode, you might want to check that you do not have any jsni stuff in your classes,
something like the foo method of com.braintribe.gwt.customization.client.StartupEntryPoint.

When this was written, there was a line like this:

	    var proto = @Class::getPrototypeForClass(Ljava/lang/Class;)(clazz);

which was actually leading to given exception. 

Example exception:
ERROR: Unable to load module entry point class com.braintribe.gwt.customization.client.StartupEntryPoint (see associated exception for details) 
	com.google.gwt.dev.jjs.InternalCompilerException: Unexpected error during visit. 
	at com.google.gwt.dev.js.ast.JsVisitor.translateException(JsVisitor.java:475) 
	at com.google.gwt.dev.js.ast.JsVisitor.doTraverse(JsVisitor.java:462) 
	at com.google.gwt.dev.js.ast.JsVisitor.doAccept(JsVisitor.java:437) 
	...
Caused by: java.lang.SecurityException: Can not make a java.lang.Class constructor accessible 
	at java.lang.reflect.AccessibleObject.setAccessible0(AccessibleObject.java:139) 
	at java.lang.reflect.AccessibleObject.setAccessible(AccessibleObject.java:129) 
	at com.google.gwt.dev.shell.DispatchClassInfo.findMostDerivedMembers(DispatchClassInfo.java:129) 
	at com.google.gwt.dev.shell.DispatchClassInfo.findMostDerivedMembers(DispatchClassInfo.java:103) 
	at com.google.gwt.dev.shell.DispatchClassInfo.lazyInitTargetMembers(DispatchClassInfo.java:229) 
	at com.google.gwt.dev.shell.DispatchClassInfo.getMemberId(DispatchClassInfo.java:60) 
	at com.google.gwt.dev.shell.CompilingClassLoader$DispatchClassInfoOracle.getDispId(CompilingClassLoader.java:170) 
	at com.google.gwt.dev.shell.CompilingClassLoader.getDispId(CompilingClassLoader.java:1029) 
	at com.google.gwt.dev.shell.Jsni$JsSourceGenWithJsniIdentFixup.visit(Jsni.java:170) 
	at com.google.gwt.dev.js.ast.JsInvocation.traverse(JsInvocation.java:91) 
	at com.google.gwt.dev.js.ast.JsVisitor.doTraverse(JsVisitor.java:460) 
	...
	at com.google.gwt.dev.shell.ModuleSpaceOOPHM.createNativeMethods(ModuleSpaceOOPHM.java:53) 
	at com.google.gwt.dev.shell.CompilingClassLoader.injectJsniMethods(CompilingClassLoader.java:1386) 
	at com.google.gwt.dev.shell.CompilingClassLoader.findClass(CompilingClassLoader.java:1156) 
	at com.google.gwt.dev.shell.CompilingClassLoader.loadClass(CompilingClassLoader.java:1201) 
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357) 
	at java.lang.Class.forName0(Native Method) 
	at java.lang.Class.forName(Class.java:340) 
	at com.google.gwt.dev.shell.ModuleSpace.loadClassFromSourceName(ModuleSpace.java:683) 
	at com.google.gwt.dev.shell.ModuleSpace.onLoad(ModuleSpace.java:390) 
	at com.google.gwt.dev.shell.OophmSessionHandler.loadModule(OophmSessionHandler.java:200) 
	at com.google.gwt.dev.shell.BrowserChannelServer.processConnection(BrowserChannelServer.java:530) 
	at com.google.gwt.dev.shell.BrowserChannelServer.run(BrowserChannelServer.java:368) 
	at java.lang.Thread.run(Thread.java:744) 
