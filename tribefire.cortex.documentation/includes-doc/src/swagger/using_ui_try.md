#### Try it Out

When you click on an entry and expand its details, you can click the **Try it out** button to test the REST call. Clicking this button enables you to fill in all the parameters, execute the query, and inspect the outcome in the **Responses** section directly in your browser.

If you try out the endpoints for REST calls which require a body to be passed, the body is already pre-populated with the resource's properties and their placeholder values.

When you tried out a REST call and you're satisfied with the result, you can copy the exact URL or Curl you need for this operation from the **Responses** section. 

### Models Section

The **Models** section contains information about every model you can use as a part of the displayed REST API. In the model, you can see the entities and their parameters, with mandatory ones being marked with an asterisk.

### JSON File Export

You can export the displayed REST API endpoints by saving the Swagger definition JSON file. 

> The functionality of importing Swagger files does not come out-of-the-box.

The file contains the entire API definition and is available under the link visible at the top of the page. All you need to do to obtain it is to right-click and select **Save As**. 

Obtaining the file allows you to transfer the models and their REST API definition to another tribefire instance. However, importing a Swagger definition file to another tribefire instance is only possible with the use of the `SwaggerModelImportCartridge`. 

#### Importing a Swagger Definition File

To set up your instance for Swagger definition import:

1. Download the `SwaggerModelImportCartridge`, deploy, and synchronize it with your instance.
2. In Control Center, navigate to **Custom Deployables**, right-click **Swagger Import Service Processor** and click **Deploy**.
3. Using the **Quick Access** box, search for `ImportSwaggerModel`. Note the following entries that appear in the **Service Requests** section: 
  * `ImportSwaggerModelFromResource`
  * `ImportSwaggerModelFromUrl`
  
4. Select the proper entry, depending on how you want to import the Swagger definition file. A new tab opens.
5. Depending on the service request you selected, you can see either the `swaggerResource` or `swaggerUrl` property. Assign your Swagger definition file or provide a valid link to one and click the **Execute** button.
    
    Property | Description
    -------- | ----------
    `swaggerResource` | The Swagger definition file stored in JSON or YAML. Only available for the `ImportSwaggerModelFromResource` service request.
    `swaggerUrl` | Link to the Swagger definition file stored in JSON or YAML. Only available for the `ImportSwaggerModelFromUrl` service request. <br/>
  
Once the import is successfully completed, a prompt is displayed with the newly created model name.

#### Definition File Metadata

During the import phase some additional information from the definition file is imported as metadata.

OpenAPI Field Name        | tribefire Metadata
--------------------  | ------------------------
`info`                | `SwaggerInfoMd`
`host`                | `SwaggerHostMd`
`basePath`            | `SwaggerBasePath`
`schemes`             | `SwaggerSchemesMd`
`securityDefinitions` | `SwaggerSecurityDefinitionsMd`
`tags`                | `SwaggerTagsMd`
`externalDocs`        | `SwaggerExternalDocsMd`

Other than that we have `SwaggerExampleMd` and `SwaggerVendorExtensionMd` representing examples and vendor extensions.

> For more information about OpenAPI specification, see [OpenAPI Specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#infoObject).

#### Models Originating from Definitions

Creating a model from an imported  definition file actually results in creating two different models: one for the entity types and one for the API definition. The names which tribefire saves the models with are created using the following patterns:



Model | Name | Description
----- | ------- | ------
Entity Types | `title` + `Model` | The value of `title` is taken from the Swagger definition file and is stripped from all whitespace characters. 
API | `title` + `ApiModel` | The value of `title` is taken from the Swagger definition file and is stripped from all whitespace characters. 
