package com.example.ethreader.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DepositPageController {

    @GetMapping("/deposit")
    public String depositPage() {
        return "deposit";
    }
}

