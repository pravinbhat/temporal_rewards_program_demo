package com.bhatman.demo.temporal.rewards.worker;

import com.bhatman.demo.temporal.rewards.activity.RewardsActivityImpl;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflowImpl;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Registers the {@link RewardsWorkflowImpl} and {@link RewardsActivityImpl}
 * with Temporal and manages the lifecycle of the underlying {@link WorkerFactory}.
 *
 * <p>Prerequisites: Temporal dev server must be running:
 * {@code temporal server start-dev}
 */
public class RewardsWorker {

    /** Task queue shared between the worker and all clients. */
    public static final String TASK_QUEUE = "REWARDS_TASK_QUEUE";

    private final WorkerFactory factory;

    public RewardsWorker(WorkflowClient client) {
        factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(RewardsWorkflowImpl.class);
        worker.registerActivitiesImplementations(new RewardsActivityImpl());
    }

    public void start() {
        factory.start();
    }

    public void stop() {
        factory.shutdown();
    }

    /**
     * Standalone entry-point: starts the worker and keeps it running until
     * the process is interrupted (Ctrl-C / SIGTERM).
     */
    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        RewardsWorker rewardsWorker = new RewardsWorker(client);
        rewardsWorker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(rewardsWorker::stop));
        System.out.println("Rewards worker started. Listening on task queue: " + TASK_QUEUE);
    }
}
