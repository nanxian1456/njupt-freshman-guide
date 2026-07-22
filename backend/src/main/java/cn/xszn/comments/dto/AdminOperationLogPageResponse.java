package cn.xszn.comments.dto;

import java.util.List;

public record AdminOperationLogPageResponse(
    List<AdminOperationLogResponse> operations,
    int page,
    boolean hasNext) {}
