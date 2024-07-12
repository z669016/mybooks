package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.SiteType;
import com.putoet.mybooks.books.domain.validation.SiteMapConstraint;
import jakarta.validation.constraints.NotBlank;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public record NewAuthorRequest(@NotBlank String name, @SiteMapConstraint Map<String,String> sites) {
    public Map<SiteType, URL> sitesWithURLs() {
        return sitesWithURLs(sites);
    }

    public static Map<SiteType, URL> sitesWithURLs(Map<String,String> sites) {
        try {
            final var domain = new HashMap<SiteType, URL>();
            for (String key : sites.keySet()) {
                domain.put(new SiteType(key), new URL(sites.get(key)));
            }
            return domain;
        } catch (MalformedURLException | RuntimeException exc) {
            throw new RuntimeException(exc.getMessage(), exc);
        }
    }
}
