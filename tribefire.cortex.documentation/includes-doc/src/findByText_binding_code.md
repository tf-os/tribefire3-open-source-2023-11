```java
@Managed
public DenotationTypeBindings extensions() {

    DenotationTypeBindingsConfig bean = new DenotationTypeBindingsConfig();

    //..

    bean.bind(FindByTextProcessor.T)
        .component(commonComponents.accessRequestProcessor())
        .expertSupplier(deployables::findByTextProcessor);

    //..

    return bean;
}
```