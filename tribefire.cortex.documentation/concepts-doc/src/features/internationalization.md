# Internationalization

Internationalization (i18n) is handled using the `I18n` model.

The `I18nBundle` entity type is used as a base type for the internationalization functionality.


```java
@Abstract
public interface I18nBundle extends GenericEntity {

	EntityType<I18nBundle> T = EntityTypes.T(I18nBundle.class);
	
	String locale = "locale";

	String getLocale();
	void setLocale(String locale);
}
```

Each bundle has a String property `locale` which describes the language of your internationalization package, for example: `en`, `pl` or `de`.

## `I18nBundle` and `LocalizedString`

The idea of the `I18nBundle` is to prepare localized messages which can be used in code (e.g. in a `ServiceProcessor`) when messages are returned to the user, for example when a notification is sent. `LocalizedStrings` are used mainly with the metadata of a model (e.g. `Name` or `Description`) as well as in the workbench (folder names).

> For information about using localization in Control Center via `LocalizedString`, see [Using Localization](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_localization.md).


## Custom Localizable Properties

You can provide your own localized String properties by creating a subtype of the `I18nBundle` entity and adding properties representing localizable messages.

By adding `@Initailizer` annotations to the properties, you can control the default values for the properties. To have the same properties presented in different languages, you must create an instance of `I18nBundle` for every language you need. Whenever a localized property is needed in code you can use the i18n support provided with Wire to get the proper `I18nBundle` and fetch the message properties via the standard `get()` method.

## Example

* Deployment model

    ```java
    public interface AdxI18nBundle extends I18nBundle {

        EntityType<AdxI18nBundle> T = EntityTypes.T(AdxI18nBundle.class);
        
        String contentCreated = "contentCreated";
        
        @Initializer("'Created ${entryType} ${entryName}'")
        String getContentCreatedMessage();
        void setContentCreatedMessage(String contentCreatedMessage);

        @Initializer("'Content Download'")
        String getContentDownloadMessage();
        void setContentDownloadMessage(String contentDownloadMessage);

        @Initializer("'Started Conversion Job for Content: ${contentName}'\nID: ${jobId}")
        String getContentConversionJobStartedMessage();
        void setContentConversionJobStartedMessage(String contentConversionJobStartedMessage);
    }
    ```

* Deployable space (WIRE):

    ```java
        @Import
        protected I18nContract i18n;

    // bean = new AdxServiceProcessor() ..
    bean.setI18n(i18n.i18n());
    ```

    > For more information, see [Wire](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire.md).

* Service processor

    Usage in the Processor which sends back localized message notifications to the client:

    ```java
    return Notifications.build()
                                .add()
                                    .message()
                                        .info()
                                        .pattern(getAdxI18nBundle().getContentCreatedMessage())
                                            .var("entryType",entryTypeDisplay())
                                            .var("entryName",this.entry.getName())
                                        .format()
                                        .close()
                                    .command()
                                        .reloadView("Reload View")
                                .close()
                                .list();
    ```

    > For more information, see [Service Processor](asset://tribefire.cortex.documentation:concepts-doc/features/service-model/service_processor.md).


Note, that in above example the `contentCreatedMessage` is taken from the bundle. This returns the localized message according to the current user session.

Since this message is defined with variables the newly introduced `pattern()` builder on the Notification API can be used to resolve theses variables from a passed context `(.var(..))`.
