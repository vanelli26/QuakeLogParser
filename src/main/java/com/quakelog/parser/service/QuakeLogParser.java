package com.quakelog.parser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.quakelog.parser.model.QuakeLog.*;

public abstract class QuakeLogParser {
    private static final Logger log = LoggerFactory.getLogger(QuakeLogParser.class);
    private static final String logFilePath = "/games.log";

    /*
        Método responsável por abrir o arquivo de log e retornar as linhas
    */
    public List<String> OpenQuakeLogFile() {
        try {
            URI uri = Objects.requireNonNull(this.getClass().getResource(logFilePath)).toURI();
            List<String> linhas = Files.readAllLines(Paths.get(uri));
            log.info("Arquivo de Log aberto com total de linhas: "+linhas.size());
            return linhas.stream().filter(getValidCommandLog()).collect(Collectors.toList());
        } catch (URISyntaxException | IOException e) {
            log.error("Error ao processar arquivo: "+e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /*
        Método responsável por filtrar as linhas somente por comandos necessários
        eliminando as linhas desnecessárias
    */
    private Predicate<String> getValidCommandLog() {
        return linha -> {
            linha = linha.toUpperCase(Locale.ROOT);
            return linha.contains(INIT_GAME.getCommand()) ||
                    linha.contains(PLAYER_CONNECT.getCommand()) ||
                    linha.contains(PLAYER_KILL.getCommand());
        };
    }
}
