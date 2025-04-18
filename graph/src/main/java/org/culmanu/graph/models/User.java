package org.culmanu.graph.models;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Setter
@Getter
public class User {
    @Id
    private String id;
    private String name;
    private boolean blocked;
}