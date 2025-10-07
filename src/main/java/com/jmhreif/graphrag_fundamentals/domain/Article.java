package com.jmhreif.graphrag_fundamentals.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.List;

public record Article(@Id String id,
                      String title,
                      String author,
                      String siteName,
                      ZonedDateTime date,
                      Double sentiment,
                      @Relationship("HAS_CHUNK") List<Chunk> chunks) {
}
