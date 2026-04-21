package com.jmhreif.graphrag_fundamentals.agentic;

import com.jmhreif.graphrag_fundamentals.OrganizationRepository;
import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//Demo purpose
@Component
public class RAGTools {
    private final Neo4jVectorStore vectorStore;
    private final OrganizationRepository repository;

    public RAGTools(Neo4jVectorStore vectorStore, OrganizationRepository repository) {
        this.vectorStore = vectorStore;
        this.repository = repository;
    }

    @Tool(description = "Semantic search over news article text. Use for content, topic, or sentiment questions (e.g. cybersecurity threats, market news, geopolitical events).")
    public String vectorSearch(String query) {
        List<Document> results = vectorStore.similaritySearch(query);

        String formattedResults = results.stream()
                .map(Document::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Vector Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }

    @Tool(description = "Retrieve organizations with their industry categories and locations from the knowledge graph, enriched with relevant articles. Use for entity-focused or trend questions about companies, sectors, cities, or time-based industry activity.")
    public String graphEnrichedSearch(String query) {
        List<Document> vectorResults = vectorStore.similaritySearch(query);

        List<Organization> graphResults = repository.findGraphEntities(
                vectorResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        String formattedResults = graphResults.stream()
                .map(Organization::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Graph Enriched Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }
}
