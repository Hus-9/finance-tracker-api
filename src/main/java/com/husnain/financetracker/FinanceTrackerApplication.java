package com.husnain.financetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This is where the app starts, running this class starts the whole API.
// @SpringBootApplication does 3 things one, it marks this as a config class, turns on Spring Boot's auto setup and scans for our classes.
// The Scan only looks in this package and everything under it, which is why all the other packages are inside com.husnain.financetracker.
@SpringBootApplication
public class FinanceTrackerApplication {

    // The normal Java main method, it starts Spring which then sets up the database, the security and the web server.
    public static void main(String[] args) {

        SpringApplication.run(FinanceTrackerApplication.class, args);

    }
}

