package com.bhatman.demo.temporal.rewards.workflow;

import com.bhatman.demo.temporal.rewards.activity.RewardsActivity;
import com.bhatman.demo.temporal.rewards.model.RewardLevel;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Entity Workflow — one long-running instance per customer.
 * State is durable via Temporal's Event History; no external database required.
 */
public class RewardsWorkflowImpl implements RewardsWorkflow {

    private static final Logger log = Workflow.getLogger(RewardsWorkflowImpl.class);

    /** Activity stub with automatic retries. */
    private final RewardsActivity activity = Workflow.newActivityStub(
            RewardsActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    private String customerId;
    private int totalPoints = 0;
    private RewardLevel currentLevel = RewardLevel.BASIC;
    private boolean programEnded = false;

    @Override
    public void startRewardsProgram(String customerId) {
        this.customerId   = customerId;
        this.currentLevel = RewardLevel.BASIC;
        this.totalPoints  = 0;

        log.info("Customer {} enrolled — starting at '{}' tier.", customerId, currentLevel);
        Workflow.await(() -> programEnded);
        log.info("Customer {} has left the rewards program.", customerId);
    }

    @Override
    public void earnPoints(int points) {
        totalPoints += points;
        currentLevel = RewardLevel.forPoints(totalPoints);
        log.info("Earned {} points. Total: {}. Level: {}.", points, totalPoints, currentLevel);
        activity.recordPointsTransaction(customerId, points);
    }

    @Override
    public int getTotalPoints() {
        return totalPoints;
    }

    @Override
    public RewardLevel getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public void leaveProgram() {
        programEnded = true;
    }
}
