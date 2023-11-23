# Jinni Setup Tool

Jinni is a standalone application based on Java that comes with a finite set of services which are represented by ServiceRequest denotation types. Requests to Jinni can only be made via passing them as command line options either directly or indirectly with a json file.

The finite set of supported ServiceRequests are related to tribefire platform setup functionality.

## How to install Jinni

### Manual Installation

#### 1. Download Application Archive
Go to URL https://[repository-url]/tribefire/extension/setup/jinni and navigate to the latest revision to download the zip archive from there.

#### 2. Install Application Archive

Unzip the downloaded zip to a location of your choice and add the extracted **bin** folder to the **PATH** variable of your operating system.

#### 3. Configure Jinni (optional)

##### Maven Profiles

If you manage different profiles in your Maven settings.xml for different project situations you can use
the jinni environment configuration to activate those profiles. Open

    jinni/conf/environment.default.properties

and add/change the variable that we normally use to switch Maven profiles and target repositories (upload):

    PROFILE_USECASE=....

Addtionally you can add other environment configurations which can be activated by endpoint parameters and that have to follow the name pattern:

    environment.<custom>.properties


##### Textual Output

Some commands like Help limit their textual output to 80 characters per line by default. This value can be overwritten with an environment variable:

    BT__JINNI_LINE_WIDTH=...

### Installation Executable

Currently there is no such installation available, but it is planned.

## How to use Jinni
Jinni comes with a CLI (OS independent as .bat and .sh):

### Option 1 - defining request and its properties directly in the command line:

You can name the request fully qualified by its original name or use a shortened version of it
which takes only the last part of the dotted name (simple name) and transforms it by inserting dashes before each uppercase letter and lowers each uppercase letter.


```bash
> jinni fully.qualified.MyRequest property1=value1 property2=value2

> jinni my-request property1=value1 property2=value2
```

### Option 2 - defining parameters in a file:
The content of input.json may look like this:

```json
{
	"_type": "fully.qualified.MyRequest",
	"property1": "value1",
	"propeprty2": "value2"
}
```
jinni -requestFile path\to\jinnisetup\input.json

## Request Description

### com.braintribe.model.platform.setup.api.PackagePlatformSetup

**Shortcut:** package-platform-setup

Resolves the transitive asset dependencies and applies packaging expert logic that is associated for each asset nature. Each associated expert will download nature specific asset artifact parts from the configured repos and use the data to create or extend files in the setup package directory structure. The resulting directory and files structure can then be used to project that information into a specific runtime environment (eg. single tomcat, single undertow)
