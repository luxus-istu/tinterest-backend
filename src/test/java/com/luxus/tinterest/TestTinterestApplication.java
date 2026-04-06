package com.luxus.tinterest;

import org.springframework.boot.SpringApplication;

public class TestTinterestApplication {

	public static void main(String[] args) {
		SpringApplication.from(TinterestApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
