package demo;

import javax.annotation.Resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import demo.actor.WorkerActor;
import demo.di.SpringExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

@Component
class Runner implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private SpringExtension springExtension;

    @Resource
	private ActorRef autowiredSingleton;

    @Resource(name = "autowiredSingleton")
	private ActorRef autowiredSingleton2;

    @Resource
	private ActorRef autowiredPrototype;

	@Resource(name = "autowiredPrototype")
	private ActorRef autowiredPrototype2;

    @Override
    public void run(String[] args) throws Exception {
        try {
            ActorRef manualWorker = actorSystem.actorOf(springExtension.props("workerActor"), "worker-actor");
			runWorker(manualWorker);

			ActorRef manualWorkerWithArgs = actorSystem.actorOf(springExtension.props("workerActor", 100), "worker-actor-2");
			runWorker(manualWorkerWithArgs);

			ActorRef manualWorkerFromClass = actorSystem.actorOf(springExtension.props(WorkerActor.class), "worker-actor-from-class");
			runWorker(manualWorkerFromClass);

			runWorker(autowiredSingleton);
			runWorker(autowiredSingleton2);

			runWorker(autowiredPrototype);
			runWorker(autowiredPrototype2);
        } finally {
            actorSystem.terminate();
            Await.result(actorSystem.whenTerminated(), Duration.Inf());
        }
    }

	private void runWorker(final ActorRef workerActor) throws Exception {
		workerActor.tell(new WorkerActor.Request(), null);
		workerActor.tell(new WorkerActor.Request(), null);
		workerActor.tell(new WorkerActor.Request(), null);

		FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
		Future<Object> awaitable = Patterns.ask(workerActor, new WorkerActor.Response(), Timeout.durationToTimeout(duration));

		logger.info("Response: " + Await.result(awaitable, duration));
	}
}
