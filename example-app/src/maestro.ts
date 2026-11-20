import { Capacitor } from '@capacitor/core';
import { AgeRange } from '@capgo/capacitor-age-range';
import type { AgeRangeResult } from '@capgo/capacitor-age-range';

const MAESTRO_PLUGIN_TIMEOUT_MS = 20_000;

type MaestroFlow = 'default' | 'mandatory';
type MaestroBucket = 'unavailable' | 'error' | 'completed';

const flowLabel = (flow: MaestroFlow): string =>
  flow === 'default' ? 'Maestro default never' : 'Maestro whenMandatory';

const outcomeElementId = (flow: MaestroFlow, bucket: MaestroBucket): string =>
  `maestro-static-${flow === 'default' ? 'default' : 'mandatory'}-${bucket}`;

const bucketForStatus = (status: AgeRangeResult['status']): MaestroBucket => {
  if (status === 'NOT_AVAILABLE') {
    return 'unavailable';
  }
  if (status === 'ERROR') {
    return 'error';
  }
  return 'completed';
};

const formatResultLine = (flow: MaestroFlow, status: AgeRangeResult['status']): string => {
  const bucket = bucketForStatus(status);
  const kind = bucket === 'unavailable' ? 'unavailable' : bucket === 'error' ? 'error' : 'completed';
  return `${flowLabel(flow)}: ${kind} status=${status}`;
};

const formatErrorLine = (flow: MaestroFlow, error: unknown): string => {
  const wrapped = error as { code?: string; message?: string };
  const code =
    typeof wrapped.code === 'string' && wrapped.code.length > 0 ? wrapped.code : 'PLUGIN_ERROR';
  const message =
    error instanceof Error ? error.message : typeof error === 'string' ? error : String(error);
  return `${flowLabel(flow)}: error code=${code} message=${message}`;
};

export const withMaestroTimeout = <T>(promise: Promise<T>, label: string): Promise<T> => {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new Error(`${label} timed out after ${MAESTRO_PLUGIN_TIMEOUT_MS}ms`));
    }, MAESTRO_PLUGIN_TIMEOUT_MS);
    promise.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error: unknown) => {
        clearTimeout(timer);
        reject(error instanceof Error ? error : new Error(String(error)));
      },
    );
  });
};

export const setMaestroHarnessStatus = (message: string) => {
  if (message === 'Maestro harness: starting') {
    document.getElementById('maestro-harness-boot')?.setAttribute('hidden', '');
    document.getElementById('maestro-harness-starting')?.removeAttribute('hidden');
  }
};

export const publishMaestroPluginOutcome = (flow: MaestroFlow, result: AgeRangeResult): void => {
  const bucket = bucketForStatus(result.status);
  const line = formatResultLine(flow, result.status);
  const target = document.getElementById(outcomeElementId(flow, bucket));
  if (target) {
    target.textContent = line;
  }
  document.body.classList.add(`maestro-outcome-${flow}-${bucket}`);
};

export const publishMaestroPluginError = (flow: MaestroFlow, error: unknown): void => {
  const line = formatErrorLine(flow, error);
  const target = document.getElementById(outcomeElementId(flow, 'error'));
  if (target) {
    target.textContent = line;
  }
  document.body.classList.add(`maestro-outcome-${flow}-error`);
};

export const runMaestroDefaultNever = async (): Promise<void> => {
  try {
    const result = await withMaestroTimeout(
      AgeRange.requestAgeRange({ ageGates: [13, 16, 18] }),
      'requestAgeRange default never',
    );
    publishMaestroPluginOutcome('default', result);
  } catch (error: unknown) {
    publishMaestroPluginError('default', error);
  }
};

export const runMaestroWhenMandatory = async (): Promise<void> => {
  try {
    const result = await withMaestroTimeout(
      AgeRange.requestAgeRange({
        ageGates: [13, 16, 18],
        androidAgeSignalsAccess: Capacitor.getPlatform() === 'android' ? 'whenMandatory' : undefined,
      }),
      'requestAgeRange whenMandatory',
    );
    publishMaestroPluginOutcome('mandatory', result);
  } catch (error: unknown) {
    publishMaestroPluginError('mandatory', error);
  }
};

export const runMaestroAutoHarness = async (): Promise<void> => {
  await runMaestroDefaultNever();
  if (Capacitor.getPlatform() === 'android') {
    await runMaestroWhenMandatory();
  }
};