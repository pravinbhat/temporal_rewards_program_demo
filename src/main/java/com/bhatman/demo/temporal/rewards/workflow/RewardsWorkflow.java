package com.bhatman.demo.temporal.rewards.workflow;

import com.bhatman.demo.temporal.rewards.model.RewardLevel;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Temporal workflow interface for the Rewards Program.
 *
 * <ul>
 *   <li>{@link #startRewardsProgram} — starts the workflow and places the
 *       customer in the {@link RewardLevel#BASIC} tier.</li>
 *   <li>{@link #earnPoints}          — signal: adds points and recalculates the tier.</li>
 *   <li>{@link #getTotalPoints}      — query: returns the current accumulated points.</li>
 *   <li>{@link #getCurrentLevel}     — query: returns the current {@link RewardLevel}.</li>
 *   <li>{@link #leaveProgram}        — signal: customer leaves, completing the workflow.</li>
 * </ul>
 */
@WorkflowInterface
public interface RewardsWorkflow {

    @WorkflowMethod
    void startRewardsProgram(String customerId);

    @SignalMethod
    void earnPoints(int points);

    @QueryMethod
    int getTotalPoints();

    @QueryMethod
    RewardLevel getCurrentLevel();

    @SignalMethod
    void leaveProgram();
}
