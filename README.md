# GraphDbDemo

https://hub.docker.com/_/neo4j

```yml
version: '3.8'

services:
  neo4j:
    image: neo4j:latest
    container_name: neo4j
    ports:
      - "7474:7474"   # HTTP browser
      - "7687:7687"   # Bolt protocol
    environment:
      - NEO4J_AUTH=neo4j/password123
      - NEO4J_dbms_memory_pagecache_size=1G
      - NEO4J_dbms_memory_heap_initial__size=1G
      - NEO4J_dbms_memory_heap_max__size=2G
    volumes:
      - neo4j_data:/data
      - neo4j_logs:/logs
    restart: unless-stopped

volumes:
  neo4j_data:
  neo4j_logs:

```

## Cypher Query Language

* Creating a Single Node

```
CREATE (p:Person {name: "Alice", age: 30});
```

Creates a node with the label Person and two properties: name and age.

* Creating Multiple Nodes and Relationships

```
CREATE (a:Person {name: "Alice"})-[:KNOWS]->(b:Person {name: "Bob"});
```

This creates two Person nodes and a KNOWS relationship from Alice to Bob.

* Complex create

```cypher
CREATE
  (a:Person {name: "Alice"}),
  (b:Person {name: "Bob"}),
  (c:Company {name: "Acme Inc."}),
  (a)-[:KNOWS]->(b),
  (a)-[:WORKS_AT]->(c),
  (b)-[:WORKS_AT]->(c);
```

-----------------------------------------

* Match All Nodes

 ````
  MATCH (n) RETURN n;
````

Retrieves all nodes in the graph. Primarily used for exploration and debugging.

* Match by Label and Property

````
MATCH (p:Person {name: "Alice"}) RETURN p;
````

Efficient for direct retrieval of nodes, especially when indexed by a frequently queried property such as name.

* Match with a Relationship

```cypher
MATCH (a:Person)-[:KNOWS]->(b:Person) RETURN a.name, b.name;
```

Fetches nodes that are connected through a specific relationship type, in this case, `KNOWS`.

* Chained Relationships

```cypher
MATCH (p:Person)-[:WORKS_AT]->(c:Company)<-[:WORKS_AT]-(colleague)
WHERE p.name = "Alice"
RETURN colleague.name;
```


Finds other persons who work at the same company as Alice.

---



* Updating an Existing Property

```cypher
MATCH (p:Person {name: "Alice"}) SET p.age = 31;
```

Modifies the `age` property of the specified node.

* Update all 

```cypher
MATCH (n) SET n.age = 31;
```

* Adding Multiple Properties

```cypher
MATCH (p:Person {name: "Alice"}) SET p += {city: "Paris", job: "Engineer"};
```


Adds or updates multiple properties on a node in a single operation.

* Adding Labels

```cypher
MATCH (p:Person {name: "Alice"}) SET p:Employee;
```


Adds a new label to an existing node. Labels can be used for categorization and filtering.

---

* Delete Node without Relationships

```cypher
MATCH (p:Person {name: "Alice"}) DELETE p;
```


Deletes a node only if it has no relationships attached. Will result in an error if it does.

* Delete Node with Relationships

```cypher
MATCH (p:Person {name: "Alice"}) DETACH DELETE p;
```

Forcibly removes a node and all of its connected relationships.

* Delete all nodes

```cypher
MATCH (n) DETACH DELETE n;
```

MATCH (n) — Matches all nodes in the graph.

DETACH DELETE n — Deletes all matched nodes and any relationships connected to them.

If you try DELETE n without DETACH, Neo4j will raise an error if the node has any relationships.

---

* Ensuring Node Uniqueness

```cypher
MERGE (p:Person {name: "Alice"});
```


Finds a node with the given properties; if none exists, creates it. Useful for idempotent operations.

* Creating Unique Relationships

```cypher
MERGE (a:Person {name: "Alice"})
MERGE (b:Person {name: "Bob"})
MERGE (a)-[:FRIENDS_WITH]->(b);
```


Prevents duplication of both nodes and their relationships.

---


* Count Nodes

```cypher
MATCH (p:Person) RETURN COUNT(p);
```

* Statistical Functions

```cypher
MATCH (p:Person) RETURN AVG(p.age), MAX(p.age), MIN(p.age);
```

* Grouping by Property

```cypher
MATCH (p:Person)
RETURN p.city, COUNT(*) AS population
ORDER BY population DESC;
```


Summarizes the dataset, such as counting the number of people per city.

---


```cypher
MATCH (p:Person) WHERE p.age > 30 RETURN p;
MATCH (p:Person) WHERE p.name STARTS WITH "A" RETURN p;
MATCH (p:Person) WHERE p.name =~ "A.*" RETURN p;
```


The `WHERE` clause refines the results of a `MATCH`. It supports comparison, pattern matching, and regular expressions.

---


```cypher
MATCH path = (a:Person)-[:KNOWS*1..3]->(b:Person)
RETURN path;
```

This query retrieves paths between people (:Person nodes) connected through the KNOWS relationship. 
It specifically looks for paths of 1 to 3 relationships in length, directed from node a to node b.


Searches for paths of variable length between nodes, which is valuable in social network analysis, recommendation
systems, or fraud detection.

---


* Creating an Index

```cypher
CREATE INDEX FOR (p:Person) ON (p.name);
```


Improves the performance of queries that filter or sort by `name`.

* Enforcing Uniqueness

```cypher
show indexes;

drop index index_4b2e9408;

CREATE CONSTRAINT FOR (p:Person) REQUIRE p.name IS UNIQUE;
```


Prevents the creation of nodes with duplicate email addresses. Critical for data integrity.

---

## Managing Databases (Enterprise Edition)

* Create a New Database

```cypher
CREATE DATABASE myNewDb;
```

* Switch Database Context

```cypher
:use myNewDb
```

* Delete a Database

```cypher
DROP DATABASE myNewDb;
```

**Note:**  
Database management commands require administrative privileges and are only available in the Enterprise edition.

---


## Best Practices

| Practice                        | Reason                                                     |
|---------------------------------|------------------------------------------------------------|
| Use `MERGE` over `CREATE`       | Avoids duplicate entries and ensures data consistency      |
| Apply constraints and indexes   | Enhances performance and enforces data integrity           |
| Avoid deep traversal by default | Unbounded queries can be computationally expensive         |
| Use parameters in applications  | Improves security and query reusability                    |
| Profile and debug queries       | Use `PROFILE` or `EXPLAIN` to understand query performance |

---




