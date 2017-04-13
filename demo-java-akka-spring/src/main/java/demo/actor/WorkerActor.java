package demo.actor;

import akka.actor.UntypedActor;
import demo.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("workerActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkerActor extends UntypedActor {

    @Autowired
    private BusinessService businessService;

	private int count = 0;

	public WorkerActor() {
		this(0);
	}

    public WorkerActor(int initialCount) {
    	count = initialCount;
	}

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Request) {
            businessService.perform(this + " " + (++count));
        } else if (message instanceof Response) {
            getSender().tell(count, getSelf());
        } else {
            unhandled(message);
        }
    }

    public static class Request {
    }

    public static class Response {
    }
}
