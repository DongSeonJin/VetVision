package com.spring.petDoctor.DTO;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Builder
public class ThreadInfo {
    private String runId;
    private String threadId;

}
