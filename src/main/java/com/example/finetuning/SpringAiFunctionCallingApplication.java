package com.example.finetuning;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiFunctionCallingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiFunctionCallingApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(FineTuningDataCreator fineTuningDataCreator) {
        return (args) -> {
            //uncomment this line top run the data generation
            // fineTuningDataCreator.run();
        };
    }

}
