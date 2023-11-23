export function arrayMinusSet<T>(array: T[], set: Set<T>): T[] {
  return array.reduce(
    (acc, currentKey) => {
      if (!set.has(currentKey)) {
        acc.push(currentKey);
      }
      return acc;
    },
    []
  )
}
