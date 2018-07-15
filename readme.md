# Code challenge (Aleksei Chaika, statistics service)

## About
This is implementation of service for handling transactions and generating statistics about them oin real-time(based on REST API). 

## Notes
By default statistics is stored for transactions not older than 60 secs (**configurable in application.yml**)
Port could be configured in properties too.
Main logic happens inside of `StatisticMonitor`.
Endpoint for getting statistics executes in O(1), as it was required, by using another thread for recalculating statistics in real time.
All transactions older than 60 secs are removed in real time too, and of course statistics recalculating stops if collection of transactions becomes empty.
There were no requirements about logging or storing of old transactions, so all deleted from `StatisticMonitor` transactions will be lost forever.

## Start Application
To run application just use `gradlew bootRun`,
I've also added simple integration test that actually helped me a little bit, to run it use `gradlew clean test --info`

## Using application
Use next endpoints:
* submit transaction - `POST 127.0.0.1:8000/transactions` with `{"amount":"2.20","timestamp":"999999999"}`
* get statistics - `GET 127.0.0.1:8000/statistics`
Port is configurable