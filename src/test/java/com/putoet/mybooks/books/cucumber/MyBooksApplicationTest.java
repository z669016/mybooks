package com.putoet.mybooks.books.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.web.client.RestTemplate;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features")
public class MyBooksApplicationTest extends MyBooksE2EBase {
    public MyBooksApplicationTest(RestTemplate sslRestTemplate, ObjectMapper mapper) {
        super(sslRestTemplate, mapper);
    }
}
