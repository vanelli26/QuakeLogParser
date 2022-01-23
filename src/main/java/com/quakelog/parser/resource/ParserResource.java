package com.quakelog.parser.resource;

import com.quakelog.parser.service.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/partidas")
public class ParserResource {

    @Autowired
    private ParserService parserService;

    @GetMapping
    public ResponseEntity<?> partidas() {
        Map<String, Object> games = parserService.getGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }
}
