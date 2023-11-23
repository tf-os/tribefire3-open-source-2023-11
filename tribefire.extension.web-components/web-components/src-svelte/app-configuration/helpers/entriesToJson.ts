import { DEFAULT_ENTRY_VALUE, DEFAULT_INDENTATION } from "@app-configuration";

export function entriesWithDescriptorsToJson(entries: any[], descriptors: Record<string, string> | any[]): string {
  const descriptorsMap: Record<string, string> = Array.isArray(descriptors)
    ? Object.fromEntries(
      (descriptors || []).map(descriptor => [descriptor.key, descriptor.value])
    )
    : descriptors;

  const nonEmptyEntries = entries.filter(Boolean);

  return nonEmptyEntries?.length > 0
    ? '[\n' +
      nonEmptyEntries
        .map(({key, value}) => 
          `${DEFAULT_INDENTATION}{\n` +
          `    "key": ${JSON.stringify(key)},\n` +
          `    "value": ${JSON.stringify(value)},\n` +
          `    "description": ${JSON.stringify(descriptorsMap[key] || DEFAULT_ENTRY_VALUE)}\n` +
          `${DEFAULT_INDENTATION}}`
        )
        .join(',\n') +
        '\n]'
    : '[ ]';
}


export function entriesToJson(entries: any[]): string {
  const nonEmptyEntries = entries.filter(Boolean);
  return nonEmptyEntries?.length > 0
    ? `{${nonEmptyEntries.map(({key, value}, index) => 
      `${
        DEFAULT_INDENTATION
      }${
        JSON.stringify(key)
      }: ${
        JSON.stringify(value)
      }`
    ).join(',\n')}\n}`
    : '{ }';
}

