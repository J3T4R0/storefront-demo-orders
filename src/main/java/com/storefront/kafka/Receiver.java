package com.storefront.kafka;

import com.storefront.model.Customer;
import com.storefront.respository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class Receiver {

    @Autowired
    private CustomerRepository customerRepository;

    private CountDownLatch latch = new CountDownLatch(1);

    public CountDownLatch getLatch() {
        return latch;
    }

    @KafkaListener(topics = "${spring.kafka.topic.accounts-customer}")
    public void receive(Customer customer) {
        log.info("received payload='{}'", customer.toString());
        latch.countDown();

        customerRepository.save(customer);
    }
}