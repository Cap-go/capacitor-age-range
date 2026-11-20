import { CapacitorUpdater } from '@capgo/capacitor-updater';
import { Capacitor } from '@capacitor/core';
import { AgeRange } from '@capgo/capacitor-age-range';
import type { AgeRangeResult } from '@capgo/capacitor-age-range';

import './style.css';
import {
  runMaestroAutoHarness,
  runMaestroDefaultNever,
  runMaestroWhenMandatory,
  setMaestroHarnessStatus,
} from './maestro';

const runWhenDomReady = (bootstrap: () => void): void => {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bootstrap, { once: true });
    return;
  }
  bootstrap();
};

const bootstrap = (): void => {
  const isMaestroAutoHarness = document.getElementById('maestro-marker')?.getAttribute('data-auto') === 'true';

  if (isMaestroAutoHarness) {
    setMaestroHarnessStatus('Maestro harness: starting');
    void runMaestroAutoHarness();
  }

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
  if (result.ageLower !== undefined && result.ageUpper === undefined) {
    return `${result.ageLower}+`;
  }
  if (result.ageLower === undefined && result.ageUpper !== undefined) {
    return `Up to ${result.ageUpper}`;
  }
  return `${result.ageLower} to ${result.ageUpper}`;
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
  if (result.ageRangeSource) {
    rows.push(`<p><span class="result-label">Age range source</span><strong>${result.ageRangeSource}</strong></p>`);
  }
  if (result.significantChangeStatus) {
    rows.push(
      `<p><span class="result-label">Significant change</span><strong>${result.significantChangeStatus}</strong></p>`,
    );
  }
  if (result.androidUserStatus) {
    rows.push(
      `<p><span class="result-label">Android user status</span><strong>${result.androidUserStatus}</strong></p>`,
    );
  }
  if (result.significantChangeApprovalDate ?? result.mostRecentApprovalDate) {
    rows.push(
      `<p><span class="result-label">Last approval</span><strong>${result.significantChangeApprovalDate ?? result.mostRecentApprovalDate}</strong></p>`,
    );
  }
  if (result.androidAgeSignalsAccessStatus) {
    rows.push(
      `<p><span class="result-label">Access status</span><strong>${result.androidAgeSignalsAccessStatus}</strong></p>`,
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
  const gates = parts.map((part) => {
    if (!/^\d+$/.test(part)) {
      throw new Error('Age gates must be whole numbers separated by commas.');
    }
    return Number.parseInt(part, 10);
  });
  if (gates.length === 0) {
    throw new Error('Provide at least one age gate.');
  }
  if (gates.length > 3) {
    throw new Error('Provide at most three age gates (iOS uses the first three).');
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

requestButton?.addEventListener('click', () => {
  void requestAgeRange();
});

versionButton?.addEventListener('click', () => {
  void refreshPluginVersion();
});

clearJsonButton?.addEventListener('click', () => {
  setJson(undefined);
});

const maestroDefaultButton = document.getElementById('maestro-run-default-never') as HTMLButtonElement;
const maestroMandatoryButton = document.getElementById('maestro-run-when-mandatory') as HTMLButtonElement;

maestroDefaultButton?.addEventListener('click', () => {
  void runMaestroDefaultNever();
});

maestroMandatoryButton?.addEventListener('click', () => {
  void runMaestroWhenMandatory();
});

if (!isMaestroAutoHarness) {
  void refreshPluginVersion();
}

  if (Capacitor.isNativePlatform() && !isMaestroAutoHarness) {
    CapacitorUpdater.notifyAppReady().catch((error: unknown) => {
      console.error('Capgo notifyAppReady failed', error);
    });
  }
};

runWhenDomReady(bootstrap);
