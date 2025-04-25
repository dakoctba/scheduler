package com.jacto.scheduler.service;

import com.jacto.scheduler.enumerations.SchedulingStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
    private final Counter schedulingTotalCounter;
    private final MeterRegistry registry;
    private final Timer schedulingDurationTimer;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.schedulingTotalCounter = Counter.builder("scheduler_scheduling_total")
            .description("Total de agendamentos")
            .register(registry);
        this.schedulingDurationTimer = Timer.builder("scheduler_scheduling_duration_seconds")
            .description("Duração dos agendamentos")
            .register(registry);
    }

    public void incrementSchedulingTotal() {
        schedulingTotalCounter.increment();
    }

    public void incrementSchedulingStatus(SchedulingStatus status) {
        Counter.builder("scheduler_scheduling_status_total")
            .description("Total de agendamentos por status")
            .tag("status", status.name())
            .register(registry)
            .increment();
    }

    public Timer.Sample startSchedulingDuration() {
        return Timer.start();
    }

    public void stopSchedulingDuration(Timer.Sample sample) {
        sample.stop(schedulingDurationTimer);
    }
}
