package com.quakelog.parser.service;

import java.util.Map;

public interface IParserService {
    Map<String, Object> getGames();
    Map<String, Object> getGame(Long id);
}
