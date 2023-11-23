
import { collectionStreamsFactory } from "@app-configuration";

export const createLocalizationState = (appConfigurationStream) =>
  collectionStreamsFactory({
    appConfigurationStream: appConfigurationStream,
    appConfiguration_CollectionsPropertyName: 'localizations',
    promptMessage: 'Enter new localization name (i.e.: at, en-GB, etc.):',
    keyAlreadyExistMessage: 'Localization with name "{newKey}" already exists',
  });
