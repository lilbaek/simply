Simply is a basic ORM-like framework designed to work with Java Records, without class generation, supporting the most common jakarta annotations.
Simply needs Spring Boot 3.2 or higher to work and currently only supports DB2.
Simply uses the new JdbcClient in Sprint Boot 3.2. It wraps the JdbcClient and provides a simple way to work with Java Records while having basic CRUD support.

# Simply might be for you in case:
* You like immutable objects and the simplicity of Java Records
* You know how to write SQL queries and care about performance
* You need basic CRUD operations
* You need Converter support
* You work with DB2
* Want a lib where you can read and understand the code

Although the Java industry offers very well maintained ORM solutions like Hibernate they tend not to work that well with Java Records.

# Features
* No need to generate classes
* Jakarta annotation support for @Table,  @Column, @Id, @Convert and @Transient
* SimpleCrudRepository for basic CRUD operations
* No need to write SQL for basic operations
* No dependency on Hibernate  
* Easy to write SQL tests without having to start a full Spring context (faster tests) using org.hsqldb:hsqldb

# Getting started

Add the dependency to your project:

```
implementation 'dk.bankdata.kfa:simply:XXX'
implementation 'jakarta.persistence:jakarta.persistence-api:XXX'
implementation 'org.springframework.boot:spring-boot-starter-jdbc'

// For testing
testRuntimeOnly "org.hsqldb:hsqldb"
```

jakarta.persistence-api version should match what you Spring Boot version is using. 

Configure your application.properties:

```
spring.datasource.url=db2-host
spring.datasource.driver-class-name=driver
spring.datasource.username=username
spring.datasource.password=password
simply.datasource.default_schema=schema
simply.show-sql=false
```

# Usage
Define your Java Record (See tests in the project):

```java
@Entity
@Table(name = "Post")
public record Post(
        @Column(name = "id")
        @Id
        String id,
        @Column(name = "title")
        String title,
        @Column(name = "enabled")
        @Convert(converter = YNToBooleanConverter.class)
        Boolean enabled,
        @Column(name = "date")
        LocalDate date,
        @Column(name = "type")
        @Convert(converter = PostTypeConverter.class)
        PostType type,
        @Column(name = "stars")
        BigDecimal stars,
        @Transient
        List<String> authorIds) {
}
```

Create a CRUD repository where the second parameter is the type of the primary key:

```java
@Repository
public class PostRepository extends SimpleCrudRepository<Post, String> {
    protected PostRepository(final DBClient dbClient) {
        super(dbClient);
    }
}
```
If you have a complex key you can define a composite key:

```java
public record PostIdEnabledClass(
                @Column(name = "id")
                String id,
                @Column(name = "enabled")
                @Convert(converter = YNToBooleanConverter.class)
                boolean enabled
) {
}
```

You can now use the repository to perform CRUD operations:

```java
repo.findById("1");
repo.deleteById("2");
repo.update(new Post(orgRecord.id(), "Updated", orgRecord.enabled(), orgRecord.date(), orgRecord.type(), orgRecord.stars(), List.of()));
repo.insert(new Post("9999", "Inserted", true, LocalDate.now(), PostType.ARTICLE, BigDecimal.ONE, List.of()));
```

And query for data using the DBClient in your repository:

```java
        final var record = client.sql("""
                        SELECT id
                        FROM {schema}POST
                        WHERE id = :id
                        """)
                .param("id", "1")
                .single(PostRecordFromQuery.class);
```
{schema} will be replaced with the schema defined in the application.properties.
To avoid SQL injection you should always use parameters in your queries.

The DBClient has a number of methods to query for data, like single, list

```java
    /**
     * Bind a named statement parameter for ":x" placeholder resolution, with each "x" name matching a ":x" placeholder in the SQL statement.
     * Params:
     * name – the parameter name value – the parameter value to bind
     */
    QuerySpec param(String name, @Nullable Object value);

    /**
     * Transforms a query result to a Java record or a primitive type.
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> T single(final Class<T> cls);

    /**
     * Transforms a query result to a Java record, a primitive type or null
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> T singleOrNull(final Class<T> cls);

    /**
     * Transforms a query result to a Java record or a primitive type wrapped in Optional.
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> Optional<T> optional(final Class<T> cls);

    /**
     * Transforms a query result to list of <T>
     * Supports @Column, @Convert and @Transient annotations for Java records
     *
     * @param cls result type
     */
    <T> List<T> list(final Class<T> cls);
```

# Testing
To get started with testing you can use the HSQLDB in-memory database. Add the dependency to your project and create a BaseSqlTest:
    
```java
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
@ContextConfiguration(classes = BaseSqlTest.DBClientTestConfiguration.class)
public abstract class BaseSqlTest {
    @Autowired
    protected DBClient dbClient;

    public static class DBClientTestConfiguration {
        public DBClientTestConfiguration() {
        }

        @Bean
        DataSource testDataSource() {
            return new SimpleDriverDataSource(new JDBCDriver(), "jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1", "SA", "");
        }

        @Bean
        DBClient testDbClient() {
            return new DBClient(JdbcClient.create(testDataSource()), new SchemaReplacer(""), new Logger(true));
        }
    }
}
```

This will setup the HSQLDB in-memory database and the DBClient for you to use in your tests. You can now write tests like this:

```java
@Sql(scripts = {"repospecttest.sql"})
public class SimpleRepositoryTest extends BaseSqlTest {
    private SimplePostRepository underTest;

    @BeforeEach
    void setUp() {
        underTest = new SimplePostRepository(dbClient);
    }

    @Test
    public void findById() {
        final var record = underTest.findById("1").get();
        assertEquals("1", record.id());
        assertTrue(record.enabled());
    }
}
```

If you want the will Spring Context for integration tests you can use the @SpringBootTest annotation. 
```java
@SpringBootTest
@Sql(scripts = {"repospecttest.sql"})
public class CrudRepositoryTest {
    @Autowired
    PostCrudRepository repo;

    @Test
    public void findById() {
        final var record = repo.findById("1").get();
        assertEquals("1", record.id());
        assertTrue(record.enabled());
    }
}
```
