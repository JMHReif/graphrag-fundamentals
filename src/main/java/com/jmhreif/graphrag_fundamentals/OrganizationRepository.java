package com.jmhreif.graphrag_fundamentals;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface OrganizationRepository extends Neo4jRepository<Organization, String> {
    //Section 1: Neo4j query

    //Section 3: GraphRAG - graph retrieval query
}
