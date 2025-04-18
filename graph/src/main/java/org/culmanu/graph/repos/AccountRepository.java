package org.culmanu.graph.repos;


import org.culmanu.graph.models.Account;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends Neo4jRepository<Account, String> {
}
