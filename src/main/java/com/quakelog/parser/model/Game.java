package com.quakelog.parser.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Game {
    private Long id;
    private int totalKills;
    private List<ClientUser> clientUsers;
}
