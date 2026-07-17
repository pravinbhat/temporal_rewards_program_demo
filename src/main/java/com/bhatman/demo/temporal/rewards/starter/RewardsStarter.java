package com.bhatman.demo.temporal.rewards.starter;

import com.bhatman.demo.temporal.rewards.worker.RewardsWorker;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Demo client. Each JVM run sends exactly one command to a long-running customer workflow,
 * then exits. State is preserved in Temporal between runs — no external database needed.
 *
 * <p>Usage: {@code RewardsStarter <customerId> <join|earn <points>|leave|status>}
 */
public class RewardsStarter {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: RewardsStarter <customerId> <join|earn <points>|leave|status>");
            System.exit(1);
        }

        String customerId = args[0];
        String command    = args[1];
        String workflowId = "rewards-" + customerId;

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        switch (command) {
            case "join" -> {
                RewardsWorkflow workflow = client.newWorkflowStub(
                        RewardsWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(RewardsWorker.TASK_QUEUE)
                                .setWorkflowId(workflowId)
                                .build());
                WorkflowClient.start(workflow::startRewardsProgram, customerId);
                System.out.printf("[JOIN]   Customer %-20s enrolled.%n", customerId);
                printState(customerId, workflow);
            }
            case "earn" -> {
                if (args.length < 3) {
                    System.err.println("Usage: RewardsStarter <customerId> earn <points>");
                    System.exit(1);
                }
                int points = Integer.parseInt(args[2]);
                RewardsWorkflow workflow = client.newWorkflowStub(RewardsWorkflow.class, workflowId);
                workflow.earnPoints(points);
                System.out.printf("[EARN]   Customer %-20s earned %d points.%n", customerId, points);
                printState(customerId, workflow);
            }
            case "leave" -> {
                RewardsWorkflow workflow = client.newWorkflowStub(RewardsWorkflow.class, workflowId);
                workflow.leaveProgram();
                System.out.printf("[LEAVE]  Customer %-20s has left the rewards program.%n", customerId);
            }
            case "status" -> {
                RewardsWorkflow workflow = client.newWorkflowStub(RewardsWorkflow.class, workflowId);
                System.out.printf("[STATUS] Customer %-20s %n", customerId);
                printState(customerId, workflow);
            }
            default -> {
                System.err.printf("Unknown command '%s'. Valid: join | earn <points> | leave | status%n", command);
                System.exit(1);
            }
        }
    }

    private static void printState(String customerId, RewardsWorkflow workflow) {
        System.out.printf("         %-24s level=%-10s totalPoints=%d%n",
                customerId, workflow.getCurrentLevel(), workflow.getTotalPoints());
    }
}
