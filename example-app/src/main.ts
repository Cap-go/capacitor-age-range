import { CapacitorUpdater } from '@capgo/capacitor-updater';
import { Capacitor } from '@capacitor/core';
import { AgeRange } from '@capgo/capacitor-age-range';
import type { AgeRangeResult } from '@capgo/capacitor-age-range';

import './style.css';

const pluginVersionEl = document.getElementById('plugin-version') as HTMLElement;
const statusChipEl = document.getElementById('status-chip') as HTMLElement;
const ageGatesInput = document.getElementById('age-gates') as HTMLInputElement;
const requestButton = document.getElementById('request-age-range') as HTMLButtonElement;
const versionButton = document.getElementById('get-version') as HTMLButtonElement;
const clearJsonButton = document.getElementById('clear-json') as HTMLButtonElement;
const resultCard = document.getElementById('result-card') as HTMLDivElement;
const jsonOutput = document.getElementById('json-output') as HTMLPreElement;

const setJson = (value: unknown) => {
  if (value === undefined) {
    jsonOutput.textContent = '{}';
    return;
  }
  jsonOutput.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
};

const setStatusChip = (status: string | null) => {
  statusChipEl.textContent = status ?? 'None';
  statusChipEl.classList.remove('muted', 'ok', 'warn', 'error');
  if (!status) {
    statusChipEl.classList.add('muted');
    return;
  }
  if (status === 'SHARING') {
    statusChipEl.classList.add('ok');
    return;
  }
  if (status === 'DECLINED_SHARING' || status === 'NOT_AVAILABLE') {
    statusChipEl.classList.add('warn');
    return;
  }
  statusChipEl.classList.add('error');
};

const formatRange = (result: AgeRangeResult): string => {
  if (result.ageLower === undefined && result.ageUpper === undefined) {
    return 'No numeric range returned';
  }
  const lower = result.ageLower ?? '?';
  const upper = result.ageUpper ?? '18+';
  return `${lower} to ${upper}`;
};

const renderResultCard = (result: AgeRangeResult) => {
  const rows: string[] = [
    `<p><span class="result-label">Status</span><strong>${result.status}</strong></p>`,
    `<p><span class="result-label">Age range</span><strong>${formatRange(result)}</strong></p>`,
  ];

  if (result.declarationSource) {
    rows.push(
      `<p><span class="result-label">Declaration</span><strong>${result.declarationSource}</strong></p>`,
    );
  }
  if (result.androidUserStatus) {
    rows.push(
      `<p><span class="result-label">Android user status</span><strong>${result.androidUserStatus}</strong></p>`,
    );
  }
  if (result.mostRecentApprovalDate) {
    rows.push(
      `<p><span class="result-label">Last approval</span><strong>${result.mostRecentApprovalDate}</strong></p>`,
    );
  }
  if (result.installId) {
    rows.push(`<p><span class="result-label">Install ID</span><strong>${result.installId}</strong></p>`);
  }

  resultCard.innerHTML = rows.join('');
};

const renderErrorCard = (message: string) => {
  resultCard.innerHTML = `<p class="result-error">${message}</p>`;
  setStatusChip('ERROR');
};

const parseAgeGates = (raw: string): number[] => {
  const parts = raw
    .split(/[,;\s]+/)
    .map((part) => part.trim())
    .filter(Boolean);
  const gates = parts.map((part) => Number.parseInt(part, 10));
  if (gates.some((gate) => Number.isNaN(gate))) {
    throw new Error('Age gates must be whole numbers separated by commas.');
  }
  if (gates.length === 0) {
    throw new Error('Provide at least one age gate.');
  }
  return gates;
};

const refreshPluginVersion = async () => {
  try {
    const { version } = await AgeRange.getPluginVersion();
    pluginVersionEl.textContent = version;
    setJson({ getPluginVersion: { version } });
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    pluginVersionEl.textContent = 'Error';
    setJson({ error: message });
  }
};

const requestAgeRange = async () => {
  requestButton.disabled = true;
  try {
    const ageGates = parseAgeGates(ageGatesInput.value);
    const result = await AgeRange.requestAgeRange({ ageGates });
    setStatusChip(result.status);
    renderResultCard(result);
    setJson(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    renderErrorCard(message);
    setJson({ error: message, platform: Capacitor.getPlatform() });
  } finally {
    requestButton.disabled = false;
  }
};

requestButton.addEventListener('click', () => {
  void requestAgeRange();
});

versionButton.addEventListener('click', () => {
  void refreshPluginVersion();
});

clearJsonButton.addEventListener('click', () => {
  setJson(undefined);
});

void refreshPluginVersion();

if (Capacitor.isNativePlatform()) {
  CapacitorUpdater.notifyAppReady().catch((error) => {
    console.error('Capgo notifyAppReady failed', error);
  });
}
