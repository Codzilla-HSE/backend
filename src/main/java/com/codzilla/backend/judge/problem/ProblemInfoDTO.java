package com.codzilla.backend.judge.problem;

import lombok.Data;

@Data
public class ProblemInfoDTO {
    private Long id;
    private String name;
    private Long externalId;
    private String type;
    private String level;
}