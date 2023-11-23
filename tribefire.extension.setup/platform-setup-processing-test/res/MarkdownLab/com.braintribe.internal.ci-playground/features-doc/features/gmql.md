# Generic Model Query Language
>Generic Model Query Language (GMQL) is a query language used for searching entities and properties of your tribefire model.

## General
GMQL contains query functions found in standard query languages (aggregate functions, string functions, joins, and so on) along with tribefire-specific functionality (the ability to create entity and enum references for your query statements). Results are returned in the form of a JSON object, the actual type depending on the query executed. It is also possible change the returned object using the `codec` parameter.

## Usage
You can use GMQL:
* when you develop with tribefire.js, REST, or Java
* in the search fields of <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>

{%include note.html content="It is possible to use GMQL as part of the JavaAPI. However, it is recommended that you use the Java Query API, which contains a series of builders for creating query statements. <br/> If you do wish to use GMQL, you can do so using the `QueryParser` object.
"%}

{% include tip.html content="For information on how to use GMQL, see [GMQL Query Types](gmql_types.html)"%} 
