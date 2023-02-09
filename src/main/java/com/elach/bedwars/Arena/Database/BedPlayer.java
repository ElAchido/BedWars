package com.elach.bedwars.Arena.Database;

import java.util.UUID;

public class BedPlayer {

    private final UUID uuid;
    private final Float points;

    public BedPlayer(UUID uuid, Float points) {
        this.uuid = uuid;
        this.points = points;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Float getPoints() {
        return points;
    }
}
