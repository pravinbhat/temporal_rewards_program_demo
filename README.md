# Temporal Rewards Program Demo

A Java demonstration project showing how [Temporal](https://temporal.io) elegantly solves the hard
problems of **distributed, long-running, stateful services** — using a customer Rewards Program as
the driving use-case.

---

## Use-Case: Rewards Program

A rewards program tracks a customer's engagement points and automatically promotes them through
three tiers:

| Level        | Points Required          |
| --------------| --------------------------|
| **Basic**    | 0 (default on enrolment) |
| **Gold**     | ≥ 500                    |
| **Platinum** | ≥ 1 000                  |

### Business Rules

* A customer **joins** the program → they are placed in the **Basic** tier.
* Customers **earn points** through various activities (purchases, referrals, etc.).
* The tier is **recalculated automatically** whenever points change.
* A customer can **leave** the program at any time.
* A customer can **retrieve** their current tier.


---

## Tech Stack

| Component               | Version                                           |
| -------------------------| ---------------------------------------------------|
| Java                    | 25                                                |
| Maven                   | 3.9.x                                             |
| Temporal Java SDK       | 1.37.0                                            |
| Temporal Server (local) | temporal version 1.7.2 (Server 1.31.1, UI 2.49.1) |
| Logging                 | slf4j-nop 2.0.x                                   |

---

## Prerequisites

1. **Java 25** — [Download](https://jdk.java.net/25/)
2. **Maven 3.9.x** — [Download](https://maven.apache.org/download.cgi)
4. **Temporal CLI** — `brew install temporal`

---

## Running the Demo

### 1. Start the Temporal Dev Server

```bash
temporal server start-dev
```

The Web UI will be available at <http://localhost:8233>.

### 2. Build the Project

```bash
mvn clean package -q
```

### 3. Run the App

```bash
mvn compile exec:java -Dexec.mainClass="com.bhatman.demo.temporal.rewards.RewardsApp"
```

This enrols a customer, earns points in four increments (50 → +250 → +350 → +550), and queries the tier after each step — walking through all three tiers (Basic → Gold → Platinum).
