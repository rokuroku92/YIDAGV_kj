package com.yid.agv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agv")
public class AgvController {
    
    @GetMapping(value = "/")
    public String pageAgv() {
        return "agv";
    }
    @GetMapping(value = "/analysis")
    public String pageAnalysis() {
        return "analysis";
    }

    @GetMapping(value = "/message")
    public String pageMessage() {
        return "message";
    }

}
