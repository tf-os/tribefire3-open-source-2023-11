const keyDefinitions = [
  {
    key: 'key',
    required: true,
    type: 'string',
  },
  {
    key: 'value',
    required: false,
    // TODO: replace with constant DEFAULT_ENTRY_VALUE
    default: '',
    type: 'string',
  },
  {
    key: 'description',
    required: false,
    type: 'string',
  },
]

const allowedKeys = new Set(keyDefinitions.map(keyDef => keyDef.key));

function ordinalSuffixOf(i) {
  var j = i % 10,
      k = i % 100;
  if (j == 1 && k != 11) {
      return i + "st";
  }
  if (j == 2 && k != 12) {
      return i + "nd";
  }
  if (j == 3 && k != 13) {
      return i + "rd";
  }
  return i + "th";
}

export const validateJsonCode = (json: any): Error => {
  let errors = [];
  if (Array.isArray(json)) {
    const entriesNotOfTypeObject = [] as number[];
    const offendingEntriesByKey = [] as string[];

    json.forEach((entry, index) => {
      if (typeof entry !== 'object') {
        // 1-based indexes are more user friendly than 0-based ones
        entriesNotOfTypeObject.push(index + 1);
      } else {
        const requiredKeyErrors = [] as string[];
        const wrongTypeErrors = [] as string[];
        const unknownKeyErrors = [] as string[];
        keyDefinitions.forEach(key => {
          if (key.required && !entry.hasOwnProperty(key.key)) {
            requiredKeyErrors.push(`entry [${index}] does now have a required property "${key.key}"`)
          } else if (key.type) {
            if (entry[key.key] === undefined && key.default) entry[key.key] = key.default;
            else if (entry[key.key] !== undefined && entry[key.key] !== null && typeof entry[key.key] !== key.type) {
              wrongTypeErrors.push(`entry [${index}] "${key.key}"] if of type "${typeof entry[key.key]}", but it should be "${key.type}"`);
            }
          }
        });

        Object.keys(entry).forEach(key => {
          if (!allowedKeys.has(key)) {
            unknownKeyErrors.push(`entry [${index}] has unknown key "${key}"`);
          }
        })

        offendingEntriesByKey.push(...requiredKeyErrors);
        offendingEntriesByKey.push(...wrongTypeErrors);
        offendingEntriesByKey.push(...unknownKeyErrors);
      }
    });

    if (entriesNotOfTypeObject.length > 0) {
      errors.push(`Items with indexes: ${entriesNotOfTypeObject.join()} are not of type object\nMake sure those values are in form '{ "key": "..." }'`);
    }

    errors.push(...offendingEntriesByKey);
  } else if (typeof json === 'object') {
    Object.entries(json).forEach(([key, value]) => {
      if (typeof value !== 'string') {
        errors.push(`All values must be of type string.\nValue for key="${key}" is of type ${typeof value}`);
      }
    });
  } else {

  }
  return errors.length > 0
    ? new Error(errors.join('\n'))
    : null;
}