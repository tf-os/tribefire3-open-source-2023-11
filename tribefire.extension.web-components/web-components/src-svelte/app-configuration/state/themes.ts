
import { collectionStreamsFactory } from "@app-configuration";

export const createThemeState = (appConfigurationStream) =>
  collectionStreamsFactory({
    appConfigurationStream: appConfigurationStream,
    appConfiguration_CollectionsPropertyName: 'themes',
    promptMessage: 'Enter new theme name (i.e. light, dark, etc.):',
    keyAlreadyExistMessage: 'Theme name {newKey} already exists',
  });
