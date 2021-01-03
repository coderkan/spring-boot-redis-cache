# Spring Boot Redis Cache


Context:

  - [**Getting Started**](#getting-started)
  - [**Maven Dependencies**](#maven-dependencies)
  - [**Redis Configuration**](#redis-configuration)
  - [**Spring Service**](#spring-service)
  - [**Docker & Docker Compose**](#docker-docker-compose)
  - [**Build & Run Application**](#build-run-application)
  - [**Endpoints with Swagger**](#endpoints-with-swagger)
  - [**Demo**](#demo)


## Getting Started

In this project, I used Redis for caching with Spring Boot.
When you send any request to get all customers or customer by id, you will wait 3 seconds if Redis has no related data.


## Maven Dependencies


```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
</dependency>
		
```

## Redis Configuration

```java
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableCaching
public class RedisConfig {

	@Autowired
	private CacheManager cacheManager;

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Serializable> template = new RedisTemplate<>();
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		RedisCacheConfiguration redisCacheConfiguration = config
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory).cacheDefaults(redisCacheConfiguration)
				.build();
		return redisCacheManager;
	}

}
```


## Spring Service

Spring Boot Customer Service Implementation will be like below class.
I used Spring Boot Cache @Annotaions for caching.

These are:

* `@Cacheable`
* `@CacheEvict`
* `@Caching`
* `@CachceConfig`
	

```java
@Service
@CacheConfig(cacheNames = "customerCache")
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Cacheable(cacheNames = "customers")
	@Override
	public List<Customer> getAll() {
		waitSomeTime();
		return this.customerRepository.findAll();
	}

	@CacheEvict(cacheNames = "customers", allEntries = true)
	@Override
	public Customer add(Customer customer) {
		return this.customerRepository.save(customer);
	}

	@CacheEvict(cacheNames = "customers", allEntries = true)
	@Override
	public Customer update(Customer customer) {
		Optional<Customer> optCustomer = this.customerRepository.findById(customer.getId());
		if (!optCustomer.isPresent())
			return null;
		Customer repCustomer = optCustomer.get();
		repCustomer.setName(customer.getName());
		repCustomer.setContactName(customer.getContactName());
		repCustomer.setAddress(customer.getAddress());
		repCustomer.setCity(customer.getCity());
		repCustomer.setPostalCode(customer.getPostalCode());
		repCustomer.setCountry(customer.getCountry());
		return this.customerRepository.save(repCustomer);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "customer", key = "#id"),
			@CacheEvict(cacheNames = "customers", allEntries = true) })
	@Override
	public void delete(long id) {
		this.customerRepository.deleteById(id);
	}

	@Cacheable(cacheNames = "customer", key = "#id", unless = "#result == null")
	@Override
	public Customer getCustomerById(long id) {
		waitSomeTime();
		return this.customerRepository.findById(id).orElse(null);
	}

	private void waitSomeTime() {
		System.out.println("Long Wait Begin");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Long Wait End");
	}

}
```

## Docker & Docker Compose


Dockerfile:

```
FROM openjdk:8
ADD ./target/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar /usr/src/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar
WORKDIR usr/src
ENTRYPOINT ["java","-jar", "spring-boot-redis-cache-0.0.1-SNAPSHOT.jar"]
```

Docker compose file:


docker-compose.yml

```yml
version: '3'

services:
  db:
    image: "postgres"
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ekoloji
  cache:
    image: "redis"
    ports: 
      - "6379:6379"
    environment:
      - ALLOW_EMPTY_PASSWORD=yes
      - REDIS_DISABLE_COMMANDS=FLUSHDB,FLUSHALL
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db/postgres
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ekoloji
      SPRING_REDIS_HOST: cache
      SPRING_REDIS_PORT: 6379
    depends_on:
      - db
      - cache
```

## Build & Run Application

* Build Java Jar.

```shell
 $ mvn clean install
```

*  Docker Compose Build and Run

```shell
$ docker-compose build --no-cache
$ docker-compose up --force-recreate

```

After running the application you can visit `http://localhost:8080`.	

## Endpoints with Swagger


You can see the endpoint in `http://localhost:8080/swagger-ui.html` page.
I used Swagger for visualization endpoints.


![Endpoints](assets/endpoints.png)


## Demo

<div align="center">
  <a href="https://www.youtube.com/watch?v=4yr4JLRK6MM"><img src="https://img.youtube.com/vi/4yr4JLRK6MM/0.jpg" alt="Spring Boot + Redis + PostgreSQL Caching"></a>
</div>

