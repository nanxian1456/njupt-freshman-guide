package cn.xszn.comments.dto;

import cn.xszn.comments.model.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
    @NotNull TargetType targetType,
    @NotBlank @Pattern(regexp = "[a-z0-9-]{2,64}") String targetKey,
    @Size(max = 20) String nickname,
    @NotBlank @Size(min = 5, max = 300) String content,
    @NotBlank @Size(max = 256) String formToken,
    @Size(max = 0) String website) {}
