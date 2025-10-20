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
                .defaultSystem("You are useful assistant that calls tools to reply to questions." +
                        "When using the neo4j_cypher tools, always call the get_neo4j_schema tool first and always return the executed Cypher query with the answer.")
                .defaultToolCallbacks(provider.getToolCallbacks())
                .defaultTools(ragTools)
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
