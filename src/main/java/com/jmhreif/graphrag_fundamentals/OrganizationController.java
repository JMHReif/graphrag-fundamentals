package com.jmhreif.graphrag_fundamentals;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Demo purposes
import com.jmhreif.graphrag_fundamentals.domain.OrgProjection;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;

@RestController
@RequestMapping("/")
public class OrganizationController {
    private final OrganizationRepository repository;
    private final ChatClient chatClient;
    private final Neo4jVectorStore vectorStore;
    private final GraphQueryAugmenter graphQueryAugmenter;

    String prompt = """
                You are a news expert providing answers to questions about news articles, and the organizations mentioned in them.
                Based on this question:
                {question}
                Please answer using this context:
                {context}
                """;
    String advisorPrompt = """
                You are a news expert providing answers to questions about news articles, and the organizations mentioned in them.
                """;

    public OrganizationController(OrganizationRepository repository, ChatClient.Builder builder, Neo4jVectorStore vectorStore, GraphQueryAugmenter graphQueryAugmenter) {
        this.repository = repository;
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.graphQueryAugmenter = graphQueryAugmenter;
    }

    //Demo purpose
    @GetMapping("/llm")
    public ChatResponse llm(@RequestParam String question) {
        return chatClient.prompt(question).call().chatResponse();
    }

    //Section 1: Neo4j query
    @GetMapping("/articleMentions")
    public List<Organization> getArticleMentions() {
        return repository.findOrganizations();
    }

    //Section 2: RAG - vector similarity search
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

    //Section 3: GraphRAG - vector search + graph retrieval
    @GetMapping("/manualGraphRAG")
    public String manualGraphRAG(@RequestParam String question) {
        List<Document> results = vectorStore.similaritySearch(question);
        List<Organization> orgList = repository.findGraphEntities(results.stream().map(Document::getId).toList());

        var template = new PromptTemplate(prompt)
                .create(Map.of("question", question,
                        "context", orgList.stream().map(Organization::toString).collect(Collectors.joining("\n"))));
        System.out.println("----- PROMPT -----");
        System.out.println(template);

        return chatClient.prompt(template)
                .call().content();
    }

    //Demo purpose
    @GetMapping("/graphRAG")
    public String advisedGraphRAG(@RequestParam String question) {
        Advisor retrieveAugmentAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(graphQueryAugmenter)
                .build();

        return chatClient.prompt()
                .system(advisorPrompt)
                .advisors(new SimpleLoggerAdvisor(),
                        retrieveAugmentAdvisor)
                .user(question)
                .call()
                .content();
    }

    //Demo purpose
    @GetMapping("/compare")
    public String vectorPlusComparison(String question) {
        //Small result set (default=4)
        List<Document> smallResults = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).build()
        );
        List<OrgProjection> smallCompanies = repository.findEntitiesCompare(
                smallResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        //Large result set
        List<Document> largeResults = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(8).build()
        );
        List<OrgProjection> largeCompanies = repository.findEntitiesCompare(
                largeResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        return String.format("""
            QUESTION: %s
            
            SMALL_SET_COUNT: %d
            SMALL_SET_ORGS: 
            %s
            
            LARGE_SET_COUNT: %d
            LARGE_SET_ORGS: 
            %s
            """,
                question,
                smallCompanies.size(),
                smallCompanies.stream().map(org -> org.toString()).collect(Collectors.joining("\n")),
                largeCompanies.size(),
                largeCompanies.stream().map(org -> org.toString()).collect(Collectors.joining("\n"))
        );
    }
}
