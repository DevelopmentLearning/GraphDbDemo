package org.culmanu.graph.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Getter
@Setter
public class Account {
    @Id
    private String id;
    private double balance;
    private boolean blocked;
}
