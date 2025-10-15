package com.jmhreif.graphrag_fundamentals.solutions.section2;

import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface OrganizationRepository extends Neo4jRepository<Organization, String> {
    @Query("MATCH (o:Organization)<-[rel:MENTIONS]-(a:Article) " +
            "RETURN o, collect(rel), collect(a) LIMIT 3;")
    List<Organization> findOrganizations();
}
