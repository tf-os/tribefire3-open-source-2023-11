# Creating Workers
In this tutorial, you're going to create a simple worker which changes data on `Person` entity type instances (in this particular case, it changes the first and last name to **Big Boss**) based on their current position (CEO in this tutorial). Your worker will also periodically check for changes, utilizing the `JobScheduling` extension point.

## Prerequisites

* Prepared development environment as per the instructions on [Setting Up IDE for Cartridge Development](asset://tribefire.cortex.documentation:tutorials-doc/cartridge/setting_up_ide.md). In order to follow this tutorial to the letter, please use the ****Demo Cartridge**.

## Implementation Steps
The following is a brief overview of worker implementation:

1. Create a denotation type extending the abstract type `Job` to depict the actual implementation.
2. Implement the `Job` expert (this is where you define the actual logic).
3. Declare the deployable in `Wire`.
4. Bind the deployable to the expert in `Wire`.
5. Create an instance of a `Job` and deploy it.
6. Create an instance of a `JobScheduler` (subtype of Worker), assign the Job instance to it, and deploy it.


## Creating a Worker Denotation Type
Let's begin with creating the denotation type, following the normal workflow of cartridge development.

1. Find the `tribefire-demo-deployment-model` project in your IDE, then create the `ChangeNameJob` interface in the `tribefire.demo.model.deployment package`.
2. Make sure that your interface extends the `Job` interface, as in the snippet below:

```
package tribefire.demo.model.deployment;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.scheduling.Job;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ChangeNameJob extends Job {

	EntityType<ChangeNameJob> T = EntityTypes.T(ChangeNameJob.class);

	IncrementalAccess getAccess();
	void setAccess(IncrementalAccess demoAccess);
	
}
```

## Implementing the Job Expert
This is the most important part of the whole process, where we define the logic of our expert. We will use a left-join select query to retrieve all CEOs from all `Company` instances, make a list of all CEOs, and finally iterate through the list to change their names.
> For more information about the query syntax, see [GMQL](asset://tribefire.cortex.documentation:concepts-doc/features/gmql.md)

1. Find and open the `tribefire-demo-cartridge` project. 
2. In the `tribefire.demo.impl.extensions` package, create the `ChangeNameJob` class, implementing the `Job` interface. Use the snippet below for reference.
> Note that this interface requires to override the `process` method.

```
package tribefire.demo.impl.extensions;

import java.util.List;
import com.braintribe.cfg.Required;
import com.braintribe.model.job.api.JobRequest;
import com.braintribe.model.job.api.JobResponse;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.api.Job;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import tribefire.demo.model.data.Company;
import tribefire.demo.model.data.Person;

public class ChangeNameJob implements Job {
	
	
	// Declaration and getters of configurable access-property and session factory
    private String accessId;
    private PersistenceGmSessionFactory sessionFactory;

    @Required
    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    @Required
    public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public JobResponse process(ServiceRequestContext context, JobRequest request) {
    	
    	PersistenceGmSession session = sessionFactory.newSession(accessId);

        //Finding all CEOs across all companies...		
    	SelectQuery ceoQuery = new SelectQueryBuilder()
                .from(Company.T,"c")
                    .leftJoin("c", Company.ceo, "ceo")
                .select("c", Company.id)
                .select("ceo")
                .distinct()
                .done();
    	
        // ...making a list of CEOs...
    	List<ListRecord> listRecords = session.query().select(ceoQuery).list();
    	
        // ...and iterating through it to change the names
    	for (ListRecord l : listRecords ) {
    		
    		Person p = (Person) l.getValues().get(1);
    		
    		p.setFirstName("Big");
    		p.setLastName("Boss");
    		
    	}
    	
    	session.commit();
    	
    	return JobResponse.T.create();
    }
    
}
```

## Declaring the Deployable in Wire
Now that we have both our Denotation Type and Expert (logic), it's time to link them together using Wire, in accordance with the cartridge development standard. First, we need to declare our expert type as a `bean` in the `DeployablesSpace` class.

1. Open the `DeployablesSpace` class from the `com.braintribe.cartridge.extension.wire.space.DeployablesSpace` package, found in the `tribefire-demo-cartridge` project.
2. Declare your expert as a bean. The actual instantiation takes place during deployment due to its managed nature. The passing of the configured values is defined here by:
    * using the `ExpertContext` as a parameter
    * getting the `Deployable` from the context object
    * getting the `access` object from the `deployable`
    * getting the `sessionFactory` from the `clientBaseContract`
    * setting both on the expert

```
@Managed
	public ChangeNameJob changeNameJob(ExpertContext<tribefire.demo.model.deployment.ChangeNameJob> context) {
		
		tribefire.demo.model.deployment.ChangeNameJob deployable = context.getDeployable();
		
		ChangeNameJob bean = new ChangeNameJob();
		bean.setAccessId(deployable.getAccess().getExternalId());
	    bean.setSessionFactory(clientBaseContract.persistenceGmSessionFactory());
	    return bean;
		
	}
```

## Expert Binding in Wire
Now we're going to bind the denotation type to the expert.

1. Open the `CustomCartridgeSpace` class from the `com.braintribe.cartridge.extension.wire.space.DeployablesSpace` package, found in the `tribefire-demo-cartridge` project.
2. Pass the denotation type's `T` value to the `bind()` method of the `DenotationTypeBindingsConfig` instance.
3. Follow with a component declaration taken from a component contract. The component declaration is the interface your denotation type extends. In this tutorial, it is `Job`. 
4. Finish the binding by providing the expert from the `DeployablesSpace` class, which is where you declared your expert type as a bean in the first place. As we use the context object to pass values, we have to use an `expertFactory` here.

```
@Managed
private DenotationTypeBindings extensions() {

	DenotationTypeBindingsConfig bean = new DenotationTypeBindingsConfig();

    //...other bindings...

    //...your binding
    bean.bind(ChangeNameJob.T)
		.component(commonComponents.job())
		.expertFactory(deployables::changeNameJob);

return bean;
}
```
## Testing
That's it in terms of programming - now it's time to see your worker in action!

1. Set up a local environment with your changes. When in doubt, follow the [Developing Cartridges](../cartridge/developing_cartridges.md#creating-a-new-cartridge) document, starting with step 5.
2. Start the server (`runtime/bin/startup.bat`) and open **Control Center**.
3. Navigate to the `Workers` entry point and create a new instance of `JobScheduler`.
4. Provide the necessary information:

    Property | Value
    -------- | -----
    `externalId` | `scheduling.name.changes`
    `name` | `Name Change Scheduler`
    `cartridge` | null
    `coalescing` | `true`
    `cronExpression` | `0/10 0/1 * 1/1 * ? *`
    `job` | New instance of `ChangeNameJob`

    > In the likely event of not knowing what a cron expression is, you can rely on the [Cron Maker](http://www.cronmaker.com/) to convert your frequency value into `cron`.

5. Configure the just created `Job` as follows:

    Property | Value
    -------- | -----
    `externalId` | `job.name.changes`
    `name` | `Name Change Watch Job`
    `access` | `Demo Access`
    `cartridge` | `tribefire.demo.cartridge`

6. Commit your changes and deploy both instances.
7. Switch to `Demo Access`.
8. In explorer, search for `Persons`. After 10 seconds (or whatever interval you set in the cron expression), all CEO names should read **Big Boss**.





