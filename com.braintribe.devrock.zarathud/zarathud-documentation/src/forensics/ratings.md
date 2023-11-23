#Ratings

Ratings are used to describe the severity of the [finger print](./fingerprint.md) they are associated with.

code | description
------- | ----------- 
OK | nothing to report
INFO | while worthwile to report, it's nothing to worry about 
WARN | while not a direct problem, you should know about it 
ERROR | a big, fat fail

There is a basic rating that is intrinsic to Zed - see [forensics](./forensics.md). But there are ways to overload this standard settings.

 ## overloading ratings
 
The ratings with their [default values](./forensics.md) can be overloaded in several ways. Such an overload package is nothing more as a collection of [finger prints](./fingerprint.md). The more precise such a fingerprint is, the more selective the ratings can be customized. 

Such fingerprints can be imported in several ways, and all sources can be combined

### per annotation
You can declare finger print overrides directly in a class or an interface 

```java
	@(Supressbla)
```

### per file 
All zed runners support an additional external file that contains a collection of the finger prints in yaml format. 

### per artifact part

An artifact can have an additional part 

- fingerprints.yaml
- <artifactId>-<version>.fp



 
 
 