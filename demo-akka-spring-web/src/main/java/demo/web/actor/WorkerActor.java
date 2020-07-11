package demo.web.actor;

import akka.actor.UntypedActor;
import demo.web.model.Message;
import demo.web.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component("workerActor")
@Scope("prototype")
public class WorkerActor extends UntypedActor {

    @Autowired
    private BusinessService businessService;

    private final CompletableFuture<Message> completableFuture;

    public WorkerActor(CompletableFuture<Message> completableFuture) {
        this.completableFuture = completableFuture;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        businessService.perform(this + " " + message);

        if (message instanceof Message) {
            completableFuture.complete((Message) message);
        } else {
            unhandled(message);
        }

        getContext().stop(self());
    }
}
