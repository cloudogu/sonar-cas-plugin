package org.sonar.plugins.cas.session;

import org.junit.Test;
import org.sonar.plugins.cas.SonarTestConfiguration;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class CasSessionStoreFactoryTest {

    @Test
    public void getInstance() throws IOException {
        SonarTestConfiguration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStorePath", "/tmp");
        CasSessionStoreFactory sut = new CasSessionStoreFactory(config);

        CasSessionStore actual = sut.getInstance();

        assertThat(actual).isNotNull().isInstanceOf(CasSessionStore.class);
    }
}