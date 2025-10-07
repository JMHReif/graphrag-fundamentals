package com.jmhreif.graphrag_fundamentals.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.Map;

public record Organization(@Id String id,
                           String name,
                           @Relationship(value = "MENTIONS", direction = Relationship.Direction.INCOMING) List<Article> articles,
                           @Relationship("HAS_CATEGORY") List<IndustryCategory> industries) {
}
