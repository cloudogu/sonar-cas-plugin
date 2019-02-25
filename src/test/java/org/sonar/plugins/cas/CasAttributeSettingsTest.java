package org.sonar.plugins.cas;

import org.junit.Test;
import org.sonar.api.config.Configuration;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;

public class CasAttributeSettingsTest {

    @Test
    public void getGroupsShouldReturnNoGroups_nullGroup() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.rolesAttributes", "groups");
        CasAttributeSettings sut = new CasAttributeSettings(config);
        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("groups", null);

        Set<String> actual = sut.getGroups(casAttributes);

        Set<String> expected = Collections.emptySet();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupsShouldReturnOneGroups() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.rolesAttributes", "groups");
        CasAttributeSettings sut = new CasAttributeSettings(config);
        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("groups", "leAdmin");

        Set<String> actual = sut.getGroups(casAttributes);

        assertThat(actual).contains("leAdmin");
    }

    @Test
    public void getGroupsShouldReturnSeveralGroups() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.rolesAttributes", "groups");
        CasAttributeSettings sut = new CasAttributeSettings(config);
        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("groups", Arrays.asList("keyUser", "communityManager"));

        Set<String> actual = sut.getGroups(casAttributes);

        assertThat(actual).containsOnly("keyUser", "communityManager");
    }

    @Test
    public void getMailShouldReturnMail() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.eMailAttribute", "mail");
        CasAttributeSettings sut = new CasAttributeSettings(config);
        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("mail", "hello@world");

        String actual = sut.getEmail(casAttributes);

        assertThat(actual).isEqualTo("hello@world");
    }

    @Test
    public void getFullNameShouldReturnFullName() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.fullNameAttribute", "displayName");
        CasAttributeSettings sut = new CasAttributeSettings(config);
        Map<String, Object> casAttributes = new HashMap<>();
        casAttributes.put("displayName", "Ada Lovelace");

        String actual = sut.getDisplayName(casAttributes);

        assertThat(actual).isEqualTo("Ada Lovelace");
    }

    private class SonarTestConfiguration implements Configuration {
        private final HashMap<String, String> props;

        SonarTestConfiguration() {
            this.props = new HashMap<>();
        }

        SonarTestConfiguration withAttribute(String sonarKey, String value) {
            this.props.put(sonarKey, value);
            return this;
        }

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(props.get(key));
        }

        @Override
        public boolean hasKey(String key) {
            return props.containsKey(key);
        }

        @Override
        public String[] getStringArray(String key) {
            return new String[0];
        }
    }
}