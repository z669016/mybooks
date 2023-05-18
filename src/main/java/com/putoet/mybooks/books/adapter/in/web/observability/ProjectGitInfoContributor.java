package com.putoet.mybooks.books.adapter.in.web.observability;

import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProjectGitInfoContributor extends GitInfoContributor {
    public ProjectGitInfoContributor(GitProperties properties) {
        super(properties);
    }

    @Override
    public void contribute(Info.Builder builder) {
        final Map<String, Object> map = generateContent();

        for (var entry : getProperties()) {
            addKeyValue(map, entry.getKey(), entry.getValue());
        }
        builder.withDetail("git", map);
    }

    private Map<String, Object> addKeyValue(Map<String, Object> map, String key, String value) {
        if (!key.contains(".")) {
            map.put(key, value);
        } else {
            final int dot = key.indexOf('.');
            final String parent = key.substring(0, dot);
            key = key.substring(dot + 1);

            final Object submap = map.computeIfAbsent(parent, k -> new HashMap<String, Object>());
            if (submap instanceof Map) {
                return addKeyValue((Map<String, Object>) submap, key, value);
            }
        }
        return map;
    }
}
