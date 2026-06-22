package com.jmhreif.graphrag_fundamentals.agentic;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//Demo purpose
@RestController
@RequestMapping("/agents")
public class AgentController {
    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpProvider;

    public AgentController(ChatClient.Builder builder, SyncMcpToolCallbackProvider provider, RAGTools ragTools) {
        this.chatClient = builder
                .defaultSystem("""
                        You are a helpful assistant that answers questions using the available tools. Choose tools by question type:
                        - vectorSearch: questions about news article contents, topics, or sentiment.
                        - graphEnrichedSearch: broad, semantic questions about which organizations or industries appear in news topics or trends.
                        - Neo4j MCP tools (get-schema then read-cypher): ANY question requiring exact property values, specific named entity lookup, numeric filtering, counting, or aggregation.
                        Always prefer MCP tools when the question has specific names, numbers, or filters.
                        When using MCP tools, call get-schema first, then read-cypher. Return the executed Cypher query with your answer.""")
                .defaultTools(provider, ragTools)
                .build();
        this.mcpProvider = provider;
    }

    //test endpoint to ensure MCP connection
    @GetMapping("/debug/tools")
    public String debugTools() {
        var callbacks = mcpProvider.getToolCallbacks();
        StringBuilder sb = new StringBuilder("Available MCP Tools:\n");
        for (var callback : callbacks) {
            sb.append("- ").append(callback.getToolDefinition().name()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/agentic")
    public String agentic(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
