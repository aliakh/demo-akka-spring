package demo.di;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class SpringActorClassProducer implements IndirectActorProducer {

	private final ApplicationContext applicationContext;
	private final Class<? extends Actor> actorClass;
	private final Object[] args;

	public SpringActorClassProducer(ApplicationContext applicationContext, Class<? extends Actor> actorClass, Object... args) {
		this.applicationContext = applicationContext;
		this.actorClass = actorClass;
		this.args = args;
	}

	@Override
	public Actor produce() {
		return applicationContext.getBean(actorClass, args);
	}

	@Override
	public Class<? extends Actor> actorClass() {
		return actorClass;
	}
}
