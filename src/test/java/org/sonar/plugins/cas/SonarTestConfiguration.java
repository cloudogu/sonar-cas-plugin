package org.sonar.plugins.cas;

import org.sonar.api.config.Configuration;

import java.util.HashMap;
import java.util.Optional;

public class SonarTestConfiguration implements Configuration {
    private final HashMap<String, String> props;

    public SonarTestConfiguration() {
        this.props = new HashMap<>();
    }

    public SonarTestConfiguration withAttribute(String sonarKey, String value) {
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
