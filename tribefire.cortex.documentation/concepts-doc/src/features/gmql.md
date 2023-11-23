# Generic Model Query Language

Generic Model Query Language (GMQL) is a domain-specific query language used for searching entities and properties of your Tribefire model.

## General

GMQL contains query functions found in standard query languages (aggregate functions, string functions, joins, and so on) along with Tribefire-specific functionality (the ability to create entity and enum references for your query statements). Results are returned in the form of a JSON object, the actual type depending on the query executed. It is also possible change the returned object using the `codec` parameter.

## Usage

You can use GMQL:

* when you develop with tribefire.js, REST, or Java (although it's not recommended)
* in the search fields of Control Center and other generic model explorers

> GMQL is a domain-specific language and therefore shouldn't be used within code. It is recommended that you use GM Query API, which contains a series of builders for creating query statements in Java. <br/> If you do wish to use GMQL, you can do so using the `QueryParser` object.

For information on how to use GMQL, see [GMQL Query Types](../gmql/gmql_types.md)