package com.bhatman.demo.temporal.rewards.workflow;

import com.bhatman.demo.temporal.rewards.model.RewardLevel;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/** Temporal workflow interface for the Rewards Program. */
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
