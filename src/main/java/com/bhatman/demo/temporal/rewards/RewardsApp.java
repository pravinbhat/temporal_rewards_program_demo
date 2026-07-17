package com.bhatman.demo.temporal.rewards;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Entry point for the Rewards Program demo.
 *
 * Starts a local Temporal worker and kicks off a sample rewards workflow that
 * enrols a customer, earns some points, and queries the current tier.
 *
 * Prerequisites: - Temporal dev server running:
 * {@code temporal server start-dev}
 */
public class RewardsApp {

	static final String TASK_QUEUE = "REWARDS_TASK_QUEUE";

	public static void main(String[] args) {

		// Connect to the locally running Temporal server
		WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
		WorkflowClient client = WorkflowClient.newInstance(service);

		// Start the worker (registers workflow + activities on the task queue)
		RewardsWorker worker = new RewardsWorker(client);
		worker.start();

		// Build a typed workflow stub for a new customer
		String customerId = "customer-002";
		RewardsWorkflow workflow = client.newWorkflowStub(RewardsWorkflow.class,
				WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId("rewards-" + customerId).build());

		// Enrol the customer (starts the workflow)
		WorkflowClient.start(workflow::enrol, customerId);

		// Earn some points via signals
		workflow.earnPoints(50);
		System.out.printf("Customer %s earned '%d' points.%n", customerId, 50);

		// Query the current tier
		String tier = workflow.getCurrentTier();
		int points = workflow.getTotalPoints();
		System.out.printf("Customer %s is currently in the '%s' tier with '%d' points.%n", customerId, tier, points);

		// Earn some points via signals
		workflow.earnPoints(250);
		System.out.printf("Customer %s earned '%d' points.%n", customerId, 250);

		// Query the current tier
		tier = workflow.getCurrentTier();
		points = workflow.getTotalPoints();
		System.out.printf("Customer %s is currently in the '%s' tier with '%d' points.%n", customerId, tier, points);

		// Earn some points via signals
		workflow.earnPoints(350);
		System.out.printf("Customer %s earned '%d' points.%n", customerId, 350);

		// Query the current tier
		tier = workflow.getCurrentTier();
		points = workflow.getTotalPoints();
		System.out.printf("Customer %s is currently in the '%s' tier with '%d' points.%n", customerId, tier, points);

		// Earn some points via signals
		workflow.earnPoints(550);
		System.out.printf("Customer %s earned '%d' points.%n", customerId, 550);

		// Query the current tier
		tier = workflow.getCurrentTier();
		points = workflow.getTotalPoints();
		System.out.printf("Customer %s is currently in the '%s' tier with '%d' points.%n", customerId, tier, points);

		// Leave the program (terminates the workflow gracefully)
		workflow.leave();

		worker.stop();
	}
}
