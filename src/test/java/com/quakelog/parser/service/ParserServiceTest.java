package com.quakelog.parser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ParserServiceTest {

    private ParserService parserService;

    @Before
    public void setup(){
        parserService = new ParserService();
    }

    @Test
    public void games(){
        Map<String, Object> games = parserService.getGames();
        assertEquals(21, games.size());
    }

    @Test
    public void getGame(){
        Map<String, Object> games = parserService.getGame(1L);
        assertEquals(1, games.size());
    }

    @Test
    public void get_games_and_compare_with_example() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> gamesExample = objectMapper.readValue(getClass().getResource("/response.json"), Map.class);
        Map<String, Object> gamesParser = parserService.getGames();

        assertEquals(gamesExample.size(), gamesParser.size());
        assertEquals(gamesExample.keySet(), gamesParser.keySet());
        Assertions.assertThat(gamesExample).usingRecursiveComparison().isEqualTo(gamesParser);
    }
}