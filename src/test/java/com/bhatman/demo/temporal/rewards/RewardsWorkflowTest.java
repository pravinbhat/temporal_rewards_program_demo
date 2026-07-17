package com.bhatman.demo.temporal.rewards;

import com.bhatman.demo.temporal.rewards.activity.RewardsActivity;
import com.bhatman.demo.temporal.rewards.model.RewardLevel;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflow;
import com.bhatman.demo.temporal.rewards.workflow.RewardsWorkflowImpl;

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
 * Unit tests for {@link RewardsWorkflowImpl} using {@link TestWorkflowEnvironment}.
 * No running Temporal server required.
 */
class RewardsWorkflowTest {

    private static final RewardsActivity ACTIVITY_STUB = (customerId, points) -> { /* no-op */ };

    @RegisterExtension
    static final TestWorkflowExtension testWorkflow = TestWorkflowExtension.newBuilder()
            .registerWorkflowImplementationTypes(RewardsWorkflowImpl.class)
            .setActivityImplementations(ACTIVITY_STUB)
            .setUseTimeskipping(false)
            .build();

    // --- Helpers ---

    private static RewardsWorkflow newWorkflow(TestWorkflowEnvironment env, Worker worker, String workflowId) {
        return env.getWorkflowClient().newWorkflowStub(
                RewardsWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(worker.getTaskQueue())
                        .setWorkflowId(workflowId)
                        .build());
    }

    /** Re-attaches to an already-running workflow by workflowId (mirrors earn/leave/status commands). */
    private static RewardsWorkflow reattach(TestWorkflowEnvironment env, String workflowId) {
        return env.getWorkflowClient().newWorkflowStub(RewardsWorkflow.class, workflowId);
    }

    private static CompletableFuture<Void> enrolAsync(RewardsWorkflow workflow, String customerId) {
        return CompletableFuture.runAsync(() -> workflow.startRewardsProgram(customerId));
    }

    // --- Single-interaction tests ---

    @Test
    void enrolStartsAtBasicTier(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String id = "test-" + UUID.randomUUID();
        RewardsWorkflow workflow = newWorkflow(env, worker, id);
        var future = enrolAsync(workflow, "customer-basic");
        Thread.sleep(200);

        assertEquals(RewardLevel.BASIC, workflow.getCurrentLevel());
        assertEquals(0, workflow.getTotalPoints());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void earnPointsPromotesToGold(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String id = "test-" + UUID.randomUUID();
        RewardsWorkflow workflow = newWorkflow(env, worker, id);
        var future = enrolAsync(workflow, "customer-gold");
        Thread.sleep(200);

        workflow.earnPoints(500);
        assertEquals(RewardLevel.GOLD, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void earnPointsPromotesToPlatinum(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String id = "test-" + UUID.randomUUID();
        RewardsWorkflow workflow = newWorkflow(env, worker, id);
        var future = enrolAsync(workflow, "customer-platinum");
        Thread.sleep(200);

        workflow.earnPoints(1_000);
        assertEquals(RewardLevel.PLATINUM, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void partialPointsRemainBasic(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String id = "test-" + UUID.randomUUID();
        RewardsWorkflow workflow = newWorkflow(env, worker, id);
        var future = enrolAsync(workflow, "customer-partial");
        Thread.sleep(200);

        workflow.earnPoints(200);
        workflow.earnPoints(199); // 399 total — still Basic

        assertEquals(RewardLevel.BASIC, workflow.getCurrentLevel());

        workflow.leaveProgram();
        future.get();
    }

    @Test
    void leaveProgramCompletesWorkflow(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String id = "test-" + UUID.randomUUID();
        RewardsWorkflow workflow = newWorkflow(env, worker, id);
        var future = enrolAsync(workflow, "customer-leave");
        Thread.sleep(200);

        workflow.earnPoints(300);
        workflow.leaveProgram();
        future.get();
    }

    // --- Multi-execution tests (simulated separate JVM invocations) ---

    @Test
    void multiExecutionFullDemoFlow(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String workflowId = "demo-" + UUID.randomUUID();

        // Execution 1: join
        RewardsWorkflow joinStub = newWorkflow(env, worker, workflowId);
        var future = enrolAsync(joinStub, "customer-demo");
        Thread.sleep(200);
        assertEquals(RewardLevel.BASIC, joinStub.getCurrentLevel());
        assertEquals(0, joinStub.getTotalPoints());

        // Execution 2: earn 300 → Basic (300 total)
        RewardsWorkflow exec2 = reattach(env, workflowId);
        exec2.earnPoints(300);
        assertEquals(RewardLevel.BASIC, exec2.getCurrentLevel());
        assertEquals(300, exec2.getTotalPoints());

        // Execution 3: earn 250 → Gold (550 total)
        RewardsWorkflow exec3 = reattach(env, workflowId);
        exec3.earnPoints(250);
        assertEquals(RewardLevel.GOLD, exec3.getCurrentLevel());
        assertEquals(550, exec3.getTotalPoints());

        // Execution 4: earn 500 → Platinum (1050 total)
        RewardsWorkflow exec4 = reattach(env, workflowId);
        exec4.earnPoints(500);
        assertEquals(RewardLevel.PLATINUM, exec4.getCurrentLevel());
        assertEquals(1_050, exec4.getTotalPoints());

        // Execution 5: leave
        RewardsWorkflow exec5 = reattach(env, workflowId);
        exec5.leaveProgram();
        future.get();
    }

    @Test
    void statusQueryDoesNotMutateState(TestWorkflowEnvironment env, Worker worker) throws Exception {
        String workflowId = "status-" + UUID.randomUUID();

        RewardsWorkflow joinStub = newWorkflow(env, worker, workflowId);
        var future = enrolAsync(joinStub, "customer-status");
        Thread.sleep(200);

        joinStub.earnPoints(600); // → Gold

        RewardsWorkflow statusStub = reattach(env, workflowId);
        assertEquals(RewardLevel.GOLD, statusStub.getCurrentLevel());
        assertEquals(600, statusStub.getTotalPoints());

        // Re-query — state unchanged
        assertEquals(RewardLevel.GOLD, statusStub.getCurrentLevel());
        assertEquals(600, statusStub.getTotalPoints());

        statusStub.leaveProgram();
        future.get();
    }
}
