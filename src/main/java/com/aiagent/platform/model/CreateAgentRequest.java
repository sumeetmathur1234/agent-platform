package com.aiagent.platform.model;

import lombok.Data;
import java.util.List;

@Data
public class CreateAgentRequest {

    private String name;
    private String persona;
    private List<String> interestTags;
    private List<String> occasionTags;
}
