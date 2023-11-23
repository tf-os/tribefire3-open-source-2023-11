# Fulltext Metadata

The package Full-Text gathers together all metadata that are concerned with full-text searching.

## General

This package has one type, FulltextMetaData, and is used to collect all full-text related metadata. This means that any metadata that is related to full-text searching generally has two super types:

* `FulltextMetadata`
* its corresponding metadata type (either `EntityTypeMetadata` or `PropertyMetadata`).

The metadata in this package only affects entities and properties.

## Available Metadata

Name    | Description  
------- | -----------
FulltextEntity | Indicates whether this entity is part of the full-text store.
AnalyzedProperty | Indicates whether this property is analyzed or not.
FulltextProperty | Indicates whether this property is part of the full-text store.
StorageHint | Defines how this property is persisted in the full-text store. Uses the `StorageOption` enum as possible options.
StorageOption | Specifies available options of persisting a property in a full-text store. Available are the following options: `reference`, `embedded`, `encoded`