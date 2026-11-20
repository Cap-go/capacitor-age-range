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
  'maestro-outcome-default-detail',
  'maestro-outcome-mandatory-detail',
  'maestro-ios-a11y-outcome',
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

if (/maestroCiUnavailable/.test(maestroSource)) {
  console.error('Maestro harness must not reference plugin maestroCiUnavailable bypass');
  process.exit(1);
}

if (!/status=\$\{status\}/.test(maestroSource)) {
  console.error('Maestro harness must encode plugin status in outcome lines');
  process.exit(1);
}

if (/publishMaestroPluginOutcome[\s\S]*?await[\s\S]*?AgeRange\.requestAgeRange/.test(maestroSource) === false) {
  const defaultBlock = maestroSource.match(/runMaestroDefaultNever[\s\S]*?^};/m)?.[0] ?? '';
  if (!/await[\s\S]*AgeRange\.requestAgeRange/.test(defaultBlock)) {
    console.error('Maestro harness must await AgeRange.requestAgeRange before publishing');
    process.exit(1);
  }
}

console.log('Maestro selectors OK:', requiredIds.join(', '));
console.log('Maestro harness outcome wiring OK');