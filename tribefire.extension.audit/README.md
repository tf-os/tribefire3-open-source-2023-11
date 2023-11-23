# tribefire.extension.audit

This extension provides audit funcationality. This means that all manipulations on an Access can be recorded in either a different Access or in the same Access.

## Usage

Attach an `AuditAspect` to your access and configure one of the following meta-data either on EntityTypes or Properties:

* `Audited`: Any change to the entity (instantiated/deleted) or property will be tracked
* `AuditedPreserved`: Same as Audited, but it also stores the previous value
* `Unaudited`: Erasure that can be used to excempt EntityTypes or Properties from being tracked

The `AuditAspect` can reference a specific Access where the manipulation records will be stored. When this is left empty, the Access that the aspect is attached to will be used instead (make sure that it depends on the `module://tribefire.extension.audit:data-audit-module` model to be able to store these records). This also means that the `AuditAspect` can be used in combination with multiple Accesses.

In addition, the `AuditAspect` can hold a set of "untracked roles". When the user that creates a manipulation has one of these roles, the manipulations will not be tracked. This might be useful when background or system processes perform manipulations that are not audit-worthy.

A `ManipulationRecord` can hold both the `value` and the `previousValue`. If you're using an Access with limited property length (e.g., an HibernateAccess), please make sure to set the `MaxLength` meta-data on this property accordingly. If an audited value (either `value` or `previousValue`) exceeds the length of this meta-data, the values will be cut and the full values will be put into separate fields (`overflowValue` and `overflowPreviousValue` respectively). Please make sure that these value have a `materialized_clob` property mapping.
## Dependencies

You will need the following dependencies in your project:

* `tribefire.extension.audit:data-audit-module` in your setup,
* `tribefire.extension.audit:data-audit-deployment-model` for creating an `AuditAspect` deployable and attach it to an Access and for attaching Audited meta-data to your EntityTypes.
* `tribefire.extension.audit:data-audit-module` for the Access that will hold the manipulation records.

