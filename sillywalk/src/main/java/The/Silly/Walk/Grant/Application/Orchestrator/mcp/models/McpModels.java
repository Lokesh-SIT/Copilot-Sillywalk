package The.Silly.Walk.Grant.Application.Orchestrator.mcp.models;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool definition for the Ministry of Silly Walks Grant Application System.
 * Represents tools that can be invoked by MCP clients.
 */
public class McpTool {
    
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    
    public McpTool() {}
    
    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }
    
    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
}

/**
 * MCP Resource definition for grant application resources.
 */
class McpResource {
    
    private String uri;
    private String name;
    private String description;
    private String mimeType;
    
    public McpResource() {}
    
    public McpResource(String uri, String name, String description, String mimeType) {
        this.uri = uri;
        this.name = name;
        this.description = description;
        this.mimeType = mimeType;
    }
    
    // Getters and setters
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}

/**
 * MCP Tool result for tool execution responses.
 */
class McpToolResult {
    
    private List<McpContent> content;
    private boolean isError;
    
    public McpToolResult() {}
    
    public McpToolResult(List<McpContent> content) {
        this.content = content;
        this.isError = false;
    }
    
    public McpToolResult(List<McpContent> content, boolean isError) {
        this.content = content;
        this.isError = isError;
    }
    
    // Getters and setters
    public List<McpContent> getContent() {
        return content;
    }
    
    public void setContent(List<McpContent> content) {
        this.content = content;
    }
    
    public boolean isError() {
        return isError;
    }
    
    public void setError(boolean error) {
        isError = error;
    }
}

/**
 * MCP Content for tool results and resource content.
 */
class McpContent {
    
    private String type;
    private String text;
    
    public McpContent() {}
    
    public McpContent(String type, String text) {
        this.type = type;
        this.text = text;
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}