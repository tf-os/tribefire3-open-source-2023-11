export function findEntry(entriesArray: any[], key: string): any {
  return entriesArray.find(entry => entry.key === key);
}
