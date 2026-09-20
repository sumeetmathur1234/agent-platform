package com.aiagent.platform.model;

import lombok.Data;

@Data
public class CreateFollowRequest {
    private String follower;
    private String followee;
}
