package gob.oax.cad.adapter.listener;

import gob.oax.cad.adapter.model.CallMetadata;

import javax.telephony.Provider;
import java.util.Map;

public interface CallMonitoringService {

    public boolean initialize();

    public void routeCall(String callId, String targetExtension) throws Exception;

    public void transferCall(String callId, String toExtension) throws Exception;

    public void holdCall(String callId) throws Exception;

    public void unholdCall(String callId) throws Exception;

    public void terminateCall(String callId) throws Exception;

    public void addToConference(String callId, String newParticipantExt) throws Exception;

    public Map<String, CallMetadata> getActiveCalls();

    public Provider getProvider();

}