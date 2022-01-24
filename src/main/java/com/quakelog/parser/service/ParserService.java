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
        //Abre o arquivo de Log e monta uma lista de partidas
        List<Game> gameList = getGameList(OpenQuakeLogFile());
        //Caso seja informado um id da partida então vai filtrar somente pela partida em questão
        if(Objects.nonNull(id)){
            gameList = gameList.stream().filter(game -> Objects.equals(game.getId(), id)).collect(Collectors.toList());
        }
        //Monta um objeto do tipo Map para que o Json fique da forma correta
        Map<String, Object> gamesMap = new LinkedHashMap<>();
        gameList.forEach(game -> {
            Map<String, Object> gameMap = new LinkedHashMap<>();
            gameMap.put("total_kills", game.getTotalKills());
            gameMap.put("players", game.getClientUsers().stream().map(ClientUser::getName).distinct().collect(Collectors.toList()));
            gameMap.put("kills", getClientUserKillsMap(game.getClientUsers()));
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
        //Percorrer todas as linhas de logs e separar em uma lista de partidas
        for(String line : linesOfLog) {
            if(line.toUpperCase().contains(INIT_GAME.getCommand())) {
                //A primeira linha não pode ser inicio e o final da partida
                if(endGame > 0) {
                    gameList.add(linesOfLog.subList(initGame, endGame));
                    initGame = endGame;
                }
            }
            endGame++;
        }
        //A ultima partida deve ser adicionada no final do FOR
        if(initGame != endGame) {
            gameList.add(linesOfLog.subList(initGame, endGame));
        }
        //Converte e retorna uma lista de partidas
        return convertToListOfGame(gameList);
    }

    /*
        Método responsável por converter uma lista de String em uma lista de "Game"
    */
    private List<Game> convertToListOfGame(List<List<String>> gameList) {
        List<Game> games = new ArrayList<>();
        //Percorre as partidas separadas anteriormente
        gameList.forEach(game -> {
            //Cria uma nova partida
            Game gamePlay = new Game();
            //O id da partida sempre vai ser sequencial
            gamePlay.setId((long) (games.size() + 1));
            //Total de kills é a quantidade de Logs com a palavra 'Kill' na partida
            gamePlay.setTotalKills((int) game.stream()
                    .filter(line -> line.toUpperCase().contains(PLAYER_KILL.getCommand()))
                    .count());
            //Monta as informações das atividades dos jogadores na partida
            gamePlay.setClientUsers(getClientUsersByGame(game));
            //Adiciona a partida na lista de partidas
            games.add(gamePlay);
        });
        return games;
    }

    /*
        Método responsável por montar uma lista de ações dos jogadores
        armazena o jogador e a quantidade de kills dele por partida
    */
    private List<ClientUser> getClientUsersByGame(List<String> game) {
        //Cria uma lista dos jogadores que conectaram na partida
        List<String> clientUsersLineList = game.stream()
                .filter(line -> line.toUpperCase().contains(PLAYER_CONNECT.getCommand()))
                .distinct()
                .collect(Collectors.toList());

        List<ClientUser> clientUsers = new ArrayList<>();
        //Percorrer a lista de jogadores conectados para armazenar as informações das ações na partida
        clientUsersLineList.forEach(clientUser -> {
            ClientUser clientUserInfo = new ClientUser();
            //Captura o nome do jogador que está nas informações de conexão
            clientUserInfo.setName(clientUser.substring(clientUser.indexOf("n\\") + 2, clientUser.indexOf("\\t\\")));
            //Calcula a quantidade de kills do jogador na partida
            clientUserInfo.setKills(getKillsByClientUser(game, clientUserInfo.getName()));
            clientUsers.add(clientUserInfo);
        });
        //Retorna a lista de informações dos jogadores na partida
        return clientUsers;
    }

    /*
        Método responsável por calcular a quantidade de Kills de um jogador na partida
    */
    private int getKillsByClientUser(List<String> game, String name) {
        //Cria uma lista de kills somente do jogador em questão
        List<String> clientUsersKillList = game.stream()
                .filter(line -> line.toUpperCase().contains(PLAYER_KILL.getCommand()))
                .filter(line -> line.toUpperCase().contains(" "+name.toUpperCase()+ " "))
                .distinct()
                .collect(Collectors.toList());

        AtomicReference<Integer> kills = new AtomicReference<>(0);
        clientUsersKillList.forEach(kill -> {
            //Captura o jogador que efetuou a kill, conforme padrão no arquivo de LOG
            String killer = kill.substring(kill.lastIndexOf(": ")+2, kill.indexOf(" killed ")).trim();
            //Captura o jogador que sofreu a kill, conforme padrão no arquivo de LOG
            String dead = kill.substring(kill.indexOf(" killed ")+8, kill.lastIndexOf(" by ")).trim();
            //Suicidio não deve ser contabilizado
            if (!killer.equalsIgnoreCase(dead)){
                //Caso o jogador efetuou uma kill
                if(name.equalsIgnoreCase(killer)) {
                    kills.getAndSet(kills.get() + 1);
                }
                //Caso o jogador sofra uma kill do "<world>"
                if (WORLD_KILL.getCommand().equalsIgnoreCase(killer)){
                    kills.getAndSet(kills.get() - 1);
                }
            }
        });
        return kills.get();
    }

    /*
        Método responsável por converter a lista de informações do jogador
        para um objeto do tipo Map
    */
    private Map<String, Object> getClientUserKillsMap(List<ClientUser> clientUsers) {
        //Monta um objeto Map conforme a lista de informações dos jogadores
        Map<String, Object> clientUserKills = new LinkedHashMap<>();
        clientUsers.forEach(clientUser -> clientUserKills.put(clientUser.getName(), clientUser.getKills()));
        return clientUserKills;
    }
}
