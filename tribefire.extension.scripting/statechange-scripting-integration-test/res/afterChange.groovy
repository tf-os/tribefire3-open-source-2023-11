person = $context.entity;
if (person.familyName != null && person.familyName) {
    person.name = "Mr. " + person.familyName;
}

// state-change custom context can be set by onBeforeScript
custom = $context.getStateChangeCustomContext(); // type: Person
if (custom != null) {
    person.setState(custom.getState());
}
