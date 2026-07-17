package com.bhatman.demo.temporal.rewards;

import com.bhatman.demo.temporal.rewards.activity.RewardsActivity;
import com.bhatman.demo.temporal.rewards.model.RewardLevel;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflow;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflowImpl;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link RewardsWorkflowImpl}.
 *
 * <p>Uses {@link TestWorkflowEnvironment} — no running Temporal server required.
 * Signals, queries, and tier promotions are verified without any network I/O.
 *
 * <p>Pattern: each test creates a fresh typed workflow stub via
 * {@link WorkflowClient#newWorkflowStub}, starts the {@code @WorkflowMethod} on a
 * background thread (so it can block on {@code Workflow.await}), then freely sends
 * signals and runs queries from the test thread.
 */
class RewardsWorkflowTest {

    /** No-op activity — avoids real side-effects in tests. */
    private static final RewardsActivity ACTIVITY_STUB = (customerId, points) -> { /* no-op */ };

    @RegisterExtension
    static final TestWorkflowExtension testWorkflow = TestWorkflowExtension.newBuilder()
            .registerWorkflowImplementationTypes(RewardsWorkflowImpl.class)
            .setActivityImplementations(ACTIVITY_STUB)
            .setUseTimeskipping(false)
            .build();

    // -----------------------------------------------------------------------
    // Helper — creates a new stub and starts startRewardsProgram in the background
    // -----------------------------------------------------------------------
    private static RewardsWorkflow newWorkflow(TestWorkflowEnvironment env, Worker worker) {
        return env.getWorkflowClient().newWorkflowStub(
                RewardsWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(worker.getTaskQueue())
                        .setWorkflowId("test-" + UUID.randomUUID())
                        .build());
    }

    private static CompletableFuture<Void> enrolAsync(RewardsWorkflow workflow) {
        return CompletableFuture.runAsync(() -> workflow.startRewardsProgram("test-customer"));
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void enrolStartsAtBasicTier(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        assertEquals(RewardLevel.BASIC, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void earnPointsPromotesToGold(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        workflow.earnPoints(500);

        assertEquals(RewardLevel.GOLD, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void earnPointsPromotesToPlatinum(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        workflow.earnPoints(1_000);

        assertEquals(RewardLevel.PLATINUM, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void partialPointsRemainBasic(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        workflow.earnPoints(200);
        workflow.earnPoints(199); // 399 total — still Basic

        assertEquals(RewardLevel.BASIC, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void multiStepPromotionFlow(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        workflow.earnPoints(300);
        assertEquals(RewardLevel.BASIC, workflow.getCurrentLevel());
        assertEquals(300, workflow.getTotalPoints());

        workflow.earnPoints(250); // 550 total
        assertEquals(RewardLevel.GOLD, workflow.getCurrentLevel());
        assertEquals(550, workflow.getTotalPoints());

        workflow.earnPoints(500); // 1 050 total
        assertEquals(RewardLevel.PLATINUM, workflow.getCurrentLevel());
        assertEquals(1_050, workflow.getTotalPoints());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void leaveProgramCompletesWorkflow(TestWorkflowEnvironment env, Worker worker) throws Exception {
        RewardsWorkflow workflow = newWorkflow(env, worker);
        var future = enrolAsync(workflow);
        Thread.sleep(200);

        workflow.earnPoints(300);
        workflow.leaveProgram();

        // Workflow should complete cleanly — future.get() would throw if it didn't
        future.get();
    }
}
