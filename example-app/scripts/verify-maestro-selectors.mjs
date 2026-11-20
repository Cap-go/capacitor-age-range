import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const htmlPath = resolve(import.meta.dirname, '../index.html');
const html = readFileSync(htmlPath, 'utf8');

const requiredIds = [
  'maestro-ready',
  'maestro-run-default-never',
  'maestro-run-when-mandatory',
  'maestro-marker',
];

const missing = requiredIds.filter((id) => !html.includes(`id="${id}"`));

if (missing.length > 0) {
  console.error('Missing Maestro selector ids:', missing.join(', '));
  process.exit(1);
}

console.log('Maestro selectors OK:', requiredIds.join(', '));