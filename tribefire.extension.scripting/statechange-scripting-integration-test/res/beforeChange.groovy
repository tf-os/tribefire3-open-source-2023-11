import tribefire.extension.scripting.test.model.data.Person;

manipulation = $context.getManipulation(); 
value = manipulation.getNewValue();
value = value.capitalize();

// this is a bit very-peculiar application here
test = Person.T.create();
if (value == "Mia") {
    test.setState("modified");
}
return test;

