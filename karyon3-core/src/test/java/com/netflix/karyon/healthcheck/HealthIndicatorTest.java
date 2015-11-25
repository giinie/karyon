package com.netflix.karyon.healthcheck;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckStatus;
import com.netflix.karyon.health.HealthIndicator;
import com.netflix.karyon.health.HealthIndicators;
import com.netflix.karyon.health.HealthState;

public class HealthIndicatorTest {
    @Test
    public void failureOnNoDefinedHealthCheck() {
        Injector injector = Karyon.newBuilder().start();
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleHealthCheck() {
        Injector injector = Karyon.newBuilder().addModules(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
            }
        }).start();
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleAndNamedHealthCheck() {
        Injector injector = Karyon.newBuilder().addModules(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        }).start();
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(3, status.getIndicators().size());
        
        System.out.println(status.getIndicators());
    }
    
    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        });
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicators.alwaysUnhealthy("bar"));
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
    }
}