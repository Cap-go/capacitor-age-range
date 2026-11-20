import { Capacitor } from '@capacitor/core';
import { AgeRange } from '@capgo/capacitor-age-range';

const MAESTRO_PLUGIN_TIMEOUT_MS = 20_000;

type MaestroFlow = 'default' | 'mandatory';
type MaestroBucket = 'unavailable' | 'error' | 'completed';

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

export const classifyMaestroOutcome = (status: string | undefined, isError: boolean): MaestroBucket => {
  if (isError) {
    return 'error';
  }
  if (!status || status === 'NOT_AVAILABLE' || status === 'ERROR') {
    return 'unavailable';
  }
  return 'completed';
};

const staticOutcomeId = (flow: MaestroFlow, bucket: MaestroBucket): string =>
  `maestro-static-${flow}-${bucket}`;

export const revealMaestroStaticOutcome = (flow: MaestroFlow, bucket: MaestroBucket) => {
  document.querySelectorAll<HTMLElement>('[data-maestro-static-outcome]').forEach((element) => {
    element.setAttribute('hidden', '');
  });
  document.getElementById(staticOutcomeId(flow, bucket))?.removeAttribute('hidden');
};

export const runMaestroDefaultNever = async (): Promise<void> => {
  try {
    const result = await withMaestroTimeout(
      AgeRange.requestAgeRange({ ageGates: [13, 16, 18] }),
      'requestAgeRange default never',
    );
    revealMaestroStaticOutcome('default', classifyMaestroOutcome(result.status, false));
  } catch {
    revealMaestroStaticOutcome('default', 'error');
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
    revealMaestroStaticOutcome('mandatory', classifyMaestroOutcome(result.status, false));
  } catch {
    revealMaestroStaticOutcome('mandatory', 'error');
  }
};

export const runMaestroAutoHarness = async (): Promise<void> => {
  await runMaestroDefaultNever();
  if (Capacitor.getPlatform() === 'android') {
    await runMaestroWhenMandatory();
  }
};