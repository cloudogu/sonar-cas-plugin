package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.Timer;
import java.util.TimerTask;

@ServerSide
public final class SessionStoreCleaner implements ServerStartHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SessionStoreCleaner.class);
    private static final int SESSION_STORE_CLEANUP_INTERVAL_IN_SECS_DEFAULT = 60 * 30;
    private static final int SESSION_STORE_CLEANUP_DISABLED = 0;
    private static final int MILLIS = 1000;

    private final CasSessionStoreFactory sessionStoreFactory;
    private final int cleanUpIntervalInSeconds;
    private final Timer timer;

    /**
     * called with injection by SonarQube during server initialization
     */
    @SuppressWarnings("unused")
    public SessionStoreCleaner(Configuration configuration, CasSessionStoreFactory sessionStoreFactory) {
        this.sessionStoreFactory = sessionStoreFactory;
        this.cleanUpIntervalInSeconds = SonarCasProperties.SESSION_STORE_CLEANUP_INTERVAL_IN_SECS
                .getInteger(configuration, SESSION_STORE_CLEANUP_INTERVAL_IN_SECS_DEFAULT);
        this.timer = new Timer();
    }

    @Override
    public void onServerStart(Server server) {
        LOG.debug("CAS session store cleaner was configured to an interval of {} seconds ", cleanUpIntervalInSeconds);
        TimerTask task = createTask();

        if (isCleanUpDisabled()) {
            LOG.error("Found that CAS session store clean up was disabled. This should be done only in a development environment");
            return;
        }

        runTask(task);
    }

    private TimerTask createTask() {
        return new CasSessionStoreCleanerTask();
    }

    private void runTask(TimerTask task) {
        // The CAS session store is persistent. This means there _may be_ clean-up work to tend to after a server start
        long firstOccurrenceImmediate = 0;
        timer.scheduleAtFixedRate(task, firstOccurrenceImmediate, cleanUpIntervalInSeconds * MILLIS);
    }

    private boolean isCleanUpDisabled() {
        return cleanUpIntervalInSeconds == SESSION_STORE_CLEANUP_DISABLED;
    }

    void cancelTimer() {
        timer.cancel();
    }

    class CasSessionStoreCleanerTask extends TimerTask {
        @Override
        public void run() {
            LOG.debug("CAS session store clean up started.");

            CasSessionStore store = sessionStoreFactory.getInstance();
            int removedEntries = store.removeExpiredEntries();

            LOG.debug("CAS session store clean up finished and removed {} entries.", removedEntries);
        }
    }
}
