package gob.oax.cad.adapter.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.TerminalConnection;

@Slf4j
@UtilityClass
public class CallUtils {

    public String extractFrom(Call call) {
        try {
            Connection[] connections = call.getConnections();
            if (connections != null && connections.length > 0) {
                return connections[0].getAddress().getName();
            }
        } catch (Exception e) {
            log.warn("Failed to extract 'from' address: {}", e.getMessage());
        }
        return "";
    }

    public String extractTo(Call call) {
        try {
            Connection[] connections = call.getConnections();
            if (connections == null) return "";

            for (Connection conn : connections) {
                TerminalConnection[] terminalConnections = conn.getTerminalConnections();
                if (terminalConnections != null && terminalConnections.length > 0) {
                    return terminalConnections[0].getTerminal().getName();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract 'to' terminal: {}", e.getMessage());
        }
        return "";
    }
}
