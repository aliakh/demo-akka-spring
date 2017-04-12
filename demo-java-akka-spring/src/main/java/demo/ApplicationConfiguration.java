package demo;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import demo.di.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ApplicationConfiguration {

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("demo-actor-system", akkaConfiguration());
    }

    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }
}
