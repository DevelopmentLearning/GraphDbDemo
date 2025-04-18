package org.culmanu.graph.services;

import org.culmanu.graph.models.Account;
import org.culmanu.graph.repos.AccountRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final Neo4jClient neo4jClient;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepository accountRepository, Neo4jClient neo4jClient) {
        this.accountRepository = accountRepository;
        this.neo4jClient = neo4jClient;
    }

    public void transferSequentially(String fromId, String toId, double amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();

        // Find the path of connected accounts between the source and destination account
        List<String> path = findPath(fromId, toId);

        // If no valid path exists, throw an error
        if (path.isEmpty()) {
            logger.error("Transaction denied: No valid path from {} to {}", fromId, toId);
            throw new IllegalStateException("Transaction denied: No valid path from " + fromId + " to " + toId);
        }

        Account currentAccount = from;

        // Traverse the path sequentially, transferring funds between each account
        for (int i = 0; i < path.size(); i++) {
            String nextAccountId = path.get(i);

            // Fetch the next account
            Account nextAccount = accountRepository.findById(nextAccountId).orElseThrow();

            // Check if the next account is blocked
            if (nextAccount.isBlocked()) {
                logger.error("Transaction stopped: Account {} is blocked.", nextAccountId);
                throw new IllegalStateException("Transaction denied: " + nextAccountId + " is blocked.");
            }

            // Ensure there is a valid connection from the current account to the next one, except for the last account in the path
            if (i < path.size() - 1) { // Only check connection for intermediary nodes
                if (!currentAccount.getId().equals(nextAccountId) && !isConnected(currentAccount.getId(), nextAccountId)) {
                    logger.error("Transaction denied: No valid connection from {} to {}.", currentAccount.getId(), nextAccountId);
                    throw new IllegalStateException("Transaction denied: No valid connection from " + currentAccount.getId() + " to " + nextAccountId);
                }
            }

            // Check for sufficient funds in the current account
            if (currentAccount.getBalance() < amount) {
                logger.error("Transaction denied: Insufficient funds in account {}", currentAccount.getId());
                throw new IllegalStateException("Transaction denied: Insufficient funds.");
            }

            // Perform the transaction: subtract from current account and add to the next account
            currentAccount.setBalance(currentAccount.getBalance() - amount);
            nextAccount.setBalance(nextAccount.getBalance() + amount);

            // Log the successful transaction
            logger.info("Transaction success: Transferred {} from {} to {}", amount, currentAccount.getId(), nextAccountId);

            // Save the updated balances
            accountRepository.save(currentAccount);
            accountRepository.save(nextAccount);

            // Move to the next account in the sequence
            currentAccount = nextAccount;
        }

        logger.info("Transaction completed successfully from {} to {}", fromId, toId);
    }

    private List<String> findPath(String fromId, String toId) {
        // Query to find a path between the source account (fromId) and destination account (toId)
        String query = """
        MATCH p = (start:Account {id: $fromId})-[*1..5]-(end:Account {id: $toId})
        UNWIND nodes(p) AS account
        RETURN DISTINCT account.id AS accountId
    """;

        // Execute the query to get a list of account IDs along the path
        List<String> accountIds = neo4jClient.query(query)
                .bind(fromId).to("fromId")
                .bind(toId).to("toId")
                .fetchAs(String.class)  // Fetch the 'id' of each account along the path
                .all().stream().toList();  // Collect the result into a list

        return accountIds;
    }



    private boolean isConnected(String fromId, String toId) {
        // Ensure there is a valid connection (direct or indirect) between accounts
        String pathQuery = """
            MATCH (a1:Account {id: $fromId})-[*1..4]->(a2:Account {id: $toId})
            RETURN COUNT(*) > 0 AS isConnected
        """;

        Boolean isConnected = neo4jClient.query(pathQuery)
                .bind(fromId).to("fromId")
                .bind(toId).to("toId")
                .fetchAs(Boolean.class)
                .one()
                .orElse(false);

        return isConnected;
    }
}


