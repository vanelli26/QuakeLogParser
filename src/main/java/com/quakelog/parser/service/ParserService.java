package com.quakelog.parser.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ParserService implements IParserService{
    @Override
    public Map<String, Object> getGames() {
        Map<String, Object> games = new LinkedHashMap<>();
        Map<String, Object> game = new LinkedHashMap<>();
        game.put("total_kills", 0);
        game.put("players", 0);
        game.put("kills", 0);
        game.put("kills_by_means", 0);

        games.put("game_1", game);
        return games;
    }

    @Override
    public Map<String, Object> getGame(Long id) {
        return null;
    }
}
