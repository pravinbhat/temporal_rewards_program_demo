package com.bhatman.demo.temporal.rewards.workflow;

import com.bhatman.demo.temporal.rewards.model.RewardLevel;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Temporal workflow interface for the Rewards Program.
 *
 * <p>One workflow instance runs per customer for the lifetime of their membership.
 * Signals mutate state; queries read it without side effects.
 */
@WorkflowInterface
public interface RewardsWorkflow {

    /** Enrols {@code customerId} in the program and blocks until they leave. */
    @WorkflowMethod
    void startRewardsProgram(String customerId);

    /** Credits {@code points} to the customer and recalculates their tier. */
    @SignalMethod
    void earnPoints(int points);

    /** Returns the customer's accumulated points (read-only). */
    @QueryMethod
    int getTotalPoints();

    /** Returns the customer's current reward tier (read-only). */
    @QueryMethod
    RewardLevel getCurrentLevel();

    /** Signals the customer's intent to leave; the workflow completes cleanly. */
    @SignalMethod
    void leaveProgram();
}
