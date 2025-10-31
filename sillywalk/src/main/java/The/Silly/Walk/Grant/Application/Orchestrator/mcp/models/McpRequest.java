package The.Silly.Walk.Grant.Application.Orchestrator.mcp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Base MCP request model for the Model Context Protocol.
 * Represents incoming requests from MCP clients.
 */
public class McpRequest {
    
    @JsonProperty("jsonrpc")
    private String jsonRpc = "2.0";
    
    private Object id;
    private String method;
    private JsonNode params;
    
    public McpRequest() {}
    
    public McpRequest(Object id, String method, JsonNode params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
    
    // Getters and setters
    public String getJsonRpc() {
        return jsonRpc;
    }
    
    public void setJsonRpc(String jsonRpc) {
        this.jsonRpc = jsonRpc;
    }
    
    public Object getId() {
        return id;
    }
    
    public void setId(Object id) {
        this.id = id;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public JsonNode getParams() {
        return params;
    }
    
    public void setParams(JsonNode params) {
        this.params = params;
    }
}