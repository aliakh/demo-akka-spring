package demo.web.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import demo.web.model.Message;
import demo.web.spring.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CompletableFutureService {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private SpringExtension springExtension;

    public CompletableFuture<Message> get(String payload, Long id) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        ActorRef workerActor = actorSystem.actorOf(springExtension.props("workerActor", future), "worker-actor");
        workerActor.tell(new Message(payload, id), null);
        return future;
    }
}
