# Kafka Traveler Microservices Demo: Orders

## Orders Service

Spring Boot/Kafka/Mongo Microservice, one of a set of microservices for this project. Services use Spring Kafka 2.1.6 to maintain eventually consistent data between their different `Customer` domain objects.

Originally code based on the post, [Spring Kafka - JSON Serializer Deserializer Example](https://www.codenotfound.com/spring-kafka-json-serializer-deserializer-example.html), from the [CodeNotFound.com](https://www.codenotfound.com/) Blog. Original business domain idea based on the post, [Distributed Sagas for Microservices](https://dzone.com/articles/distributed-sagas-for-microservices), on [DZone](https://dzone.com/).

## Development

For [Kakfa](https://kafka.apache.org/), I use my [garystafford/kafka-docker](https://github.com/garystafford/kafka-docker) project, a clone of the [wurstmeister/kafka-docker](https://github.com/wurstmeister/kafka-docker) project. The `garystafford/kafka-docker` [local docker-compose file](https://github.com/garystafford/kafka-docker/blob/master/docker-compose-local.yml) builds a Kafka, ZooKeeper, MongoDB, and Alpine Linux OpenJDK container.

## Commands

I debug directly from JetBrains IntelliJ. For testing the application in development, I build the jar, copy it to Alpine Linux OpenJDK `testapp` container, and run it. If testing more than one service in the same testapp container, make sure ports don't collide. Start services on different ports.

```bash
# start container if stopped
docker start kafka-docker_testapp_1

# build
./gradlew clean build

# copy
docker cp build/libs/orders-1.0.0.jar kafka-docker_testapp_1:/orders-1.0.0.jar
docker exec -it kafka-docker_testapp_1 sh

# install curl
apk update && apk add curl

# start with 'dev' profile
# same testapp container as accounts,
# so start on different port
java -jar orders-1.0.0.jar \
    --spring.profiles.active=dev \
    --server.port=8090 \
    --logging.level.root=DEBUG
```

## Creating Sample Data

Create sample customers with an order history.
```bash
# create sample accounts customers
curl http://localhost:8080/customers/sample

# create sample orders products
curl http://localhost:8090/products/sample

# add sample order history to orders customers
# (received from kafka `accounts.customers.change` topic)
curl http://localhost:8090/customers/samples
curl http://localhost:8090/customers/sample
curl http://localhost:8090/customers/fulfill

```

## Container Infrastructure

```text
CONTAINER ID        IMAGE                            COMMAND                  CREATED             STATUS              PORTS                                                NAMES
6079603c5d92        openjdk:8u151-jdk-alpine3.7      "sleep 6000"             4 hours ago         Up About an hour                                                         kafka-docker_testapp_1
df8914058cbb        hlebalbau/kafka-manager:latest   "/kafka-manager/bin/…"   4 hours ago         Up 4 hours          0.0.0.0:9000->9000/tcp                               kafka-docker_kafka_manager_1
5cd8f61330e0        wurstmeister/kafka:latest        "start-kafka.sh"         4 hours ago         Up 4 hours          0.0.0.0:9092->9092/tcp                               kafka-docker_kafka_1
497901621c7d        mongo:latest                     "docker-entrypoint.s…"   4 hours ago         Up 4 hours          0.0.0.0:27017->27017/tcp                             kafka-docker_mongo_1
9079612e36ad        wurstmeister/zookeeper:latest    "/bin/sh -c '/usr/sb…"   4 hours ago         Up 4 hours          22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   kafka-docker_zookeeper_1
```

## Orders Customer Object in MongoDB

`db.customer.orders.find().pretty();`

```bson
{
	"_id" : ObjectId("5b0c54e2be41760051d00383"),
	"name" : {
		"title" : "Mr.",
		"firstName" : "John",
		"middleName" : "S.",
		"lastName" : "Doe",
		"suffix" : "Jr."
	},
	"contact" : {
		"primaryPhone" : "555-666-7777",
		"secondaryPhone" : "555-444-9898",
		"email" : "john.doe@internet.com"
	},
	"orders" : [
		{
			"timestamp" : NumberLong("1527538871249"),
			"orderStatus" : "COMPLETED",
			"orderItems" : [
				{
					"productGuid" : "b5efd4a0-4eb9-4ad0-bc9e-2f5542cbe897",
					"quantity" : 2,
					"unitPrice" : "1.99"
				},
				{
					"productGuid" : "a9d5a5c7-4245-4b4e-b1c3-1d3968f36b2d",
					"quantity" : 4,
					"unitPrice" : "5.99"
				},
				{
					"productGuid" : "f3b9bdce-10d8-4c22-9861-27149879b3c1",
					"quantity" : 1,
					"unitPrice" : "9.99"
				},
				{
					"productGuid" : "b506b962-fcfa-4ad6-a955-8859797edf16",
					"quantity" : 3,
					"unitPrice" : "13.99"
				}
			]
		},
		{
			"timestamp" : NumberLong("1527538871249"),
			"orderStatus" : "PROCESSING",
			"orderItems" : [
				{
					"productGuid" : "d01fde07-7c24-49c5-a5f1-bc2ce1f14c48",
					"quantity" : 5,
					"unitPrice" : "3.99"
				},
				{
					"productGuid" : "4efe33a1-722d-48c8-af8e-7879edcad2fa",
					"quantity" : 2,
					"unitPrice" : "7.99"
				},
				{
					"productGuid" : "7f3c9c22-3c0a-47a5-9a92-2bd2e23f6e37",
					"quantity" : 4,
					"unitPrice" : "11.99"
				}
			]
		}
	],
	"_class" : "com.storefront.model.Customer"
}
```

## Current Results

Output from application, on the `accounts.customers.change` topic

```text
2018-05-29 02:14:29.774  INFO [-,,,] 312 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-1, groupId=orders] Successfully joined group with generation 3
2018-05-29 02:14:29.782  INFO [-,,,] 312 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-1, groupId=orders] Setting newly assigned partitions [accounts.customers.change-0]
2018-05-29 02:14:29.789  INFO [-,,,] 312 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : partitions assigned: [accounts.customers.change-0]
2018-05-29 02:15:25.098  INFO [-,c7948d1dffbfabea,c7948d1dffbfabea,false] 312 --- [nio-8890-exec-1] org.mongodb.driver.connection            : Opened connection [connectionId{localValue:2, serverValue:59}] to mongo:27017
2018-05-29 02:16:09.902  INFO [-,905beb7476b150df,90f3b6934989d2ae,false] 312 --- [ntainer#0-0-C-1] com.storefront.kafka.Receiver            : received payload='Customer(id=5b0cb7e9be417600fda8c707, name=Name(title=Mr., firstName=John, middleName=S., lastName=Doe, suffix=Jr.), contact=Contact(primaryPhone=555-666-7777, secondaryPhone=555-444-9898, email=john.doe@internet.com), orders=null)'
2018-05-29 02:16:09.920  INFO [-,905beb7476b150df,503afeb73b072ac4,false] 312 --- [ntainer#0-0-C-1] com.storefront.kafka.Receiver            : received payload='Customer(id=5b0cb7e9be417600fda8c708, name=Name(title=Ms., firstName=Mary, middleName=null, lastName=Smith, suffix=null), contact=Contact(primaryPhone=456-789-0001, secondaryPhone=456-222-1111, email=marysmith@yougotmail.com), orders=null)'
```

Output from Kafka container using the following command.

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --from-beginning --topic accounts.customers.change
```

Kafka Consumer Output

```text
{"id":"5b0ca230be4176002f61999f","name":{"title":"Mr.","firstName":"John","middleName":"S.","lastName":"Doe","suffix":"Jr."},"contact":{"primaryPhone":"555-666-7777","secondaryPhone":"555-444-9898","email":"john.doe@internet.com"},"addresses":[{"type":"BILLING","description":"My cc billing address","address1":"123 Oak Street","address2":null,"city":"Sunrise","state":"CA","postalCode":"12345-6789"},{"type":"SHIPPING","description":"My home address","address1":"123 Oak Street","address2":null,"city":"Sunrise","state":"CA","postalCode":"12345-6789"}],"creditCards":[{"type":"PRIMARY","description":"VISA","number":"1234-6789-0000-0000","expiration":"6/19","nameOnCard":"John S. Doe"},{"type":"ALTERNATE","description":"Corporate American Express","number":"9999-8888-7777-6666","expiration":"3/20","nameOnCard":"John Doe"}],"credentials":{"username":"johndoe37","password":"skd837#$hfh485&"}}
{"id":"5b0ca230be4176002f6199a0","name":{"title":"Ms.","firstName":"Mary","middleName":null,"lastName":"Smith","suffix":null},"contact":{"primaryPhone":"456-789-0001","secondaryPhone":"456-222-1111","email":"marysmith@yougotmail.com"},"addresses":[{"type":"BILLING","description":"My CC billing address","address1":"1234 Main Street","address2":null,"city":"Anywhere","state":"NY","postalCode":"45455-66677"},{"type":"SHIPPING","description":"Home Sweet Home","address1":"1234 Main Street","address2":null,"city":"Anywhere","state":"NY","postalCode":"45455-66677"}],"creditCards":[{"type":"PRIMARY","description":"VISA","number":"4545-6767-8989-0000","expiration":"7/21","nameOnCard":"Mary Smith"}],"credentials":{"username":"msmith445","password":"S*$475hf&*dddFFG3"}}
```

The `orders.order.fulfill` sends pending orders (FulfillmentRequest) to fulfillment, via topic

```bash
kafka-topics.sh --create \
  --zookeeper zookeeper:2181 \
  --replication-factor 1 --partitions 1 \
  --topic orders.order.fulfill
```

## References

-   [Spring Kafka – Consumer and Producer Example](https://memorynotfound.com/spring-kafka-consume-producer-example/)
-   [Spring Kafka - JSON Serializer Deserializer Example](https://www.codenotfound.com/spring-kafka-json-serializer-deserializer-example.html)
-   [Spring for Apache Kafka: 2.1.6.RELEASE](https://docs.spring.io/spring-kafka/reference/html/index.html)
-   [Spring Data MongoDB - Reference Documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
