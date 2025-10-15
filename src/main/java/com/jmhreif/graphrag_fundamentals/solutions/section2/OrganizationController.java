package com.jmhreif.graphrag_fundamentals.solutions.section2;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class OrganizationController {
    private final OrganizationRepository repository;
    private final ChatClient chatClient;
    private final Neo4jVectorStore vectorStore;

    String prompt = """
                You are a news expert providing answers to questions about news articles, and the organizations mentioned in them.
                Based on this question:
                {question}
                Please answer using this context:
                {context}
                """;

    public OrganizationController(OrganizationRepository repository, ChatClient.Builder builder, Neo4jVectorStore vectorStore) {
        this.repository = repository;
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/articleMentions")
    public List<Organization> getArticleMentions() {
        return repository.findOrganizations();
    }

    @GetMapping("/manualRAG")
    public String manualVectorRAG(@RequestParam String question) {
        List<Document> results = vectorStore.similaritySearch(question);

        var template = new PromptTemplate(prompt)
                .create(Map.of("question", question,
                        "context", results.stream().map(Document::toString).collect(Collectors.joining("\n"))));
        System.out.println("----- PROMPT -----");
        System.out.println(template);

        return chatClient.prompt(template)
                .user(question)
                .call().content();
    }
}
