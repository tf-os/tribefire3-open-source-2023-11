# Web Contexts

## Create a new WebContext asset

Create a project folder structure for your web context content and place at least following files as described below.

### Devrock
* `web-context-asset-name`
    * src
    * asset.man
    * build.xml
    * pom.xml

#### Folder: src

Place static web context content here.

#### File: asset.man

The content of this file must include  
`$natureType = com.braintribe.model.asset.natures.WebContext`.
#### File: build.xml

The project must include `web-context-ant-script` like  
`<bt:import artifact="com.braintribe.devrock.ant:web-context-ant-script#1.0" useCase="DEVROCK" />`

#### File: pom.xml

A standard `pom.xml` is required. Dependencies are optional.

### Maven
TO BE WRITTEN.