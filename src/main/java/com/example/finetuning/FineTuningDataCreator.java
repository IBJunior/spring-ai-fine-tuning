package com.example.finetuning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class FineTuningDataCreator {
    public static final String JSONL_FILE_NAME = "fly-intelligent-fine-tuning-data.jsonl";
    public static final String JSONL_DIRECTORY_PATH = "src/main/resources/jsons";

    private static final Logger log = LoggerFactory.getLogger(FineTuningDataCreator.class);
    private final FineTuningDataService fineTuningDataService;


    public FineTuningDataCreator(FineTuningDataService fineTuningDataService) {
        this.fineTuningDataService = fineTuningDataService;
    }

    public void run() throws InterruptedException {
        log.info("Creating synthetic data started...");

        List<Conversation> conversations = fineTuningDataService.getConversations();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (Conversation conversation : conversations) {
            String json = gson.toJson(conversation);
            writeToJsonFile(json.getBytes(StandardCharsets.UTF_8));
        }

        log.info("Created {} conversations", conversations.size());
        log.info("Creating synthetic data ended...");
    }


    public void writeToJsonFile(byte[] bytes) {
        Path directory = Paths.get(JSONL_DIRECTORY_PATH);
        Path filePath = directory.resolve(JSONL_FILE_NAME);
        try {
            Files.write(filePath, bytes, StandardOpenOption.APPEND);
            Files.write(filePath, "\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            //append new line
            System.out.printf("Saved %s to %s%n", JSONL_FILE_NAME, JSONL_DIRECTORY_PATH);

        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException("Error writing json file", e);
        }
    }

}
