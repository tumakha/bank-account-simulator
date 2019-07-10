# bank-account-simulator #

Bank Account Simulator - test task

### Prerequisites ###

Java 11, Gradle 4.10+ or gradle-wrapper

### Run/Debug main class ###

    bank.account.WebAppMain
    
### Build ###

    gradle build

### Run web app ###

    cd ./build/libs
    
    java -jar bank-account.jar
    
Note: Java 11 is required


RESTful API
===========

 * Create bank account

        curl -v -X POST --header "Content-Type: application/json" -d '{"balance": 1000}' localhost:8888/v1/account/1122

 * Get bank account balance

        curl -v localhost:8888/v1/account/1122

 * Transfer money from one bank account to another

        curl -v -X POST --header "Content-Type: application/json" -d '{"from": 1111, "to": 2222, "amount": 500}' localhost:8888/v1/transfer
