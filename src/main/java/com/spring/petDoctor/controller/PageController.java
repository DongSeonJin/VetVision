package com.spring.petDoctor.controller;

import com.spring.petDoctor.service.GPTService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PageController {

    private final GPTService gptService;

    public PageController(GPTService gptService) {
        this.gptService = gptService;
    }

    @RequestMapping("/")
    public String home(){
        return "/main";
    }



}
