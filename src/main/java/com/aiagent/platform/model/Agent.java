package com.aiagent.platform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
public class Agent {

    private final String name;
    private final String persona;
    private final List<String> interestTags;
    private final List<String> occasionTags;
}
