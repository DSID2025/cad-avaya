package gob.oax.cad.consumer.routing;

import gob.oax.cad.consumer.model.AgentAvailability;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentSessionRegistry {

    // agentId → set of sessionIds
    private final Map<String, Set<String>> agentSessions = new ConcurrentHashMap<>();

    // sessionId → agentId (inverso)
    private final Map<String, String> sessionToAgent = new ConcurrentHashMap<>();

    // agentId → availability (AVAILABLE, BUSY)
    private final Map<String, AgentAvailability> agentStates = new ConcurrentHashMap<>();

    public void register(String agentId, String sessionId) {
        agentSessions
                .computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        sessionToAgent.put(sessionId, agentId);
        agentStates.putIfAbsent(agentId, AgentAvailability.AVAILABLE);
    }

    public void unregister(String sessionId) {
        String agentId = sessionToAgent.remove(sessionId);
        if (agentId != null) {
            Set<String> sessions = agentSessions.get(agentId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    agentSessions.remove(agentId);
                    agentStates.remove(agentId);
                }
            }
        }
    }

    public boolean isOnline(String agentId) {
        return agentSessions.containsKey(agentId);
    }

    public boolean isAvailable(String agentId) {
        return agentStates.getOrDefault(agentId, AgentAvailability.AVAILABLE) == AgentAvailability.AVAILABLE;
    }

    public void markBusy(String agentId) {
        if (isOnline(agentId)) {
            agentStates.put(agentId, AgentAvailability.BUSY);
        }
    }

    public void markAvailable(String agentId) {
        if (isOnline(agentId)) {
            agentStates.put(agentId, AgentAvailability.AVAILABLE);
        }
    }

    public Set<String> getOnlineAgents() {
        return Set.copyOf(agentSessions.keySet());
    }

    public Set<String> getSessions(String agentId) {
        return agentSessions.getOrDefault(agentId, Collections.emptySet());
    }

    public Optional<String> findAnyAgent() {
        return agentSessions.keySet().stream().findFirst();
    }

    public Optional<String> findAnyAvailableAgent() {
        return agentSessions.keySet().stream()
                .filter(this::isAvailable)
                .findFirst();
    }
}