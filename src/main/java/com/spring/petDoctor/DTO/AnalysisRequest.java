package com.spring.petDoctor.DTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AnalysisRequest {
    private String imageUrl;
    private String inputText;
}
