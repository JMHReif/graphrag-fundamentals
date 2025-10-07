package com.jmhreif.graphrag_fundamentals.domain;

import org.springframework.data.neo4j.core.schema.Id;

public record IndustryCategory(@Id String id,
                               String name) {
}
