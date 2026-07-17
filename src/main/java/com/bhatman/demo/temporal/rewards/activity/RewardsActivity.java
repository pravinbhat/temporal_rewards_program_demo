package com.bhatman.demo.temporal.rewards.activity;

import io.temporal.activity.ActivityInterface;

/**
 * Temporal activity interface for the Rewards Program.
 *
 * <p>Activities encapsulate side-effectful or unreliable work (database writes,
 * HTTP calls, emails). Temporal retries them automatically on failure without
 * re-executing the workflow logic.
 */
@ActivityInterface
public interface RewardsActivity {

    /**
     * Persists a points transaction for the given customer.
     *
     * @param customerId the unique customer identifier
     * @param points     the number of points earned in this transaction
     */
    void recordPointsTransaction(String customerId, int points);
}
