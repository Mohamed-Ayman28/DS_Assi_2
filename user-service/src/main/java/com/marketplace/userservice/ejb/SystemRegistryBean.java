package com.marketplace.userservice.ejb;

/**
 * EJB Type 2: SINGLETON Session Bean
 *
 * In a traditional Jakarta EE environment, this would be annotated with @Singleton
 * with @Startup and @Lock(LockType.READ/WRITE) for concurrency control.
 *
 * Simulation within Spring Boot:
 *  - @Component with default singleton scope (Spring beans are singletons by default)
 *  - Thread-safety achieved via synchronized methods and volatile fields
 *  - Initialized once at application startup via @PostConstruct
 *
 * Responsibility:
 *  - Maintain a single system-wide registry of service categories
 *  - Track platform statistics (total registrations, active sessions count)
 *  - Acts as an application-scoped configuration store
 */

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Simulated @Singleton EJB — one instance, shared across the JVM
@Component
@Slf4j
public class SystemRegistryBean {

    // Concurrency-safe structures (mirrors @Lock behavior in real EJB)
    private final Set<String> serviceCategories = Collections.synchronizedSet(new LinkedHashSet<>());
    private final AtomicLong totalRegistrations = new AtomicLong(0);
    private final Map<String, Long> categoryUsageCount = new ConcurrentHashMap<>();
    private volatile String systemStatus = "OPERATIONAL";
    private volatile long startupTime;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        startupTime = System.currentTimeMillis();
        // Seed default service categories
        List.of("Plumbing", "Carpentry", "Electrical", "Cleaning",
                "Painting", "Landscaping", "HVAC", "Appliance Repair")
                .forEach(this::addCategory);
        log.info("[EJB-Singleton] SystemRegistryBean initialized with {} categories", serviceCategories.size());
    }

    // ── Category Management ───────────────────────────────────────────────────

    public synchronized boolean addCategory(String category) {
        String normalized = category.trim();
        if (serviceCategories.add(normalized)) {
            categoryUsageCount.put(normalized, 0L);
            log.info("[EJB-Singleton] Category added: {}", normalized);
            return true;
        }
        return false; // already exists
    }

    public synchronized List<String> getAllCategories() {
        return new ArrayList<>(serviceCategories);
    }

    public synchronized boolean categoryExists(String category) {
        return serviceCategories.contains(category.trim());
    }

    public synchronized void incrementCategoryUsage(String category) {
        categoryUsageCount.merge(category, 1L, Long::sum);
    }

    public synchronized Map<String, Long> getCategoryUsageStats() {
        return new HashMap<>(categoryUsageCount);
    }

    // ── System Stats ──────────────────────────────────────────────────────────

    public long incrementAndGetRegistrations() {
        return totalRegistrations.incrementAndGet();
    }

    public long getTotalRegistrations() {
        return totalRegistrations.get();
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startupTime) / 1000;
    }

    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", systemStatus);
        info.put("uptimeSeconds", getUptimeSeconds());
        info.put("totalRegistrations", totalRegistrations.get());
        info.put("serviceCategories", getAllCategories());
        info.put("categoryUsage", getCategoryUsageStats());
        return info;
    }
}
