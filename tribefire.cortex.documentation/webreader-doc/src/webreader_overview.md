# WebReader Overview

The WebReader functionality allows you to upload documents to Tribefire, convert and treat them as entity types. This means that the uploaded documents become instances of `Document` and can be stored in a Tribefire access.
<!--
> For more information about tribefire accesses, see [Access](asset://tribefire.cortex.documentation:concepts-doc/features/data-integration/access.md).-->

You can perform a number of operations on an uploaded document using the WebReader user interface, including:

* displaying and downloading documents
* starting comment threads
* adding, deleting, and saving comments as reusable **Stamps**

You can use a local setup for the Webreader functionality, but we recommend to use a cloud-based service.

### Conversion Cartridge

The conversion cartridge provides a service which transforms files into different formats:

| Source         | Target    | Type       |
| -------------  | ----      | -----      |
| Image          | PDF       | Conversion |
| PDF            | Image     | Conversion |
| Image          | Image     | Conversion |
| Image          | Image     | Resizing   |
| PDF            | PDF       | Split      |
| PDF            | PDF       | Watermark  |
| TXT            | PDF       | Conversion |
| RTF            | PDF       | Conversion |
| CSV            | PDF       | Conversion |
| MS Word        | PDF       | Conversion |
| MS Excel       | PDF       | Conversion |
| MS PowerPoint  | PDF       | Conversion |
| MS Visio       | PDF       | Conversion |

The conversion service, however, doesn't keep the results of the conversion. To keep the docs for longer, you have the documents access provided by the documents cartridge.

> You can use a local conversion service but we recommend to use a cloud-based one.

### Documents Cartridge

The documents cartridge acts like a facade towards the conversion cartridge.

The `document-data-model` contains a series of entities that are used to describe an uploaded document within tribefire. It is important to remember that a `Document` entity is just that, a series of properties describing the metadata of the actual document.

When a new document is uploaded to Tribefire, a new `Resource` instance is created and the document is assigned to it. This `Resource` is then assigned to the `Document` entity at the `Source Representation` property.

Once a new `Document` is created and committed the Resource State Change Processor then extracts all the pages from the document resource and creates a new instance of the `Page` entity for each page found. Similar to the relationship between the `Document` entity and the document itself, the `Page` entity contains properties that describe the metadata for each page, while the Conversion Cartridge creates different representation of that page as an image file. These representations (images of the original page) are placed in the property `representations`. There are by standard two different representations created: the main image for display and its thumbnail equivalent.

### WebReader UI

WebReader comes in two flavors (integrated in Explorer and as a external web application) and makes it possible to view documents, add comments, and perform full text searches. It can be accessed from within an access or can be called using a HTTP call with the appropriate Document ID and Access ID.

Before it can display the documents, WebReader requires a properly formatted document. A method of achieving this is through the use of the Conversion Cartridge, which takes a source document and splits it into its component pages. It is the pages that are read and displayed by WebReader, as opposed to the source document itself.

Regardless of how you access WebReader, either in the Explorer client or externally using the web app, the interface and the usage of WebReader are the same.

> For more information on how to use the WebReader UI, see [Using WebReader UI](using_webreader_ui.md).


## What's Next?

Get to know the different ways to set up this functionality on the [Installation](asset://tribefire.cortex.documentation:webreader-doc/installation.md) page.