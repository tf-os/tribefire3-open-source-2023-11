import IntlMessageFormat from 'intl-messageformat'

const transformStringForFormatJS = (s: string) => {
  s ??= '';
  return s.replaceAll('`', '\'');
}

export const validateInterpolationString = (s: string, locale = 'en'): Error | null => {
  try {
    console.log('validateInterpolationString', s);
    const format = new IntlMessageFormat(transformStringForFormatJS(s), locale);
    (window as any).format = format;
  } catch (error) {
    console.error('validateInterpolationString: error');
    console.error(error.message);
  }
  return null;
}
