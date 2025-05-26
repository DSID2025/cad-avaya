package gob.oax.cad.consumer.config;

import gob.oax.cad.consumer.model.AgentActionMessage;
import gob.oax.cad.consumer.model.ConferenceCallRequest;
import gob.oax.cad.consumer.model.RouteCallRequest;
import gob.oax.cad.consumer.model.TransferCallRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "call-adapter", url = "${adapter.avaya.url}")
public interface CallAdapterClient {

    @PostMapping("/api/calls/route")
    void routeCall(@RequestBody RouteCallRequest request);

    @PostMapping("/api/calls/{callId}/end")
    void endCall(@PathVariable String callId);

    @PostMapping("/api/calls/{callId}/hold")
    void holdCall(@PathVariable String callId);

    @PostMapping("/api/calls/{callId}/unhold")
    void unholdCall(@PathVariable String callId);

    @PostMapping("/api/calls/transfer")
    void transferCall(@RequestBody TransferCallRequest request);

    @PostMapping("/api/calls/conference")
    void addToConference(@RequestBody ConferenceCallRequest request);
}