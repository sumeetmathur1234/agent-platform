package com.aiagent.platform.model;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String authorId;
    private String content;
    private String parentId;
}
