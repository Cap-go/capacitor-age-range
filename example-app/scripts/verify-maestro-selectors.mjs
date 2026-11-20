import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const htmlPath = resolve(import.meta.dirname, '../index.html');
const maestroPath = resolve(import.meta.dirname, '../src/maestro.ts');
const html = readFileSync(htmlPath, 'utf8');
const maestroSource = readFileSync(maestroPath, 'utf8');

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

if (maestroSource.includes('revealMaestroStaticOutcome')) {
  console.error('Maestro harness must not call revealMaestroStaticOutcome (pre-plugin stub)');
  process.exit(1);
}

if (!/publishMaestroPluginOutcome\(\s*['"]default['"]/s.test(maestroSource)) {
  console.error('Maestro harness must publish outcomes from plugin results');
  process.exit(1);
}

console.log('Maestro selectors OK:', requiredIds.join(', '));
console.log('Maestro harness outcome wiring OK');