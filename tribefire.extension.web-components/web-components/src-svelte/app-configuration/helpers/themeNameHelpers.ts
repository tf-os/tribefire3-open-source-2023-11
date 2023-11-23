export const themeNameToEmoji = (themeName: string): string => {
  const languageLowerCase = (themeName ?? '').toLocaleLowerCase();

  if (themeMap.has(languageLowerCase)) return themeMap.get(languageLowerCase)[1];

  return themeMap.get('default')[1];
};

const themeMap = new Map([
  ['light', ['☀️', '&#9728;']],
  ['dark', ['🌚', '&#127761;']],
  ['default', ['🎨', '&#127912;']],
]);
