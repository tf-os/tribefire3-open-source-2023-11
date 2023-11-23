# Error Reasoning

## Reason Model

The reason model was created to describe transitive reasoning that can be used in various situations. We mostly use reasons in order to give detail information on how an an erroneous state was reached. Currently the model has two types:

```
entity Reason {
  text: string,
  reasons: list<Reason>
}

entity HasFailure {
  failure: Reason
}
```

The model is published with the following artifact

`com.braintribe.gm:reason-model#2.0`

Reasons can be caused by other reasons to describe transitive reasoning. To organize, categorize and concretize Reasons a domain should create its own `<domain>-reason-model` in which derivates of Reason are declared. Each derivate can introduce individual type-safe properties that can carry important context information. These properties can also be used by property-placeholders in message localization.

Here a little example for a custom reason model (e.g. ```example-reason-model```) to be used later in this document:

```
entity ExampleReason extends Reason {}
    
entity PersonNotFound extends ExampleReason {}

entity NameNotAvailable extends ExampleReason {}
```

## Maybes

Beside using a ReasonException to communicate an error to a caller, there is the option to use the Maybe class that is a vehicle to either tansport a positive value and/or a Reason when the call failed in some way that is explainable by a Reason:

```java

Maybe<Person> getPerson(Claim claim) {
    if (claim.getOwner() == null) {
        return Reasons.build(PersonNotFound.T)
            .text("The given claim contained no fitting person")
            .toMaybe();
    }

    return Maybe.of(claim.getOwner());
}
```

Using a Maybe to communicate potential errors creates more awareness on the caller side but also more diligence. It also avoids exception creation and break of code flow.

### Maybe support in DDSA

The evaluator of a ServiceRequest can decide if it wants to receive reasons by catching ReasonExceptions or by calling a new method on EvalContext:

```java
interface EvalContext<T> {
   // ...
   Maybe<T> getMaybe();
   // ...
}
```

Additionally a ReasonedServiceProcessor can be used to already return a potential from a processing method

```java
class MyServiceProcessor implements ReasonedServiceProcessor {
    public Maybe<String> processReasoned(ServiceRequestContext context, GetPersonName request)
    {
        GetPerson getPerson = GetPerson.T.create();
        getPerson.setClaim(request.getClaim());

        Maybe<Person, Reason> result = getPerson.eval(context).getMaybe();

        if (result.isSuccessful())
            return Maybe.of(result.get().getName());

        return Reasons.build(NameNotAvailable.T)
            .text("The owner name could not be determined")
            .cause(result.failure())
            .toMaybe();
    }
}
```

## Reason Exceptions

We introduce a ReasonException that mainly carries a Reason from the reason-model that was not handled. The ReasonException should not be catched in order to react on specific reasons in a controlled fashion. If you want to control error handling than you have to find a way to receive a Reason via a Maybe or a response type that derives from HasFailure.

In case of DDSA we will use the special stacktrace free exception `UnsatisfiedReasonTunneling` following exception to transfer Reasons out of special processors such as `ReasonedServiceProcessor`, `ReasonedAccessRequestProcessor`:

## Endpoint Behaviour

Endpoints should catch ReasonException or use the EvalContext.getPotential() in order to be aware of error Reasons and also potentially map it to other error systems like HTTP status codes in REST. This can involve dynamic mapping information from the cortex database.

The Endpoint should also check if the response is instanceof HasFailure and decide based on a given leniency to ignore it or to make an error of HasFailure.failure just like it would have happend with the Exception or Potential.

# Discussion Backlog

The discussion from Thursday April 29th 2021 brought up the following controverses, concerns, ideas and requirements:

## Contextualization

> Roman brought up the concern that Reasons miss stacktrace information

On one hand we basically aggreed that expectable errors (Reasons) that should be communicable to some handler or a user are not exceptions. The stacktrace of exceptions does not contain usable information for a handler but just for a developer that wants to find the path and place of the sources to be analyzed and debugged.

On the other hand it seems vital that reasons need to make the origin identifyable. That can either happen by the selected use of specific reasons just in dedicated places or all the information needed to identify the origin and the context is to be modeled in an expressive way. The more generic a concrete Reason is, the more context information it requires to identify an origin. For example in the case of a generic Request validation interceptor the same set of Reasons could occur for various request types and contexts. A specially modelled top level Reason of the validator domain could transport the context information of a DDSA request stack and maybe also a stacktrace information on the modeled Reason. Such context information could also be shared by polymorphism with other generic domains.

We also discussed that the depth of the Reason coming from a call-site is an important information to associate an origin. In that sense imagine a generic NotFound reason which may come from a top level GetPerson request or from a deeper precursor. The call-site cannot know about the relevance of the NotFound if it cannot distinguish if it is semantically bound to the specific GetPerson logic or some deeper logic. To distinguish that the depth of the reason is of relevance.

## Logging

> Gerry brought up that logging should be configurable

> Roman requested that the log should carry stacktrace information of the reason when it is being logged.

We said that specific Reason types could be marked with a LogReason metadata in order to automatically log them.

Preserving stacktraces requires extra efforts. As all the recognitions of the [contextualization](#contextualization) still apply the stacktrace would not be available on the Reason but it could be acquired on DDSA framework level that could capture the Reason before propagating it. When a Reason is captured and the logging is activated by metadata then the stacktrace on DDSA level and also the request stack itself could be determined and logged.

## Service Return Type Maybe

> Gerry suggested that a Service could return a Maybe

This is already possible with the `ReasonedServiceProcessor`, `ReasonedAccessRequestProcessor` and `DispatchConfiguration`. But maybe Gerry meant that a ServiceRequest is returning a Maybe.

As a Maybe is not a GenericModelType based value it may not directly be associated with a ServiceRequest but the intent behind that idea can be achieved by modeling. In such a case you would derive your response type from `HasFailure` like in this little example:

```
entity GetPersonResponse extends HasFailure {
    Person person;
}
```

When using HasFailure with your response type the response can acutally be in 3 different states:

* `success` -> failure property is null
* `failure` -> failure property is not null but no other information is given
* `lenient failure` -> failure property is not null but other information is given

A caller has always to check the failure state of the response and to decide if everthing is ok or if a failure is present. In the second case the caller could react leniently and continue with partial information from the reponse.

## Reasons from normal APIs vs. DDSA processors

> Gerry asked for a differentiation betweend Reasons from normal APIs and Reasons from DDSA processors

## Treatment of Normal Excpeptions

> Gerry asked what do we do about normal Exceptions at endpoints

Maybe we should turn normal Exceptions into Reasons of a specific kind that carries no stacktrace but traceback information

```
entity InternalError extends Reason {
    tracebackId: string
}
```
