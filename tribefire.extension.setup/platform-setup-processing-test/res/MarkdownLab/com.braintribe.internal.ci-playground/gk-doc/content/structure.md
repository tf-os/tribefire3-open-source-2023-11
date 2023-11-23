# Structure
Collaborative markdown documentation can be structured in many ways. Your documentation is kept coherent by [links between the structural elements](links.md). You can link easily between any of the following structural elements within the same setup. That way you can ensure coherence.

You can use the following structural elements:

## Setups
The finally rendered customized outcome of your documentation. You could for example have a setup for internal developers, another one for business experts and a third one for training purposes.
You can basically plug your setup together by specifying the building blocks it should contain. This way you can easily reuse parts between different documentations.

## Assets
These are the "building blocks" for your documentation. Consider keeping the content of an asset reusable between different setups. An asset can also have dependencies to other assets. That way you can make sure that everything is brought with your building block that you need to understand the contained information as well as all information you are linking to.

## Folders
Within an asset you have the possibility to structure your information in folders. This should rather help the content authors to keep an internal order and is not necessarily reflected in the final outcome displayed to the reader of the documentation.

## Files
To be able to provide nice chunks of documentation you can split the information within an asset in separate files. They will be rendered as one (HTML) page each.

## Headers
A file can be structured by common markdown headers. Because we generate anchors automatically for every header you can link to them easily.
