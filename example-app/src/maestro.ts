import { Capacitor } from '@capacitor/core';
import { AgeRange } from '@capgo/capacitor-age-range';

const markerEl = () => document.getElementById('maestro-marker') as HTMLElement | null;

const setMaestroMarker = (message: string) => {
  const el = markerEl();
  if (!el) {
    return;
  }
  el.hidden = false;
  el.textContent = message;
};

const NATIVE_CALL_TIMEOUT_MS = 90_000;

const withTimeout = async <T>(promise: Promise<T>, label: string): Promise<T> => {
  let timer: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timer = setTimeout(() => {
      reject(new Error(`${label} timed out after ${NATIVE_CALL_TIMEOUT_MS}ms`));
    }, NATIVE_CALL_TIMEOUT_MS);
  });
  try {
    return await Promise.race([promise, timeout]);
  } finally {
    if (timer) {
      clearTimeout(timer);
    }
  }
};

export const classifyMaestroOutcome = (status: string | undefined, isError: boolean): string => {
  if (isError) {
    return 'error';
  }
  if (!status || status === 'NOT_AVAILABLE' || status === 'ERROR') {
    return 'unavailable';
  }
  return 'completed';
};

export const runMaestroDefaultNever = async (): Promise<void> => {
  try {
    const result = await withTimeout(
      AgeRange.requestAgeRange({ ageGates: [13, 16, 18] }),
      'requestAgeRange default never',
    );
    const outcome = classifyMaestroOutcome(result.status, false);
    setMaestroMarker(`Maestro default never: ${outcome} (${result.status})`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    setMaestroMarker(`Maestro default never: error (${message})`);
  }
};

export const runMaestroWhenMandatory = async (): Promise<void> => {
  try {
    const result = await withTimeout(
      AgeRange.requestAgeRange({
        ageGates: [13, 16, 18],
        androidAgeSignalsAccess: Capacitor.getPlatform() === 'android' ? 'whenMandatory' : undefined,
      }),
      'requestAgeRange whenMandatory',
    );
    const outcome = classifyMaestroOutcome(result.status, false);
    setMaestroMarker(`Maestro whenMandatory: ${outcome} (${result.status})`);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    setMaestroMarker(`Maestro whenMandatory: error (${message})`);
  }
};