package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import com.tinyflow.service.EventEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Router node - decides execution mode (flash/thinking/pro/ultra).
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouterNode implements GraphNode {

    private final ChatClient chatClient;

    private static final String ROUTER_PROMPT = """
            分析用户请求，选择最合适的执行模式：
            
            可选模式：
            - flash: 简单问题，直接回答（如问候、简单事实查询）
            - thinking: 需要深度推理的问题（如数学、逻辑分析）
            - pro: 需要规划执行的任务（如多步骤任务、代码生成）
            - ultra: 需要并行研究的复杂任务（如研究报告、综合分析）
            
            用户请求: %s
            
            只回复模式名称（flash/thinking/pro/ultra），不要解释。""";

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String lastMessage = getLastUserMessage(state);
        if (lastMessage == null) {
            return Map.of(
                    "executionMode", "flash",
                    "route", "respond"
            );
        }

        String prompt = String.format(ROUTER_PROMPT, lastMessage);

        try {
            String mode = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .toLowerCase();

            // Validate mode
            mode = switch (mode) {
                case "thinking" -> "thinking";
                case "pro" -> "pro";
                case "ultra" -> "ultra";
                default -> "flash";
            };

            log.info("Router selected mode: {} for thread: {}", mode, 
                    state.metadata().get("thread_id").toString());

            // Emit mode_selected event
            EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
            if (emitter != null) {
                emitter.emitModeSelected(mode, null);
            }

            String route = switch (mode) {
                case "thinking" -> "think_respond";
                case "pro", "ultra" -> "plan";
                default -> "respond";
            };

            return Map.of(
                    "executionMode", mode,
                    "route", route
            );

        } catch (Exception e) {
            log.warn("Router failed, defaulting to flash: {}", e.getMessage());
            return Map.of(
                    "executionMode", "flash",
                    "route", "respond"
            );
        }
    }

    private String getLastUserMessage(AgentGraphState state) {
        var messages = state.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole() == Message.Role.USER) {
                return messages.get(i).getContent();
            }
        }
        return null;
    }

}
