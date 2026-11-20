package app.capgo.agerange;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.google.android.play.agesignals.AgeSignalsManager;
import org.junit.Test;

public class MandatorySharingProbeTest {

    @Test
    public void resolve_withoutApplicableAgeFeaturesApi_invokesUnavailableRunnable() {
        Runnable onMandatory = mock(Runnable.class);
        Runnable onNotMandatory = mock(Runnable.class);
        Runnable onUnavailable = mock(Runnable.class);

        MandatorySharingProbe.resolve(mock(AgeSignalsManager.class), onMandatory, onNotMandatory, onUnavailable);

        verify(onUnavailable).run();
        verifyNoInteractions(onMandatory, onNotMandatory);
    }
}
