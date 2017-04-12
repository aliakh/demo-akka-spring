package demo.di;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class SpringActorProducer implements IndirectActorProducer {

	final private ApplicationContext applicationContext;
	final private Class<? extends Actor> actorClass;
	final private Object[] args;

	public SpringActorProducer(ApplicationContext applicationContext, Class<? extends Actor> actorClass, Object... args) {
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
