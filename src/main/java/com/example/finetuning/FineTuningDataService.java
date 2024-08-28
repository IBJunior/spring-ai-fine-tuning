package com.example.finetuning;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FineTuningDataService {
    Logger log = LoggerFactory.getLogger(FineTuningDataService.class);
    public static final String SYSTEM_MESSAGE_CONTENT = """
            Tu es un assistant qui fournit des informations concises et précises sur
            la compagnie aérienne Fly Intelligent en fonction des requêtes des utilisateurs
            """;
    private static final long MAX_TOKEN_LENGTH = 128_000;
    public static final int WAITING_TIME_BETWEEN_CHAT_CLIENT_CALL = 4000;

    @Value("classpath:fine-tuning-prompt-template.st")
    private Resource promptResource;
    @Value("classpath:documents/intro-fly-intelligent.pdf")
    private String introDocument;
    @Value("classpath:documents/fly-intelligent-faq.pdf")
    private String faqDocument;

    private final ChatClient chatClient;


    public FineTuningDataService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public List<Conversation> getConversations() throws InterruptedException {
        List<Conversation> allConversations = new ArrayList<>();
        List<Document> documents = loadPagesFromDocument(faqDocument, introDocument);

        BeanOutputConverter<List<Conversation>> outputConverter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<Conversation>>() {
                });

        PromptTemplate promptTemplate = new PromptTemplate(promptResource);

        for (Document document : documents) {

            Prompt prompt = promptTemplate.create(
                    Map.of("numberOfConversation", "6",
                            "textPassage", document.getContent())
            );

            //verify the length of the prompt not to exceed maximum token accepted by the model
            long tokenLength = getTokenLength(prompt.getContents());
            log.info("Number of Token for passage {} is {} tokens ", document.getContent().substring(1, 100), tokenLength);
            if (tokenLength > MAX_TOKEN_LENGTH) {
                continue;
            }

            String content = this.chatClient
                    .prompt(prompt)
                    .call()
                    .content();

            //verify the response before converting
            if (content != null && !content.isBlank()) {
                List<Conversation> conversations = outputConverter.convert(content);

                //verify the length before adding the response
                if (conversations != null && !conversations.isEmpty()) {
                    conversations.forEach(this::addSystemMessageContent);
                    allConversations.addAll(conversations);
                }
            }

            // Wait after each call to the model to not saturate the APIs
            Thread.sleep(WAITING_TIME_BETWEEN_CHAT_CLIENT_CALL);


        }

        return allConversations;
    }

    private long getTokenLength(String content) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding encoding = registry.getEncoding(EncodingType.CL100K_BASE);
        return encoding.countTokens(content);
    }

    // add system to reduce the number of tokens
    private void addSystemMessageContent(Conversation conversation) {
        List<Conversation.Message> messages = new ArrayList<>();

        Conversation.Message message = new Conversation.Message();
        message.setContent(SYSTEM_MESSAGE_CONTENT);
        message.setRole("system");
        messages.add(message);
        messages.addAll(conversation.getMessages());
        conversation.setMessages(messages);

    }

    private List<Document> loadPagesFromDocument(String... resources) {
        List<Document> documents = new ArrayList<>();
        for (String resourceURL : resources) {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resourceURL,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfBottomTextLinesToDelete(0)
                                    .withNumberOfTopPagesToSkipBeforeDelete(0)
                                    .build())
                            .withPagesPerDocument(1)
                            .build());

            documents.addAll(pdfReader.get());
        }
        return documents;
    }
}
