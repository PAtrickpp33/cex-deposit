package com.example.ethreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private DepositProcessorService depositProcessorService;

    // Update confirmations every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void updateConfirmations() {
        try {
            depositProcessorService.updateConfirmations();
        } catch (Exception e) {
            logger.error("Error updating confirmations in scheduled task", e);
        }
    }
}

