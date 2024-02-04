package com.spring.petDoctor.controller;

import com.spring.petDoctor.DTO.AnalysisRequest;
import com.spring.petDoctor.DTO.ThreadInfo;
import com.spring.petDoctor.service.GPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;


@RestController
public class GPTController {

    private final GPTService gptService;

    @Autowired
    public GPTController(GPTService gptService) {
        this.gptService = gptService;
    }


    @RequestMapping(value = "/vet", method = RequestMethod.POST)
    public String analysis(@RequestBody AnalysisRequest analysisRequest) throws URISyntaxException, IOException, InterruptedException {
        String response = null;
        //이미지가 없다면 assistants api, 있다면 vision api
        if(analysisRequest.getImageUrl() == null){
            ThreadInfo threadInfo = gptService.createThreadAndRun(analysisRequest.getInputText());

            while(true){
                Thread.sleep(2000);
                String status = gptService.checkThreadStatus(threadInfo.getThreadId(), threadInfo.getRunId());
                if(status.equals("completed")){
                    break;
                }
            }
            response = gptService.getResponse(threadInfo.getThreadId());
            gptService.deleteThread(threadInfo.getThreadId());


        }else {
            response = gptService.imageAnalysis(analysisRequest.getImageUrl(), analysisRequest.getInputText());
        }





        return response;
    }
}
