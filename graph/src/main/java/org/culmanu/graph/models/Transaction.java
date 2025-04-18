package org.culmanu.graph.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@Getter
@Setter
@RelationshipProperties
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;
    private double amount;
    private LocalDateTime timestamp;

    @TargetNode
    private Account target;
}
