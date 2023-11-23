import IntlMessageFormat from "intl-messageformat";

export const validateI18nString = (
  stringToValidate: string,
  locale?: string,
): Error | null => {
  try {
    const formatter = new IntlMessageFormat(stringToValidate, locale);
    const ast = formatter.getAst();
    const values = ast
      .filter(astNode => astNode.type > 0)
      .reduce((acc, astNode) => {
        acc[(astNode as any).value] = null;
        return acc;
      }, {});
    formatter.format(values);
    return null;
  } catch (error) {
    // console.error('validateI18nString error', s, error);
    return error;
  }
}
