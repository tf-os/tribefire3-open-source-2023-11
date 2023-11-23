
```java
package tribefire.demo.model.deployment;
import //...

public interface FindByTextProcessor extends AccessRequestProcessor {
	
	EntityType<FindByTextProcessor> T = EntityTypes.T(FindByTextProcessor.class);
}
```