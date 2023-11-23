import IntlMessageFormat from "intl-messageformat"
import type { MessageFormatElement } from "@formatjs/icu-messageformat-parser/types.d";

export class InternationalFormatter {
  private _intlMessageFormat: IntlMessageFormat;

  constructor(
    public message: string | MessageFormatElement[],
    locale: string,
  ) {
    try {
      this._intlMessageFormat = new IntlMessageFormat(message, locale);
    }
    catch (error) {
      console.error(`Error creating formatter for string "${message}"\n`, error);
    }
  }

  format = (values?: any): string => {
    if (this._intlMessageFormat) {
      try {
        return this._intlMessageFormat.format(values) as string;
      } catch (error) {
        console.error(`Error interpolating "${this.message}"\n`, error);
      }
    }
    return '';
  }
}
