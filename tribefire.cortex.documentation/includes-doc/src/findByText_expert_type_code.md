
```java
package tribefire.demo.impl.extensions;
import //...

public class FindByTextProcessor implements AccessRequestProcessor<FindByText,  List<GenericEntity>>  {

    // Define the Expert Registry
    private GmExpertRegistry registry = null;

    public void setRegistry(GmExpertRegistry registry) {
        this.registry = registry;
    }

    public FindByTextProcessor() {
        
        // Fill the Expert Registry
        if (this.registry == null) {
            ConfigurableGmExpertRegistry registry = new ConfigurableGmExpertRegistry();
            registry.add(FindByTextExpert.class, Person.class, new PersonFinder());
            registry.add(FindByTextExpert.class, Company.class, new CompanyFinder());
            registry.add(FindByTextExpert.class, Department.class, new DepartmentFinder());
            this.registry = registry;
        }
    }
	
    // Implement the process-method
    @Override
    public List<GenericEntity> process(AccessRequestContext<FindByText> context) {

        // Get request, type, text, and session from the context-object
        FindByText request = context.getOriginalRequest();
        String type = request.getType();
        String text = request.getText();
        PersistenceGmSession session = context.getSession();

        // Call the expert respective to the type
        FindByTextExpert finder = registry.getExpert(FindByTextExpert.class).forType(type);
        return finder.query(text, session);
    }

    // Define an interface declaring a query-method                                   
    public interface FindByTextExpert {
        List<GenericEntity> query (String text, PersistenceGmSession session);
    }

    // Expert implementation of the FindByTextExpert-interface specific to Person
    public class PersonFinder implements FindByTextExpert {
        @Override
        public List<GenericEntity> query(String text, PersistenceGmSession session) {

            EntityQuery query = EntityQueryBuilder
                .from(Person.T)
                .where()
                    .disjunction()
                        .property(Person.firstName).like(text+"*")
                        .property(Person.lastName).like(text+"*")
                    .close()
                .done();

            return session.query().entities(query).list();
        }
    }
                                                                                                       
    // Expert implementation of the FindByTextExpert-interface specific to Company
    public class CompanyFinder implements FindByTextExpert {
        @Override
        public List<GenericEntity> query(String text, PersistenceGmSession session) {

            EntityQuery query = EntityQueryBuilder
                .from(Company.T)
                .where()
                    .property(Company.name).like(text+"*")
                .done();

            return session.query().entities(query).list();
        }
    }
                                                                                                       
    // Expert implementation of the FindByTextExpert-interface specific to Department
    public class DepartmentFinder implements FindByTextExpert {
    @Override
    public List<GenericEntity> query(String text, PersistenceGmSession session) {
        EntityQuery query = EntityQueryBuilder
            .from(Department.T)
            .where()
                .property(Department.name).like(text+"*")
            .done();

        return session.query().entities(query).list();
        }
    }	
}
```