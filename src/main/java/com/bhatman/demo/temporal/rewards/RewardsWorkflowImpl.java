package com.bhatman.demo.temporal.rewards;

import org.slf4j.Logger;

import io.temporal.workflow.Workflow;

/**
 * Temporal workflow implementation for the Rewards Program.
 *
 * Tier thresholds:
 * <ul>
 * <li>Basic — 0 points (default on enrolment)</li>
 * <li>Gold — ≥ 500 points</li>
 * <li>Platinum — ≥ 1 000 points</li>
 * </ul>
 */
public class RewardsWorkflowImpl implements RewardsWorkflow {

	private static final Logger log = Workflow.getLogger(RewardsWorkflowImpl.class);

	private static final int GOLD_THRESHOLD = 500;
	private static final int PLATINUM_THRESHOLD = 1_000;

	private int totalPoints = 0;
	private String tier = "Basic";
	private boolean active = true;

	@Override
	public void enrol(String customerId) {
		log.info("Customer {} enrolled — starting at '{}' tier.", customerId, tier);

		// Keep the workflow alive until the customer leaves
		Workflow.await(() -> !active);

		log.info("Customer {} has left the rewards program.", customerId);
	}

	@Override
	public void earnPoints(int points) {
		totalPoints += points;
		recalculateTier();
		log.info("Earned {} points. Total: {}. Tier: {}.", points, totalPoints, tier);
	}

	@Override
	public int getTotalPoints() {
		return totalPoints;
	}

	@Override
	public String getCurrentTier() {
		return tier;
	}

	@Override
	public void leave() {
		active = false;
	}

	private void recalculateTier() {
		if (totalPoints >= PLATINUM_THRESHOLD) {
			tier = "Platinum";
		} else if (totalPoints >= GOLD_THRESHOLD) {
			tier = "Gold";
		} else {
			tier = "Basic";
		}
	}
}
