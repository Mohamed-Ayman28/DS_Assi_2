package com.marketplace.userservice.ejb;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class SystemRegistryBean {

    private final Set<String> serviceCategories = Collections.synchronizedSet(new LinkedHashSet<>());
    private final AtomicLong totalRegistrations = new AtomicLong(0);
    private final Map<String, Long> categoryUsageCount = new ConcurrentHashMap<>();
    private volatile String systemStatus = "OPERATIONAL";
    private volatile long startupTime;
    private final Set<String> activeSessions = Collections.synchronizedSet(new HashSet<>());


    @PostConstruct
    public void init() {
        startupTime = System.currentTimeMillis();
        List.of("Plumbing", "Carpentry", "Electrical", "Cleaning",
                "Painting", "Landscaping", "HVAC", "Appliance Repair")
                .forEach(this::addCategory);
        log.info("[EJB-Singleton] SystemRegistryBean initialized with {} categories", serviceCategories.size());
    }

    public synchronized boolean addCategory(String category) {
        String normalized = category.trim();
        if (serviceCategories.add(normalized)) {
            categoryUsageCount.put(normalized, 0L);
            log.info("[EJB-Singleton] Category added: {}", normalized);
            return true;
        }
        return false;
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

    public boolean isAlreadyLoggedIn(String username) {
        return activeSessions.contains(username);
    }

    public void addSession(String username) {
        activeSessions.add(username);
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
    }

}
