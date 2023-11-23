import { languageMap } from '../../utils/language-emoji'

export const languageToEmoji = (language: string): string => {
  const languageLowerCase = (language ?? '').toLocaleLowerCase();

  if (languageMap.has(languageLowerCase)) return languageMap.get(languageLowerCase)[2];

  return '&#x1F3F3;&#xFE0F;';
};

export const languageToDescription = (language: string): string => {
  const languageLowerCase = (language ?? '').toLocaleLowerCase();

  if (languageMap.has(languageLowerCase)) return languageMap.get(languageLowerCase)[1];

  return '';
};
