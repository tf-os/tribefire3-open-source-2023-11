# Hydrux Deployment Model

This is a summary of all `hydrux-deployment-model` types, with their corresponding `hydrux-api` interfaces (if relevant).

## Application

Entity:
```java
public interface HxApplication extends ModelMetaData {

	EntityType<HxApplication> T = EntityTypes.T(HxApplication.class);

	@Description("Value that is set as the html document title.")
	String getTitle();
	void setTitle(String title);

	@Description("Technical identifier of your Hydrux application that is sent to the server. "
			+ "This plays a role e.g. when the server is pushing notifications to the registered clients, where the recipients may be filtered by instanceId.")
	String getApplicationId();
	void setApplicationId(String clientId);

	HxView getView();
	void setView(HxView view);

	HxScope getDefaultScope();
	void setDefaultScope(HxScope defaultScope);

	@Description("List of modules to be loaded automatically on application load. This is relevant for avoiding delays once the app is running, "
			+ "as well as loading modules which would otherwise not be reachable (were there are no HxComponets referencing them). "
			+ "Modules that simply bind Dialog Processors are prime candidates (see HxRequestDialog)")
	List<UxModule> getPrefetchModules();
	void setPrefetchModules(List<UxModule> prefetchModules);

}
```


Interface: `IHx` 
```typescript
interface IHxApplication {
    /** Loads CSS file from given UxModule using cssResourcePath as a relative path within that module. More specifically, 
     * a UxModule.path is a URL for the module's main js file (index.js). The cssResourcePath is relative to the main file's parent.
     * Calling this method with the same arguments more than once has no additional effect.*/
    loadCss(uxModule: jsM.UxModule, cssResourcePath: string): void;

    /** HxApplication.title */
    getTitle(): string;
    /** HxApplication.clientId */
    getApplicationId(): string;
    /** Random gibberish string which is used as identifier of this HX Application instance for push-requests via WebSocket.
     * See also "this.newPushRequest(...)"*/
    getPushChannelId(): string;

    /** Returns a CallbackPushAddressing instance which has its pushChannelId (and maybe also it's serviceId) set. */
    newPushAddress(serviceId?: string): CallbackPushAddressing;

    getServiceProcessorBinder(): IHxServiceProcessorBinder;
    getServicesSession(): remote.ServicesSession;
    getRootScope(): Promise<IHxScope>;
    getEvaluator(): remote.EvaluatorBuilder;
    getLocalEvaluator(): eval_.Evaluator<ServiceRequest>;
    getHostSettings(): IHxHostSettings;
}
```

## Components

Note it is up to the developer to provide an implementation for said interfaces, which explains the minimalistic design of these interfaces with no convenience methods.

### HxComponent

This is the **basic abstract base** for all component types.

Entity:
```java
@Abstract
public interface HxComponent extends GenericEntity {

	EntityType<HxComponent> T = EntityTypes.T(HxComponent.class);

    /* This is used to load the JS module using module's UxModule.path as URL. */
	UxModule getModule();
	void setModule(UxModule module);

    /* See HxScope later */
	HxScope getScope();
	void setScope(HxScope scope);

}
```

Interface:
```typescript
interface IHxComponent { /* Nothing */ }
```


### HxView

Represents a **visual component**.

The view set as `HxApplication.view` is **resolved** automatically **on** application **load** and its `HTMLElement` is **set as** the sole child of **document's body**.

Entity:
```java
@Abstract
public interface HxView extends HxComponent {

	EntityType<HxView> T = EntityTypes.T(HxView.class);

}
```

Interface:
```typescript
interface IHxView extends IHxComponent {
    /*  Returns the HTML element of this component; all invocations return  the some one. 
       An instance can be simply created using IHxViews.create(() => htmlElement); */
    htmlElement(): HTMLElement;
}
```

### HxRequestDialog

Represents a special view whose purpose is to get input from the user.

The main use-case is to bind a `HxRequestdialog` instance as a special `Service Processor`, meaning that **request** is **evaluated** by **asking the user for input**. // TODO how?

Entity:
```java
@Abstract
public interface HxRequestDialog extends HxView {

	EntityType<HxRequestDialog> T = EntityTypes.T(HxRequestDialog.class);

	String getTitle();
	void setTitle(String title);

	boolean getDraggable();
	void setDraggable(boolean draggable);

	boolean getResizable();
	void setResizable(boolean resizable);

	String getWidth();
	void setWidth(String width);

	String getHeight();
	void setHeight(String height);

	/** If true, the element is rendered as a modal window, i.e. it blocks any interaction with the rest of the application. */
	boolean getModal();
	void setModal(boolean modal);

}
```

Interface:
```typescript
interface IHxRequestDialog extends IHxView { /* Nothing */ }
```

## Non Components

### HxScope


