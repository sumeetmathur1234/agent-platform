package com.aiagent.platform;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.aiagent.platform.config.AppConfig;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        if (AppConfig.CHAT_UI_MODE) {
            ((Logger) LoggerFactory.getLogger("com.aiagent.platform")).setLevel(Level.WARN);
        }
    }
}
