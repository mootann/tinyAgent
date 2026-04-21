package com.tinyflow.graph;

import com.tinyflow.config.TinyFlowProperties;
import com.tinyflow.graph.node.*;
import com.tinyflow.memory.MemoryEngine;
import com.tinyflow.middleware.*;
import com.tinyflow.model.AgentGraphState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Builder for constructing the LangGraph4J StateGraph.
 * Mirrors the Python implementation in tiny-flow backend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangGraphBuilder {

    private final RouterNode routerNode;
    private final RespondNode respondNode;
    private final ThinkRespondNode thinkRespondNode;
    private final PlanNode planNode;
    private final DispatchNode dispatchNode;
    private final SkillNode skillNode;
    private final ExecuteNode executeNode;
    private final ReflectorNode reflectorNode;
    private final MergeNode mergeNode;

    private final TodoMiddleware todoMiddleware;
    private final LoopDetectionMiddleware loopDetectionMiddleware;
    private final ContextCompactionMiddleware contextCompactionMiddleware;

    private final MemoryEngine memoryEngine;
    private final TinyFlowProperties properties;

    /**
     * Build and compile the agent graph with 4-way routing.
     */
    public CompiledGraph<AgentGraphState> build() throws GraphStateException {
        log.info("Building LangGraph4J StateGraph...");

        int maxIterations = properties.getGraph().getMaxIterations();

        // Create middleware chains for each mode
        MiddlewareChain flashChain = new MiddlewareChain(List.of());
        MiddlewareChain thinkingChain = new MiddlewareChain(List.of(contextCompactionMiddleware));
        MiddlewareChain proChain = new MiddlewareChain(List.of(todoMiddleware, contextCompactionMiddleware, loopDetectionMiddleware));
        MiddlewareChain ultraChain = new MiddlewareChain(List.of(todoMiddleware, contextCompactionMiddleware, loopDetectionMiddleware));

        // Build the graph
        var stateGraph = new StateGraph<>(AgentGraphState.SCHEMA, AgentGraphState::new)
                // Add nodes with middleware wrappers
                .addNode("router", wrapWithMemory(routerNode))
                .addNode("respond", node_async(respondNode::execute))
                .addNode("think_respond", wrapWithMiddleware(thinkRespondNode, thinkingChain))
                .addNode("plan", wrapWithMiddleware(planNode, proChain))
                .addNode("dispatch", node_async(dispatchNode::execute))
                .addNode("skill_node", node_async(skillNode::execute))
                .addNode("execute", wrapWithMiddleware(executeNode, proChain))
                .addNode("reflector", wrapWithMiddleware(reflectorNode, proChain))
                .addNode("merge", node_async(mergeNode::execute))

                // Set entry point
                .addEdge(START, "router")

                // Router → direct routing based on mode
                .addConditionalEdges("router", (AsyncEdgeAction<AgentGraphState>) state -> {
                    String mode = state.executionMode();
                    if ("flash".equals(mode)) {
                        return CompletableFuture.completedFuture("respond");
                    } else if ("thinking".equals(mode)) {
                        return CompletableFuture.completedFuture("think_respond");
                    } else if ("pro".equals(mode) || "ultra".equals(mode)) {
                        return CompletableFuture.completedFuture("plan");
                    }
                    return CompletableFuture.completedFuture("respond");
                }, Map.of(
                        "respond", "respond",
                        "think_respond", "think_respond",
                        "plan", "plan"
                ))

                // Flash / Thinking → END
                .addEdge("respond", END)
                .addEdge("think_respond", END)

                // Plan → conditional: skill match or dispatch
                .addConditionalEdges("plan", (AsyncEdgeAction<AgentGraphState>) state -> {
                    if ("ultra".equals(state.executionMode())) {
                        return CompletableFuture.completedFuture("dispatch");
                    }
                    return CompletableFuture.completedFuture("skill_node");
                }, Map.of(
                        "skill_node", "skill_node",
                        "dispatch", "dispatch"
                ))

                // Skill / Dispatch → Execute
                .addEdge("skill_node", "execute")
                .addEdge("dispatch", "execute")

                // Execute → Reflector (pro) or Merge (ultra)
                .addConditionalEdges("execute", (AsyncEdgeAction<AgentGraphState>) state -> {
                    if ("ultra".equals(state.executionMode())) {
                        return CompletableFuture.completedFuture("merge");
                    }
                    return CompletableFuture.completedFuture("reflector");
                }, Map.of(
                        "reflector", "reflector",
                        "merge", "merge"
                ))

                // Merge → Reflector
                .addEdge("merge", "reflector")

                // Reflector → END or loop back to execute
                .addConditionalEdges("reflector", (AsyncEdgeAction<AgentGraphState>) state -> {
                    String route = state.route();
                    if ("continue_execute".equals(route)) {
                        return CompletableFuture.completedFuture("execute");
                    }
                    return CompletableFuture.completedFuture("end");
                }, Map.of(
                        "end", END,
                        "execute", "execute"
                ));

        // Compile without checkpoint
        var compiledGraph = stateGraph.compile();

        log.info("LangGraph4J StateGraph built successfully");
        return compiledGraph;
    }

    /**
     * Wrap node with memory injection.
     */
    private AsyncNodeAction<AgentGraphState> wrapWithMemory(GraphNode node) {
        return state -> {
            // Inject memory before routing
            String memoryContext = memoryEngine.inject();
            state = state.withMemoryContext(memoryContext);
            return CompletableFuture.completedFuture(node.execute(state));
        };
    }

    /**
     * Wrap node with middleware chain.
     */
    private AsyncNodeAction<AgentGraphState> wrapWithMiddleware(GraphNode node, MiddlewareChain chain) {
        return state -> CompletableFuture.completedFuture(chain.runNode(state, node::execute));
    }

}
