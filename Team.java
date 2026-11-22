package com.aegis.team;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    private final UUID id;
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();

    public Team(UUID leader) {
        this.id = UUID.randomUUID();
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID getId() { return id; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Set<UUID> getMembers() { return members; }
}
