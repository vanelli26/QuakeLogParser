package com.quakelog.parser.service;

import com.quakelog.parser.model.ClientUser;
import com.quakelog.parser.model.Game;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.quakelog.parser.model.QuakeLog.*;

@Service
public class ParserService extends QuakeLogParser implements IParser {

    @Override
    public Map<String, Object> getGames() {
        return quakeParserToJson(null);
    }

    @Override
    public Map<String, Object> getGame(Long id) {
        return quakeParserToJson(id);
    }

    /*
        Método responsável por fazer a leitura do arquivo de log
        e retornar um objeto json dos jogos
    */
    private Map<String, Object> quakeParserToJson(Long id) {
        List<Game> gameList = getGameList(OpenQuakeLogFile());
        if(Objects.nonNull(id)){
            gameList = gameList.stream().filter(game -> Objects.equals(game.getId(), id)).collect(Collectors.toList());
        }

        Map<String, Object> gamesMap = new LinkedHashMap<>();
        gameList.forEach(game -> {
            Map<String, Object> gameMap = new LinkedHashMap<>();
            gameMap.put("total_kills", game.getTotalKills());
            gameMap.put("players", game.getClientUsers().stream().map(ClientUser::getName).distinct().sorted());
            gameMap.put("kills", getClientUserKills(game.getClientUsers()));
            gamesMap.put("game_" + game.getId(), gameMap);
        });
        return gamesMap;
    }

    /*
        Método responsável por separar a lista de Strings do Log em
        uma lista de jogos separando pelo comando InitGame
    */
    private List<Game> getGameList(List<String> linesOfLog) {
        List<List<String>> gameList = new ArrayList<>();
        int initGame = 0;
        int endGame = 0;
        for(String line : linesOfLog) {
            if(line.toUpperCase().contains(INIT_GAME.getCommand())) {
                if(endGame > 0) {
                    gameList.add(linesOfLog.subList(initGame, endGame));
                    initGame = endGame;
                }
            }
            endGame++;
        }
        if(initGame != endGame) {
            gameList.add(linesOfLog.subList(initGame, endGame));
        }
        return convertToListOfGame(gameList);
    }

    /*
        Método responsável por converter uma lista de String em uma lista de "Game"
    */
    private List<Game> convertToListOfGame(List<List<String>> gameList) {
        List<Game> games = new ArrayList<>();
        gameList.forEach(game -> {
            Game gamePlay = new Game();
            gamePlay.setId((long) (games.size() + 1));
            gamePlay.setTotalKills(game.stream()
                    .filter(line -> line.toUpperCase().contains(PLAYER_KILL.getCommand()))
                    .count());
            gamePlay.setClientUsers(getClientUsersByGame(game));
            games.add(gamePlay);
        });
        return games;
    }

    private List<ClientUser> getClientUsersByGame(List<String> game) {
        List<String> clientUsersList = game.stream()
                .filter(line -> line.toUpperCase().contains(PLAYER_CONNECT.getCommand()))
                .distinct()
                .collect(Collectors.toList());
        List<ClientUser> clientUsers = new ArrayList<>();
        clientUsersList.forEach(clientUser -> {
            ClientUser clientUserInfo = new ClientUser();
            String name = clientUser.substring(clientUser.indexOf("n\\") + 2, clientUser.indexOf("\\t\\"));
            clientUserInfo.setName(name);
            clientUserInfo.setKills(getKillsByClientUser(game, name));
            clientUsers.add(clientUserInfo);
        });
        return clientUsers;
    }

    private Long getKillsByClientUser(List<String> game, String name) {
        List<String> clientUsersKillList = game.stream()
                .filter(line -> line.toUpperCase().contains(PLAYER_KILL.getCommand()))
                .filter(line -> line.toUpperCase().contains(name.toUpperCase()))
                .distinct()
                .collect(Collectors.toList());
        AtomicReference<Long> kills = new AtomicReference<>(0L);
        clientUsersKillList.forEach(kill -> {
            String killer = kill.substring(kill.lastIndexOf(":")+1, kill.indexOf("killed")).trim();
            String dead = kill.substring(kill.indexOf("killed")+7, kill.lastIndexOf("by")).trim();
            if(killer.equalsIgnoreCase(name)) {
                kills.getAndSet(kills.get() + 1);
            }
            if(dead.equalsIgnoreCase(name)) {
                kills.getAndSet(kills.get() - 1);
            }
        });
        return kills.get();
    }

    private Map<String, Object> getClientUserKills(List<ClientUser> clientUsers) {
        Map<String, Object> clientUserKills = new LinkedHashMap<>();
        clientUsers.forEach(clientUser -> clientUserKills.put(clientUser.getName(), clientUser.getKills()));
        return clientUserKills;
    }
}
