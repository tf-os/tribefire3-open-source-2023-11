<script>
	import {
		InternationalFormatter,
		IntlMessageFormat,
		activeThemeName,
		addAppLocalization,
		addAppLocalizations,
		addThemes,
		addAppTheme,
		addAppThemes,
		addDictionaries,
		addDictionary,
		getLocaleFromNavigator,
		i18nInit,
		i18nLocale,
		i18nNumber,
		injectTheme,
		themeAsRecordStore,
		themeAsStyleStore,
		translate,
		translateAsStore,
		validateI18nString,
	} from '../../src/svelte'
	export let name;

	// localization
	addDictionaries({
		en: { hello: 'hello' },
		es: { hello: 'hola' },
		de: { hello: 'hallo' },
		fr: { hello: 'bonjour' },
	})


	// localization
	// make sure svelte-i18n is not in your package.json
	i18nInit({
		fallbackLocale: 'fr',
		initialLocale: getLocaleFromNavigator(),
	});

	$: console.log('locale: ', $i18nLocale);

	const helloLocalized = translateAsStore('hello');

	$: console.log('helloLocalized: ', $helloLocalized);

	setTimeout(() => i18nLocale.set('es'), 1000);




	// theme
	addThemes({
		light: { logo: 'light-logo.png'},
		dark: { logo: 'dark-logo.png'},
	});

	activeThemeName.set('light');

	$: activeTheme = themeAsRecordStore($activeThemeName);

	$: console.log('theme: ', $activeThemeName);
	$: console.log('logo: ', $activeTheme.logo);

	setTimeout(() => activeThemeName.set('dark'), 1100);

</script>

<main>

</main>

<style>
	main {
		text-align: center;
		padding: 1em;
		max-width: 240px;
		margin: 0 auto;
	}

	h1 {
		color: #ff3e00;
		text-transform: uppercase;
		font-size: 4em;
		font-weight: 100;
	}

	@media (min-width: 640px) {
		main {
			max-width: none;
		}
	}
</style>