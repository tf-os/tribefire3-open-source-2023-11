export function compareEntries(entry1: any, entry2: any): number {
  return (entry1?.key ?? '').localeCompare(entry2?.key ?? '');
}