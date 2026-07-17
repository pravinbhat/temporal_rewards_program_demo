package com.bhatman.demo.temporal.rewards.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link RewardsActivity}.
 *
 * <p>In a real application this would write to a database or call an external
 * service. Here it simply logs the transaction so the demo stays self-contained.
 */
public class RewardsActivityImpl implements RewardsActivity {

    private static final Logger log = LoggerFactory.getLogger(RewardsActivityImpl.class);

    @Override
    public void recordPointsTransaction(String customerId, int points) {
        log.info("Recording points transaction: customer={}, points={}", customerId, points);
        // TODO: persist to database / emit event
    }
}
