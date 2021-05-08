### Project
A RESTful API for Bank Account creation and performing different actions of the different types of BankAccount.

CHECKING_ACCOUNT, SAVINGS_ACCOUNT and PRIVATE_LOAN_ACCOUNT


### Solution
Since the app is supposed to be stateless, the below design considerations were made:
* Repository methods are synchronized to ensure thread saftey
* Bank accounts are stored in Hashmap with IBAN as key for easy lookup (0(1))


### Technologies
This project is created with:
* Java version 11
* SpringBoot version 2.3.5.RELEASE
* RestAssured, Junit 5 and Mockito for Testing
* Docker for containerization
* Swagger for API documentation

### Installation and Running
The project can be run in two ways:
* Gradle
* Docker

#### Maven
To start the app using gradle, you'll need to run the command below:

```
./gradlew bootRun
```

#### Docker
To run the app as a docker container, we have to first package the jar by running the command below
 ```
./gradlew build
```
 
build the image using the Dockerfile using the command below

```
 docker build ${project-dir} -t sample-tag
```

and then run the app in a container by using the command below:

```
docker run -p 8080:8080 sample-tag
```

App should be available at **localhost:8080**
  
### Documentation
 The API documentation can be viewed in this path
 **/swagger-ui.html**

### Testing
To run the tests that were created along with the submission, Kindly run the command below:

```
./gradlew clean test
```

### Production ready considerations(out of scope)
* Metrics and Alerts

### Side Note :)
* Since application is supposed to be stateless, we cannot get the ACID properties of a relational database to gaurantee `TRANSACTIONS`
