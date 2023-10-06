package com.yid.agv.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
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
    @GetMapping(value = "/equipment")
    public String pageEquipment() {
        return "equipment";
    }
    @GetMapping(value = "/equipment1")
    public ResponseEntity<Resource> getStaticPage() {
        Resource resource = new ClassPathResource("static/equipment copy.html");
        return ResponseEntity.ok().body(resource);
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
