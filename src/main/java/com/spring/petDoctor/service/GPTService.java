package com.spring.petDoctor.service;

import com.spring.petDoctor.DTO.ThreadInfo;

import java.io.IOException;
import java.net.URISyntaxException;

public interface GPTService {
    ThreadInfo createThreadAndRun(String question) throws URISyntaxException, IOException, InterruptedException;

    String imageAnalysis(String image, String question) throws URISyntaxException, IOException, InterruptedException;

    String checkThreadStatus(String threadId, String runId) throws IOException, InterruptedException, URISyntaxException;

    String getResponse(String threadId) throws URISyntaxException, IOException, InterruptedException;

    void deleteThread(String threadId) throws URISyntaxException, IOException, InterruptedException;
}
