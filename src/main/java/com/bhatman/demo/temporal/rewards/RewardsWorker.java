package com.bhatman.demo.temporal.rewards;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Registers the {@link RewardsWorkflowImpl} with Temporal and manages the
 * lifecycle of the underlying {@link WorkerFactory}.
 */
public class RewardsWorker {

	private final WorkerFactory factory;

	public RewardsWorker(WorkflowClient client) {
		factory = WorkerFactory.newInstance(client);
		Worker worker = factory.newWorker(RewardsApp.TASK_QUEUE);
		worker.registerWorkflowImplementationTypes(RewardsWorkflowImpl.class);
	}

	public void start() {
		factory.start();
	}

	public void stop() {
		factory.shutdown();
	}
}
