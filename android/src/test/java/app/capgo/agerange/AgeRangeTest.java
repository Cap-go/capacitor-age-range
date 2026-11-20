package app.capgo.agerange;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AgeRangeTest {

    @Test
    public void normalizeAccessMode_preservesKnownModes() {
        assertEquals(AgeRange.ACCESS_NEVER, AgeRange.normalizeAccessMode("never"));
        assertEquals(AgeRange.ACCESS_ALWAYS, AgeRange.normalizeAccessMode("always"));
        assertEquals(AgeRange.ACCESS_WHEN_MANDATORY, AgeRange.normalizeAccessMode("whenMandatory"));
    }

    @Test
    public void normalizeAccessMode_unknownFallsBackToNever() {
        assertEquals(AgeRange.ACCESS_NEVER, AgeRange.normalizeAccessMode(""));
        assertEquals(AgeRange.ACCESS_NEVER, AgeRange.normalizeAccessMode("whenever"));
        assertEquals(AgeRange.ACCESS_NEVER, AgeRange.normalizeAccessMode("ALWAYS"));
    }
}
