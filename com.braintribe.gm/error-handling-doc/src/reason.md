# Error Handling

Error handling needs to be carefully differntiated. 

On one hand there are situations in which code is **not expecting** certain errors that can occur or has no meaningful way to react on an error. Such situations are exceptional and therefore [exceptions](#exceptions) are the appropriate vehicle and mechanism to carry on the error. 

On the other hand there are situations in which the code is **expecting** certain errors that can occur. In such situations exceptions are not appropriate. Instead functions should return values that are structured to potentially hold information about the error and its causes. Such a value must then be checked by the caller with conditional code. To give that way of handling errors a culture [Reason, HasFailure and Maybe](#reasons-hasfailure-and-maybes) were designed.

Code should try to handle errors in the second way as much as it can. Historically this way was the only way. Then exceptions where introduced in certain programming languages and people misunderstood the meaning of exceptions by using them as error vehicle for the expected case. Read more about the idea and misunderstanding of [exceptions](#exceptions). 

## Exceptions

The essential idea about exceptions is that they break the natural code flow in order to let code be unaware of errors. Naturally function code flows through its control structures and leaves the function at well defined places (e.g with `return` statements). An exception breaks this flow and the function is left at various points not being able to deliver a return value. This behaviour neccessarily continues for all recursive callers until there is a matching `try/catch` control structure. At this very point the natural code flow is restablished.

Breaking the code flow has some advantage but much more problems to it. The primary advantage is that error information can be transported without any preparation by the code whose flow is broken. That advantage already has the negative consequence that the origin and therefore relevance of the error gets lost as callers that catch exceptions do not know from which call depth the exception is comming. Although exceptions carry stacktraces with them this information is not usable by code to decide about the relevance. The stacktrace mainly comes in form of text but also the enormous variations in which function calls can be nested makes it even harder to draw meaning from this origin information. Actually the stacktrace is not intended for programatic decision making but for a developer that would read an exception stacktrace from a log or error dialog in order to find a debugging strategy.

The break of code flow also brings the requirement that code that is intentionally unaware of errors still needs to be aware of exceptions from anywhere to maintain consistent datastructures (e.g. stacks, locks). The `try/finally` control structure only exists because of the potential code flow break coming from exceptions.

Exceptions really mean a lot of loss of control and maybe exceptions bring much more problems that they actually solve. In consequence stay away from exceptions as a vehicle to transport well defined errors. Use them only if there is no other way around. Still you have to live with the fact that exceptions are already used as vehicle to pass on expectable errors in various APIs and libraries. Use your code to turn exception information as early as possible into real error information as described in the next section.

## Reasons, HasFailure and Maybes

In order to normalize causal information for error and other purposes the term `Reason` was introduced and modelled with the `GenericModel` paradigm. That means reasons and therefore will be `typesafe`, `polymorphic` and available for all domain agnostic algorithms made available with the `GenericModel` technology. We draw special benefit from those algorithms in case of platform APIs (REST, Swagger) where we are able to deliver the causal error information just like a positive result in the typical marshalled forms like `JSON`, `YAML` and others.