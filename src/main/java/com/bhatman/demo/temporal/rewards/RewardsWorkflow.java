package com.bhatman.demo.temporal.rewards;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Temporal workflow interface for the Rewards Program.
 *
 * <ul>
 * <li>{@link #enrol} — starts the workflow and places the customer in the Basic
 * tier.</li>
 * <li>{@link #earnPoints} — signal: adds points and recalculates the tier.</li>
 * <li>{@link #getTotalPoints} — query: returns the current totalPoints without
 * mutating state.</li>
 * <li>{@link #getCurrentTier} — query: returns the current tier without
 * mutating state.</li>
 * <li>{@link #leave} — signal: customer leaves the program, completing the
 * workflow.</li>
 * </ul>
 */
@WorkflowInterface
public interface RewardsWorkflow {

	@WorkflowMethod
	void enrol(String customerId);

	@SignalMethod
	void earnPoints(int points);

	@QueryMethod
	int getTotalPoints();

	@QueryMethod
	String getCurrentTier();

	@SignalMethod
	void leave();
}
