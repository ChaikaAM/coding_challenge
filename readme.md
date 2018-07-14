# Code challenge
To run application use `gradlew bootRun`
By default statistics is stored for transactions not older than 60 secs (**configurable in application.yml**)

Use next endpoints:
* submit transaction - `POST 127.0.0.1:8000/transactions` with `{"amount":"2.20","timestamp":"999999999"}`
* get statistics - `GET 127.0.0.1:8000/statistics`

Port could be configured in properies too.
Main logic happens inside of `StatisticMonitor`
There were no requirements about logging or storing of old transactions, so all deleted from `StatisticMonitor` transaction will be lost forever.