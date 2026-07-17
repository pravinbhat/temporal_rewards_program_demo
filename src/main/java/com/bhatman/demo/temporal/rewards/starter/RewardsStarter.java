package com.bhatman.demo.temporal.rewards.starter;

import com.bhatman.demo.temporal.rewards.worker.RewardsWorker;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/** Demo client — enrols a customer, earns points, queries level, then leaves. */
public class RewardsStarter {

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        String customerId = (args.length > 0) ? args[0] : "BhatMan-Returns-007";
        RewardsWorkflow workflow = client.newWorkflowStub(
                RewardsWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(RewardsWorker.TASK_QUEUE)
                        .setWorkflowId("rewards-" + customerId)
                        .build());

        WorkflowClient.start(workflow::startRewardsProgram, customerId);

        workflow.earnPoints(300);
        printState(customerId, workflow);

        workflow.earnPoints(250);
        printState(customerId, workflow);

        workflow.earnPoints(500);
        printState(customerId, workflow);

        workflow.leaveProgram();
        System.out.printf("Customer %s has left the rewards program.%n", customerId);
    }

    private static void printState(String customerId, RewardsWorkflow workflow) {
        System.out.printf("Customer %s → level=%-8s  totalPoints=%d%n",
                customerId, workflow.getCurrentLevel(), workflow.getTotalPoints());
    }
}
