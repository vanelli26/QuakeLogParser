package com.quakelog.parser.model;

public enum QuakeLog {
    INIT_GAME("INITGAME:"),
    PLAYER_CONNECT("CLIENTUSERINFOCHANGED:"),
    PLAYER_KILL("KILL:");

    private String command;
    QuakeLog(String command) { this.command = command; }
    public String getCommand(){return command;}
}
