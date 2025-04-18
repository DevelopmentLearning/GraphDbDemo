package org.culmanu.graph.seeders;

import jakarta.annotation.PostConstruct;
import org.culmanu.graph.models.Account;
import org.culmanu.graph.models.User;
import org.culmanu.graph.repos.AccountRepository;
import org.culmanu.graph.repos.UserRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final Neo4jClient neo4jClient;

    public DataSeeder(UserRepository userRepository, AccountRepository accountRepository, Neo4jClient neo4jClient) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.neo4jClient = neo4jClient;
    }

//    @PostConstruct
    public void seedData() {
        for (int i = 1; i <= 5; i++) {
            String userId = "user-" + i;
            String accountId = "acc-" + i;

            User user = new User();
            user.setId(userId);
            user.setName("User " + i);
            user.setBlocked(false);
            userRepository.save(user);

            Account account = new Account();
            account.setId(accountId);
            account.setBalance(1000.0 * i);
            account.setBlocked(i == 4); // Mark account 5 as blocked (fraud)
            accountRepository.save(account);

            // Create OWNS relationship in Neo4j
            neo4jClient.query("MATCH (u:User {id: $uid}), (a:Account {id: $aid}) CREATE (u)-[:OWNS]->(a)")
                    .bind(userId).to("uid")
                    .bind(accountId).to("aid")
                    .run();
        }

        // Link accounts with transactions to simulate a connected graph
        createTransaction("acc-1", "acc-2", 150.0);
        createTransaction("acc-2", "acc-3", 200.0);
        createTransaction("acc-3", "acc-4", 250.0);
        createTransaction("acc-4", "acc-5", 300.0); // Connect to fraudulent account
    }

    private void createTransaction(String fromId, String toId, Double amount) {
        neo4jClient.query("""
                            MATCH (a1:Account {id: $fromId}), (a2:Account {id: $toId})
                            CREATE (a1)-[:TRANSFERRED {
                                amount: $amount,
                                timestamp: datetime($timestamp)
                            }]->(a2)
                        """)
                .bind(fromId).to("fromId")
                .bind(toId).to("toId")
                .bind(amount).to("amount")
                .bind(LocalDateTime.now().toString()).to("timestamp")
                .run();
    }
}
