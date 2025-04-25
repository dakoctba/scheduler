package com.jacto.scheduler.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "scheduler")
            .commonTags("environment", "production");
    }

    @Bean
    public Counter schedulingTotalCounter(MeterRegistry registry) {
        return Counter.builder("scheduler_scheduling_total")
            .description("Total de agendamentos")
            .register(registry);
    }

    @Bean
    public Counter schedulingStatusCounter(MeterRegistry registry) {
        return Counter.builder("scheduler_scheduling_status_total")
            .description("Total de agendamentos por status")
            .tag("status", "PENDING")
            .register(registry);
    }

    @Bean
    public Timer schedulingDurationTimer(MeterRegistry registry) {
        return Timer.builder("scheduler_scheduling_duration_seconds")
            .description("Duração dos agendamentos")
            .register(registry);
    }
}
