package com.lilbaek.simply.test;

import com.lilbaek.simply.test.model.Post;
import com.lilbaek.simply.test.service.PostService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class SimplyApplication {

	public static void main(final String[] args) {
		SpringApplication.run(SimplyApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(final PostService postService) {
		return args -> {
			postService.create(new Post("1234", "Hello World", "hello-world", true, LocalDate.now(), 1, "java, spring"));
		};
	}
}
