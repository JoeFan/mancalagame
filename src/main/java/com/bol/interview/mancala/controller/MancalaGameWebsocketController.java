package com.bol.interview.mancala.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class MancalaGameWebsocketController {
    @RequestMapping({"/", "mancalagame"})
    public String toMancalaGame() {
        return "mancalagame";
    }


}