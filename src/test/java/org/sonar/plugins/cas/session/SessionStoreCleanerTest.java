package org.sonar.plugins.cas.session;

import org.junit.After;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.SonarTestConfiguration;

import static org.mockito.Mockito.*;

public class SessionStoreCleanerTest {

    private SessionStoreCleaner cleaner;

    @After
    public void tearDown() throws Exception {
        cleaner.cancelTimer();
    }

    @Test(timeout = 1000L)
    public void cleanUpIntervalOfZeroShouldNotStart() {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStore.cleanUpIntervalInSeconds", "0");
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        this.cleaner = new SessionStoreCleaner(configuration, sessionStoreFactory);

        cleaner.onServerStart(null);

        VerificationMode noInteraction = times(0);
        verify(sessionStoreFactory, noInteraction).getInstance();
    }

    @Test
    public void cleanUpIntervalOfNonZeroShouldStartCleanup() {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStore.cleanUpIntervalInSeconds", "1000");
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        CasSessionStore sessionStore = mock(CasSessionStore.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);
        when(sessionStore.removeExpiredEntries()).thenReturn(999);
        this.cleaner = new SessionStoreCleaner(configuration, sessionStoreFactory);

        cleaner.onServerStart(null);

        VerificationMode atLeastOnce = atLeast(1);
        verify(sessionStoreFactory, atLeastOnce).getInstance();
        verify(sessionStore, atLeastOnce).removeExpiredEntries();
    }

    @Test
    public void cleanerTaskShouldCallRemoveExpiredEntries() {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStore.cleanUpIntervalInSeconds", "1000");
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        CasSessionStore sessionStore = mock(CasSessionStore.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);
        when(sessionStore.removeExpiredEntries()).thenReturn(999);

        cleaner = new SessionStoreCleaner(configuration, sessionStoreFactory);
        SessionStoreCleaner.CasSessionStoreCleanerTask task = cleaner.new CasSessionStoreCleanerTask();

        task.run();

        VerificationMode atLeastOnce = atLeast(1);
        verify(sessionStoreFactory, atLeastOnce).getInstance();
        verify(sessionStore, atLeastOnce).removeExpiredEntries();
    }
}