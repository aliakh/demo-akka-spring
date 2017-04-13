package demo.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import demo.di.SpringActorProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ActorConfiguration {

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private SpringActorProps springActorProps;

	@Bean
	public ActorRef autowiredSingleton() {
		return actorSystem.actorOf(springActorProps.props("workerActor"), "singleton-worker-actor");
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ActorRef autowiredPrototype() {
		return actorSystem.actorOf(springActorProps.props("workerActor"));
	}
}
