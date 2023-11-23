# Link Extensions

## Files of the same asset
There are several ways of linking to a [file](structure.md#files) of the same [asset](structure.md#assets). If the file is in the same [folder](structure.md#folders) it's straightforward:
```
[link to a file within the same folder](filename.md)
```

If the file is in a sub- or parent folder you can use relative URIs
```
[link to a file in a subfolder](sub/folder/filename.md)
[link to a file in a parent folder](../../filename.md)
[link to a file in a sibling folder](../folder/filename.md)
and so on...
```

You can also use asset-absolute URIs that are resolved from the asset root. Notice the leading slash.
```
[link to a file in the asset root](/filename.md)
[link to a file in a subfolder](/sub/folder/filename.md)
```

## Files of another asset
To link between [assets](structure.md#assets) you can use our proprietary asset scheme:

```
[link to a file in another asset](asset://my.group:other-asset-name/filename.md)
```

## Headers
For every [header](structure.md#headers) an anchor is generated that resembles its content but changed to lowercase as well as replacing all spaces with dashes. For example "Header Name" becomes "header-name".

To link to a specific header just add a fragment element to the uri of your markdown link.
```
[link to a header of the same document](#header-name)
[link to a header of another document](anotherDocumentUri#header-name)
```

## Combinations
Of course you can combine these as you like

```
[link to a header of another asset's file](asset://my.group:other-asset-name/filename.md#header-name)
[link to a header using asset-absolute path](/sub/folder/filename.md#header-name)
[link to another asset's file in a subfolder](asset://my.group:other-asset-name/sub/folder/filename.md)
```

## Includes
You also can *include* a file instead of linking to it. This will display the file content just where you put the include link. You make a link an include link by simply appending `?INCLUDE`
```
[include a file in the same folder](filename.md?INCLUDE)
[include a file of another asset](asset://my.group:other-asset-name/filename.md?INCLUDE)
```
Adding a fraction (header) part to your include link it currently not supported

## Non proprietary URIs
Non proprietary URIs like http-links to other websites can of course still be used as specified by the markdown standard.
```
[Link to Braintribe](https://www.braintribe.com)
```
