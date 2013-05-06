Camel Transaction Example
This is an example of using database transactions with Apache Camel. It is based on the example given at the start of Chapter 12 of "Camel In Action", but using database transactions, rather than a JMS transaction. It has two routebuilders:
 * TransactionlessJDBCRouteBuilder
 * TransactedJDBCRouteBuilder

There are four testcases:
 1. TransactionlessJDBCTest.transactionlessJDBCTest_noError - will succeed, and shows that transactionless routebuilder inserts two records if there are no exceptions.
 1. TransactionlessJDBCTest.transactionlessJDBCTest_withConnectionProblem - will fail, showing that when the second insert fails, the first one is not rolled back.
 1. TransactedJDBCTest.transactedJDBCTest_noError - will succeed, showing two records inserted.
 1. TransactedJDBCTest.transactedJDBCTest_withConnectionProblem - will succeed, showing that the first insert is rolled back if the second one fails.

The project is built with Maven, so to run it, you need Maven on your path, and then you can run:
 * mvn test 

 Of course you can run the tests individually if you want by using the -Dtest parameter with maven. e.g.
 * mvn test -Dtest=TransactionlessJDBCTest