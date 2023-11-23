# Implementation of Checks

In [Tribefire Checks](checks.md) all relevant terms and types have been introduced. This knowledge serves as the base for this tutorial.  

## Implement a Custom Check

### 1. Introduction

In this example  we will create a simple `DemoCheckProcessor` which returns a successful result (status is `ok`). The result, associated to a `Module`, will be accessed via the landing page's prepared link. So let's go!

### 2. Denotation Type Creation

We need a denotation type for our processor. Let the interface extend from `com.braintribe.model.extensiondeployment.check.CheckProcessor` like:

```java
public interface DemoCheckProcessor extends CheckProcessor {
	EntityType<DemoCheckProcessor> T = EntityTypes.T(DemoCheckProcessor.class);
}
```

### 2. Expert Implementation

The expert needs to implement interface `com.braintribe.model.processing.check.api.CheckProcessor`
Override method `check()` and provide check logic there. The return type is `CheckResult`.

```java
public class DemoCheckProcessor implements CheckProcessor {
 
    @Override
    public CheckResult check(ServiceRequestContext requestContext CheckRequest request) {
        CheckResult result  = CheckResult.T.create();
        
        CheckResultEntry entry = CheckResultEntry.T.create();
        entry.setName("Demo Check");
        entry.setDetails("Successfully executed demo check.");
        entry.setCheckStatus(CheckStatus.ok);
        
        /* We could have more CheckResultEntry instances here */

        result.getEntries().add(entry);
        
        return result;
    }
}
```
### 3. Deployable Creation

Navigate to your module's `DeployableSpace` and create a Wire instance for your processor:
```java
@Managed
public DemoCheckProcessor demoCheckProcessor() {
	return new DemoCheckProcessor();
}
```

### 4. Processor Binding

Navigate to your module's `ModuleSpace` where `bindDeployables` is called and add another binding:
```java
bindings.bind(DemoCheckProcessor.T)
	.component(tfPlatform.binders().checkProcessor())
	.expertSupplier(deployables::demoCheckProcessor);
```
### 5. Initializer: Processor Instance Creation

Navigate to the initializer space and create a new Wire instance:
```java
@Managed
private DemoCheckProcessor demoCheckProcessor() {
    DemoCheckProcessor bean = create(DemoCheckProcessor.T);
    
    bean.setModule(existingInstances.module());
    bean.setExternalId("serviceProcessor.demoCheckProcessor");
    bean.setName("Demo Check Processor");

    return bean;
}
```

### 6. CheckBundle Creation

As we have created the processor wire instance, we need to create a `CheckBundle` where we attach it.

```java
@Managed
@Override // announce the bean and call it in your initializer's initialize() method
public CheckBundle demoChecks() {
    CheckBundle bean = create(CheckBundle.T);
    
    // reference the module as you do it for Deployables as well
    bean.setModule(existingInstances.module());

    // the previously created wire instance
    bean.getChecks().add(demoCheckProcessor());

    // mandatory property
    bean.setName("Demo Checks");

    // we are free to qualify the bundle. Let's do so:
    bean.setWeight(CheckWeight.medium);
    bean.setLabels(Sets.set("demo", "tutorial"));
    
    return bean;
}
```

Don't forget to announce bean `demoChecks()` and call it in the initializer's initialize() method!

### 7. Access the check result via landing page

Access the landing page of a running tribefire instance after setting your project up containing the new processor implementation. The module your processor is originating shows a link **Checks** - when clicking on it you are redirected to a HTML page showing you the result of your check! Congrats!

## Implement a Health Check

The tribefire framework will register a `CheckBundle` as a health check if `CheckBundle.coverage` is set to `vitality`. For implementation steps see [Implement a Custom Check](#implement-a-custom-check) right above.

## What's Next?

As we are familiar with the implementation now, we are ready to execute those requests.

### Executing Checks in General

* [Execution of Checks via tribefire Landing Page](executing-checks-landing-page.md)
* [Execution of Checks via REST](executing-checks-rest.md)

### Executing Health Checks

* [Health Checks via cURL](executing-health-checks.md)