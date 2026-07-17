package com.bhatman.demo.temporal.rewards.starter;

import com.bhatman.demo.temporal.rewards.worker.RewardsWorker;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Demo runner for the Rewards Program workflow.
 *
 * <p>This class starts an embedded worker, enrols a customer, sends several
 * point-earning signals, queries the current level between signals, and finally
 * signals the customer to leave the program.
 *
 * <p>Prerequisites: Temporal dev server must be running:
 * {@code temporal server start-dev}
 */
public class RewardsStarter {

    public static void main(String[] args) throws InterruptedException {

        // Connect to the locally running Temporal server
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Start the worker (registers workflow + activity on the task queue)
        RewardsWorker worker = new RewardsWorker(client);
        worker.start();

        // Build a typed workflow stub for a new customer
        String customerId = "customer-002";
        RewardsWorkflow workflow = client.newWorkflowStub(
                RewardsWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(RewardsWorker.TASK_QUEUE)
                        .setWorkflowId("rewards-" + customerId)
                        .build());

        // 1. Enrol the customer (starts the workflow — Basic tier)
        WorkflowClient.start(workflow::startRewardsProgram, customerId);

        // 2. Signal 300 points earned → still Basic
        workflow.earnPoints(300);
        printState(customerId, workflow);

        // 3. Signal 250 more points → promoted to Gold (550 total)
        workflow.earnPoints(250);
        printState(customerId, workflow);

        // 4. Signal 500 more points → promoted to Platinum (1 050 total)
        workflow.earnPoints(500);
        printState(customerId, workflow);

        // 5. Customer leaves the program (workflow completes gracefully)
        workflow.leaveProgram();
        System.out.printf("Customer %s has left the rewards program.%n", customerId);

        worker.stop();
    }

    private static void printState(String customerId, RewardsWorkflow workflow) {
        System.out.printf("Customer %s → level=%-8s  totalPoints=%d%n",
                customerId, workflow.getCurrentLevel(), workflow.getTotalPoints());
    }
}
