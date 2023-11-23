# hibernate-shared-md-priming

Creates commonly used hibernate-mappings in `cortex` so other modules don't have to create their own versions over and over again.

## Mapping MD instances:

Following Hibernate mapping meta data are created:

Description | globalId | Type | Details
-|-|-|-
Mapped Entity | hbm:mapped-entity | EntityMapping | `EntityMapping` with `mapToDb=true`
Unapped Entity | hbm:unmapped-entity | EntityMapping | `EntityMapping` with `mapToDb=false`
Mapped Property | hbm:mapped-property | PropertyMapping | `PropertyMapping` with `mapToDb=true`
Unapped Property MD | hbm:unmapped-property | PropertyMapping | `PropertyMapping` with `mapToDb=false`

## Configuration Model

Besides meta data, this also creates a configuration model which explicitly says `globalId` and `partition` are not mapped (though it doesn't say anything is mapped).

When mapping a custom model, it might be convenient to add a dependency to this model, rather than having to un-map the properties explicitly.

-|-
-|-
name | basic-hbm-configuration-model
globalId | model:tribefire.extension.hibernate:basic-hbm-configuration-model
Description | specifies `globalId` and `partition` properties are NOT MAPPED
