import { map, shareReplay, switchMap, tap } from 'rxjs/operators';
import { lazyLoadProperty, SvelteSubject } from '@app-configuration';
import { combineLatest, from, of } from 'rxjs';

export const createAppConfigurationState = ({
  initialAppConfiguration = null,
  initialTabIndex = 0,
} = {}) => {
  const internalAppConfigurationStream = new SvelteSubject(initialAppConfiguration);

  const appConfigurationStream = internalAppConfigurationStream
    .pipe(
      switchMap(appConfiguration =>
        appConfiguration
          ? combineLatest([
            of(appConfiguration),
            from(lazyLoadProperty(appConfiguration, 'localizations')),
            from(lazyLoadProperty(appConfiguration, 'themes')),
            from(lazyLoadProperty(appConfiguration, 'descriptors')),
          ])
          : of([null]),
      ),
      map(([appConfiguration]) => appConfiguration),
      shareReplay(1),
    )

  const appConfigurationTab = new SvelteSubject(initialTabIndex);

  function setAppConfiguration(appConfiguration: any) {
    internalAppConfigurationStream.next(appConfiguration);
  }

  return {
    appConfigurationTab,
    appConfigurationStream,
    setAppConfiguration,
  }
}