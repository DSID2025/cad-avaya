package gob.oax.cad.consumer.routing;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallAssignmentRegistry {

    private final Map<String, String> callToAgent = new ConcurrentHashMap<>();

    public void assign(String callId, String agentId) {
        callToAgent.put(callId, agentId);
    }

    public Optional<String> getAssignedAgent(String callId) {
        return Optional.ofNullable(callToAgent.get(callId));
    }

    public void unassign(String callId) {
        callToAgent.remove(callId);
    }

    public void clearAll() {
        callToAgent.clear();
    }

    public boolean isAssigned(String callId) {
        return callToAgent.containsKey(callId);
    }
}
