package cn.xszn.comments.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTargetCatalogRequest(
    @NotBlank @Size(max = 40) String name,
    @NotBlank @Size(max = 40) String groupName,
    @Min(0) @Max(1000) int sortOrder,
    @NotNull Boolean enabled) {}
