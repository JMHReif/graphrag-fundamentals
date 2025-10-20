package com.jmhreif.graphrag_fundamentals;

import com.jmhreif.graphrag_fundamentals.domain.OrgProjection;
import com.jmhreif.graphrag_fundamentals.domain.Organization;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface OrganizationRepository extends Neo4jRepository<Organization, String> {
    //Section 1: Neo4j query
    @Query("MATCH (o:Organization)<-[rel:MENTIONS]-(a:Article) " +
            "RETURN o, collect(rel), collect(a) LIMIT 3;")
    List<Organization> findOrganizations();

    //Section 3: GraphRAG - graph retrieval query
    @Query("MATCH (o:Organization)<-[rel:MENTIONS]-(a:Article)-[rel2:HAS_CHUNK]->(c:Chunk) " +
            "WHERE c.id IN $chunkIds " +
            "OPTIONAL MATCH (o)-[rel3:HAS_CATEGORY]->(i:IndustryCategory) " +
            "RETURN o, collect(rel), collect(a), collect(rel2), collect(c), collect(rel3), collect(i);")
    List<Organization> findGraphEntities(List<String> chunkIds);

    //Demo purpose
    @Query("MATCH (o:Organization)<-[rel:MENTIONS]-(a:Article)-[rel2:HAS_CHUNK]->(c:Chunk) " +
            "WHERE c.id IN $chunkIds " +
            "RETURN o ORDER BY o.name DESC;")
    List<OrgProjection> findEntitiesCompare(List<String> chunkIds);
}