Entity:
```java
/**
 * Represents a logical space of {@link HxComponent components} within which a given denotation instance denotes the same implementation instance (I
 * promise it gets clearer).
 * 
 * <h3>Motivation</h3>
 * 
 * Imagine we want to have a button in our app which creates a new tab. We could want one of the two:
 * <p>
 * <ul>
 * <li>all the components within the tab are independent from the rest of the application, i.e. if we do some changes within that tab, we cannot save
 * them by clicking "save" in a different tab.
 * <li>some component are shared across all the tabs, i.e. it is the same instance everywhere.
 * </ul>
 * 
 * How do we configure one or the other?
 * 
 * <h3>Solution</h3>
 * 
 * Let's first have a look at the final solutions, and then explain how it works.
 *
 * <h4>Independent tabs</h4>
 * 
 * In this case, we have a <tt>FrameView</tt> with a "new tab" button, and the actual tabs, denoted by <tt>TabView</tt>. Consider following
 * configuration:
 * 
 * <pre>
 * HxApplication
 *   defaultScope: HxScope(root)
 *   view: FrameView
 *     scope: null
 *     tab: TabView
 *       scope: HxScope(tab)
 * </pre>
 * 
 * <p>
 * If we had three tabs open, these would be the scopes of our components (numbers distinguish different instances of the same type):
 * 
 * <pre>
 * frameView: hxScope(root)
 * tabView1: hxScope(tab)1
 * tabView2: hxScope(tab)2
 * tabView3: hxScope(tab)3
 * </pre>
 * 
 * Q: Why is the frameView's scope the root scope?<br>
 * A: As the denotation's scope is <tt>null</tt>, the scope is inherited from the context that is resolving it. Since that view is configured as
 * <tt>HxApplication.view</tt>, it's the application that is resolving it, thus it's scope is inherited.
 * <p>
 * Q: What is <tt>tabView1</tt>, <tt>tabView2</tt>...?<br>
 * A: Those are different instances of an actual <tt>TabView</tt> implementation. Every time our <tt>frameView</tt> implementation resolves the same
 * <tt>TabView</tt> denotation, with the same <tt>HxScope</tt> denotation, a new <tt>IHxScope</tt> is created and a new <tt>tabView</tt>
 * implementation instance is created.
 * <p>
 * Q: Why always a new instance?<br>
 * A: The application keeps track of a stack of scopes. Since you are in the <tt>frameView</tt>, only the <tt>hxScope(root)</tt> is on the stack. Now
 * when you try to resolve the <tt>TabView</tt> instance, it sees a scope that is not yet on the stack - <tt>HxScope(tab)</tt>. Thus it creates a new
 * instance of this scope, and uses that new scope to resolve the <tt>TabView</tt>. And the new scope is of course empty, so a new <tt>tabView</tt>
 * implementation is created.
 * <p>
 * Q: So now, for each tab, the scope stack contains the root scope and the tab scope, but for each tab it is a different tab scope?<br>
 * A: Exactly.
 * <p>
 * Q: And that means?<br>
 * A: That means if the tab referenced a component with <tt>HxScope(root)</tt>, every single tab instance gets the exact same implementation instance
 * of this component. See also the next example.
 * 
 * <h4>Tabs sharing a component</h4>
 * 
 * In second case, we want the tabs to have a common component instance. So for example, if they are referencing an {@link HxSession}, it would be the
 * exact same implementation instance in all tabs. This would be achieved with this configuration:
 * 
 * <pre>
 * HxApplication
 *   defaultScope: HxScope(root)
 *   view: FrameView
 *     scope: null
 *     tab: TabView
 *       scope: HxScope(tab)
 *       session: HxSession
 *         scope: HxScope(root)
 * </pre>
 * 
 * <p>
 * 
 * If we had three tabs open, these would be the scopes of our components:
 * 
 * <pre>
 * frameView: hxScope(root)
 * tabView1: hxScope(tab)1
 * tabView2: hxScope(tab)2
 * tabView3: hxScope(tab)3
 * 
 * tabView1.session: hxScope(root)
 * tabView2.session: hxScope(root)
 * tabView3.session: hxScope(root)
 * </pre>
 * 
 * Q: OK, I get it.<br>
 * A: Just to be sure, each tab is a new instance, just like before. But when these instances try to resolve their dependency - the
 * <tt>HxSession</tt>, that session's scope (<tt>HxScope(root)</tt>) is resolved. And because that already exists on the scope stack (root scope is
 * always on the stack), we do not get a new scope instance, but an existing one. So when the first tab resolves the <tt>HxSession</tt> against the
 * root scope, a new session implementation instance is created and cached. When the other two tabs do the resolution, the same instance is already
 * found and returned. Thus we have achieved that all our tabs share the same session.
 * <p>
 * 
 * Hopefully it's clear now how this scopes are intended to be used.
 * 
 * @author peter.gazdik
 */
@SelectiveInformation("HxScope[${name}]")
public interface HxScope extends HasName {

	EntityType<HxScope> T = EntityTypes.T(HxScope.class);

	/** This has no technical purpose, just makes the configuration easier to understand. */
	@Override
	String getName();

	HxDomainSupplier getDefaultDomain();
	void setDefaultDomain(HxDomainSupplier defaultDomain);

	/** These components are initialized (resolve) automatically when the scope is resolved. */
	List<HxController> getControllers();
	void setControllers(List<HxController> controllers);

}
```

Interface:
```typescript
export interface IHxScope {

    getApplication(): IHxApplication;

    getDomain(): Promise<string>;

    resolveDomain(supplierDenotation: hxM.HxDomainSupplier): Promise<string>;

    resolveComponent<R extends IHxComponent>(component: hxM.HxComponent): Promise<R>;

    resolveView<R extends IHxView>(view: hxM.HxView): Promise<R>;

    resolveController<R extends IHxController>(controller: hxM.HxController): Promise<R>;

    resolveDataConsumer<R extends IHxDataConsumer>(dataConsumer: hxM.HxDataConsumer): Promise<R>;

    resolveSelectionEventSource<R extends IHxSelectionEventSource>(selectionEventSource: hxM.HxSelectionEventSource): Promise<R>;

    resolveSession(sessionDenotation: hxM.HxSession): Promise<PersistenceGmSession>;

    resolveSessionFactory(sessionFactory: hxM.HxSessionFactory): Promise<IHxSessionSupplier>;

    resolveDomainSupplier(domainIdSupplier: hxM.HxDomainSupplier): Promise<IHxDomainSupplier>;
}
```

----------------

Entity:
```java

```


Interface:
```typescript

```

