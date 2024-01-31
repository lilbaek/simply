package com.lilbaek.simply;

import com.lilbaek.simply.model.Post;
import com.lilbaek.simply.service.PostService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class SimplyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimplyApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(PostService postService) {
		return args -> {
			postService.create(new Post("1234", "Hello World", "hello-world", true, LocalDate.now(), 1, "java, spring"));
		};
	}
}
