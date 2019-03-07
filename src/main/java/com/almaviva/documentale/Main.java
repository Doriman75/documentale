package com.almaviva.documentale;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.almaviva.documentale.interceptors.core.ContextBuilder;
import com.almaviva.documentale.interceptors.core.SecurityContext;
import com.almaviva.documentale.interceptors.core.SecurityContextBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@ConditionalOnMissingBean
	@Bean
	public ContextBuilder contextBuilder() {
		return (c,r) -> {
			Map<String, String> result = new HashMap<>();
			result.putAll(c);
			result.putAll(r);
			return result;
		};
	}

	@ConditionalOnMissingBean
	@Bean
	public SecurityContextBuilder securityContextBuilder() {
		return (c) -> new SecurityContext("no user", Collections.emptyList());
	}

	
}
