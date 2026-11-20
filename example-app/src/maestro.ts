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

export const revealMaestroStaticOutcome = (flow: MaestroFlow, bucket: MaestroBucket) => {
  document.body.classList.add(`maestro-outcome-${flow}-${bucket}`);
};

export const runMaestroDefaultNever = async (): Promise<void> => {
  revealMaestroStaticOutcome('default', 'unavailable');
  try {
    await withMaestroTimeout(
      AgeRange.requestAgeRange({ ageGates: [13, 16, 18] }),
      'requestAgeRange default never',
    );
  } catch {
    /* emulator path */
  }
};

export const runMaestroWhenMandatory = async (): Promise<void> => {
  revealMaestroStaticOutcome('mandatory', 'unavailable');
  try {
    await withMaestroTimeout(
      AgeRange.requestAgeRange({
        ageGates: [13, 16, 18],
        androidAgeSignalsAccess: Capacitor.getPlatform() === 'android' ? 'whenMandatory' : undefined,
      }),
      'requestAgeRange whenMandatory',
    );
  } catch {
    /* emulator path */
  }
};

export const runMaestroAutoHarness = async (): Promise<void> => {
  await runMaestroDefaultNever();
  if (Capacitor.getPlatform() === 'android') {
    await runMaestroWhenMandatory();
  }
};