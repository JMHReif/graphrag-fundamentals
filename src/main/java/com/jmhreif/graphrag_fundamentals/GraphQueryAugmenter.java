package com.jmhreif.graphrag_fundamentals;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//Demo purpose
@Component
public class GraphQueryAugmenter implements QueryAugmenter {
    private final OrganizationRepository orgRepository;

    public GraphQueryAugmenter(OrganizationRepository orgRepository) {
        this.orgRepository = orgRepository;
    }

    @Override
    public Query augment(Query query, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return query;
        }

        // Extract document IDs for graph lookup
        List<String> docIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        // Get books from graph
        List<Organization> books = orgRepository.findGraphEntities(docIds);

        if (books.isEmpty()) {
            return query;
        }

        // Augment query with book context
        String orgContext = books.stream()
                .map(Organization::toString)
                .collect(Collectors.joining("\n"));

        String augmentedText = query.text() + "\n\nAdditional context:\n" + orgContext;
        return new Query(augmentedText);
    }
}
