# App Modernization at Scale
## Legacy Java to Modern Java/Spring Boot Migrations
### Factory.AI Autonomous Migration Framework

---

## 1. Executive Summary

**App Modernization at Scale** leverages Factory.AI's autonomous development capabilities to transform legacy Java applications into modern, cloud-native systems. This use case demonstrates how AI-powered Droids can accelerate migration projects by 10x while ensuring quality, security, and maintainability.

### Migration Overview

| Phase | Description | Factory.AI Capabilities |
|-------|-------------|-------------------------|
| ASSESS | Codebase Analysis | Pattern detection, dependency mapping, complexity scoring |
| PLAN | Migration Strategy | Sprint planning, risk assessment, component prioritization |
| MIGRATE | AI-Powered Refactoring | Multi-file refactoring, framework upgrades, test generation |
| VALIDATE | Automated Testing | Regression testing, performance benchmarks, security scanning |
| DEPLOY | Phased Rollout | Blue/green deployment, feature flags, monitoring setup |

---

## 2. Migration Scope

### 2.1 Source Stack (Legacy)

| Component | Legacy Technology | Challenges |
|-----------|-------------------|------------|
| **Java Version** | Java 8/11 | Missing modern APIs, security vulnerabilities |
| **Framework** | Spring MVC, Struts 1.x | XML configuration, outdated patterns |
| **Data Access** | JDBC, Hibernate HBM | Manual transactions, verbose code |
| **Build** | Ant, Maven 2 | Legacy plugins, WAR packaging |
| **Testing** | JUnit 4, manual tests | Low coverage, brittle tests |
| **Deployment** | WebLogic/WebSphere | Manual scaling, complex configuration |

### 2.2 Target Stack (Modern)

| Component | Modern Technology | Benefits |
|-----------|-------------------|----------|
| **Java Version** | Java 17/21 | Records, pattern matching, virtual threads |
| **Framework** | Spring Boot 3 | Auto-configuration, embedded server |
| **Data Access** | Spring Data JPA | Repository pattern, declarative queries |
| **Build** | Maven 3/Gradle | Dependency management, native compilation |
| **Testing** | JUnit 5, Mockito 5 | Parameterized tests, better assertions |
| **Deployment** | Docker/Kubernetes | Container orchestration, auto-scaling |

---

## 3. Migration Phases

### Phase 1: ASSESS (Weeks 1-2)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PHASE 1: ASSESS                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  INPUT                    PROCESS                    OUTPUT          │
│  ─────                    ───────                    ──────          │
│                                                                      │
│  • Legacy Codebase        Factory.AI Analysis:       • Complexity    │
│  • Build Configs          ─────────────────          │  Report       │
│  • Dependencies           • Static analysis          • Dependency    │
│  • Documentation          • Pattern detection        │  Graph        │
│                           • Dependency mapping       • Migration     │
│                           • Complexity scoring       │  Roadmap      │
│                           • Risk identification      • Risk Matrix   │
│                                                                      │
│  FACTORY.AI CAPABILITIES:                                            │
│  • Automated codebase scanning (1000s of files)                      │
│  • Legacy pattern identification (EJB, Struts, etc.)                 │
│  • Dependency vulnerability assessment                               │
│  • Technical debt quantification                                     │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Activities:**
- [ ] Scan entire codebase with Factory.AI
- [ ] Identify legacy patterns and anti-patterns
- [ ] Map all dependencies and their versions
- [ ] Assess security vulnerabilities
- [ ] Calculate migration complexity score
- [ ] Generate comprehensive assessment report

**Success Criteria:**
- Complete codebase analysis report
- Dependency graph with vulnerability flags
- Prioritized list of migration targets
- Risk assessment matrix

---

### Phase 2: PLAN (Weeks 3-4)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PHASE 2: PLAN                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  INPUT                    PROCESS                    OUTPUT          │
│  ─────                    ───────                    ──────          │
│                                                                      │
│  • Assessment Report      Strategy Development:      • Migration     │
│  • Business Goals         ─────────────────────      │  Plan         │
│  • Timeline               • Component grouping       • Sprint        │
│  • Team Capacity          • Dependency ordering      │  Backlog      │
│                           • Risk mitigation          • Architecture  │
│                           • Resource allocation      │  Design       │
│                                                      • Rollback Plan │
│                                                                      │
│  MIGRATION STRATEGIES:                                               │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ STRANGLER FIG    → Gradual replacement of legacy modules    │    │
│  │ BIG BANG         → Complete rewrite (high risk)             │    │
│  │ PHASED           → Module-by-module migration (recommended) │    │
│  │ PARALLEL RUN     → Run both systems simultaneously          │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Activities:**
- [ ] Define migration strategy (Strangler Fig recommended)
- [ ] Prioritize components by business value and risk
- [ ] Create sprint backlog with user stories
- [ ] Design target architecture
- [ ] Plan rollback procedures
- [ ] Set up CI/CD pipeline for modernization

**Success Criteria:**
- Approved migration plan
- Sprint backlog ready
- Architecture documentation complete
- Rollback procedures tested

---

### Phase 3: MIGRATE (Weeks 5-12)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PHASE 3: MIGRATE                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  FACTORY.AI DROID CAPABILITIES                                       │
│  ────────────────────────────                                        │
│                                                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │
│  │ CODE ANALYSIS   │  │ REFACTORING     │  │ TEST GENERATION │      │
│  │ ─────────────   │  │ ───────────     │  │ ─────────────── │      │
│  │ • Pattern detect│  │ • Multi-file    │  │ • Unit tests    │      │
│  │ • Dependency map│  │ • Framework     │  │ • Integration   │      │
│  │ • Impact assess │  │ • API upgrades  │  │ • Mocking       │      │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘      │
│                                                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │
│  │ CONFIG MIGRATE  │  │ BUILD UPDATE    │  │ DOCUMENTATION   │      │
│  │ ──────────────  │  │ ────────────    │  │ ─────────────── │      │
│  │ • XML to YAML   │  │ • pom.xml       │  │ • API docs      │      │
│  │ • Properties    │  │ • Dependencies  │  │ • Migration log │      │
│  │ • Annotations   │  │ • Plugins       │  │ • Code comments │      │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘      │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

#### 3.1 Java Version Upgrade

**From Java 8/11 to Java 17/21:**

| Pattern | Before | After |
|---------|--------|-------|
| Anonymous Classes | `new Runnable() {...}` | `() -> {...}` |
| Null Checks | `if (x != null && x.y != null)` | `Optional.ofNullable(x).map(...)` |
| Data Classes | POJO with getters/setters | `record Person(String name, int age)` |
| Switch | `switch (x) { case 1: ... break; }` | `switch (x) { case 1 -> ...; }` |
| String Handling | String concatenation | Text blocks `"""..."""` |
| Type Checks | `if (obj instanceof String) { String s = (String) obj; }` | `if (obj instanceof String s)` |

**Factory.AI Automation:**
- Scans codebase for Java 8/11 patterns
- Generates modernized code using Java 17/21 features
- Creates PRs with detailed migration notes
- Updates build configuration for new Java version

---

#### 3.2 Spring Framework Migration

**From Spring MVC to Spring Boot 3:**

| Area | Before | After |
|------|--------|-------|
| Configuration | `applicationContext.xml` | `application.yml` + `@Configuration` |
| Web Setup | `web.xml` + DispatcherServlet | `@SpringBootApplication` |
| Bean Wiring | `<bean id="..." class="...">` | `@Component`, `@Service`, `@Repository` |
| Properties | `.properties` files | `application.yml` with profiles |
| Server | External Tomcat/WebLogic | Embedded Tomcat |
| Security | XML security config | `@EnableWebSecurity` + Java config |

**Factory.AI Automation:**
- Converts XML beans to annotation-based configuration
- Migrates web.xml to Spring Boot auto-configuration
- Updates security configuration to modern patterns
- Generates application.yml from properties

---

#### 3.3 Data Access Modernization

**From JDBC/Hibernate HBM to Spring Data JPA:**

| Area | Before | After |
|------|--------|-------|
| Entity Mapping | `.hbm.xml` files | `@Entity`, `@Table`, `@Column` |
| Repositories | DAO pattern with boilerplate | `extends JpaRepository<T, ID>` |
| Queries | HQL strings / Criteria API | Method naming conventions / `@Query` |
| Transactions | Manual session management | `@Transactional` |
| Connection Pool | C3P0 / DBCP | HikariCP (default) |

**Factory.AI Automation:**
- Converts HBM XML to JPA annotations
- Generates Spring Data repositories
- Migrates HQL queries to JPQL or derived queries
- Updates transaction management

---

#### 3.4 Testing Modernization

**From JUnit 4 to JUnit 5:**

| Area | Before (JUnit 4) | After (JUnit 5) |
|------|------------------|-----------------|
| Test Annotation | `@Test` (org.junit) | `@Test` (org.junit.jupiter) |
| Lifecycle | `@Before`, `@After` | `@BeforeEach`, `@AfterEach` |
| Class Lifecycle | `@BeforeClass`, `@AfterClass` | `@BeforeAll`, `@AfterAll` |
| Extensions | `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| Assertions | `Assert.assertEquals(...)` | `Assertions.assertEquals(...)` |
| Exceptions | `@Test(expected = ...)` | `assertThrows(...)` |

**Factory.AI Automation:**
- Converts JUnit 4 tests to JUnit 5
- Generates missing unit tests for 80%+ coverage
- Creates integration tests with `@SpringBootTest`
- Updates mocking to Mockito 5

---

#### 3.5 Build System Migration

**From Ant/Maven 2 to Maven 3/Gradle:**

| Area | Before | After |
|------|--------|-------|
| Build File | `build.xml` | `pom.xml` (Maven) / `build.gradle` |
| Dependencies | Manual JAR management | Dependency management with BOM |
| Plugins | Ant tasks | Maven/Gradle plugins |
| Packaging | WAR with external libs | Executable JAR / Native image |
| Profiles | Separate build files | Maven profiles / Gradle variants |

**Factory.AI Automation:**
- Converts Ant build.xml to Maven/Gradle
- Migrates dependencies with version management
- Updates plugins to modern equivalents
- Configures Spring Boot packaging

---

### Phase 4: VALIDATE (Weeks 13-14)

```
┌─────────────────────────────────────────────────────────────────────┐
│                       PHASE 4: VALIDATE                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  TEST PYRAMID                                                        │
│  ────────────                                                        │
│                                                                      │
│                    ┌───────────────┐                                 │
│                    │   E2E Tests   │  10%                            │
│                   ─┼───────────────┼─                                │
│                  │ │ Integration   │ │  20%                          │
│                 ─┼─┼───────────────┼─┼─                              │
│                │   │  Unit Tests   │   │  70%                        │
│               ─────┼───────────────┼─────                            │
│                    └───────────────┘                                 │
│                                                                      │
│  VALIDATION CHECKLIST:                                               │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ ✓ Unit test coverage > 80%                                  │    │
│  │ ✓ Integration tests passing                                 │    │
│  │ ✓ Performance benchmarks meet SLA                           │    │
│  │ ✓ Security scan clean (no critical/high CVEs)               │    │
│  │ ✓ API compatibility verified                                │    │
│  │ ✓ Database migration validated                              │    │
│  │ ✓ Configuration reviewed                                    │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Activities:**
- [ ] Run comprehensive test suite
- [ ] Execute performance benchmarks
- [ ] Perform security scanning
- [ ] Validate API compatibility
- [ ] Test database migrations
- [ ] Conduct code review

**Success Criteria:**
- All tests passing
- 80%+ code coverage
- No critical security vulnerabilities
- Performance within SLA
- API backward compatibility maintained

---

### Phase 5: DEPLOY (Weeks 15-16)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PHASE 5: DEPLOY                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  DEPLOYMENT STRATEGY: BLUE/GREEN                                     │
│  ───────────────────────────────                                     │
│                                                                      │
│  ┌─────────────┐         ┌─────────────┐                            │
│  │   BLUE      │         │   GREEN     │                            │
│  │  (Legacy)   │         │  (Modern)   │                            │
│  │             │    ──▶  │             │                            │
│  │  Java 8     │         │  Java 17    │                            │
│  │  Spring MVC │         │  Spring Boot│                            │
│  └─────────────┘         └─────────────┘                            │
│        │                       │                                     │
│        └───────────┬───────────┘                                     │
│                    │                                                 │
│              ┌─────┴─────┐                                           │
│              │   LOAD    │                                           │
│              │  BALANCER │                                           │
│              └───────────┘                                           │
│                                                                      │
│  ROLLOUT PHASES:                                                     │
│  1. Deploy Green (modern) environment                                │
│  2. Route 10% traffic to Green                                       │
│  3. Monitor metrics and errors                                       │
│  4. Gradually increase to 100%                                       │
│  5. Decommission Blue (legacy)                                       │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Activities:**
- [ ] Deploy to staging environment
- [ ] Run smoke tests
- [ ] Deploy with feature flags
- [ ] Gradual traffic shifting
- [ ] Monitor application health
- [ ] Document rollback procedures

**Success Criteria:**
- Zero-downtime deployment
- Successful canary release
- All health checks passing
- Monitoring dashboards operational
- Rollback tested and documented

---

## 4. Factory.AI Migration Capabilities

### 4.1 Automated Code Analysis

| Capability | Description | Benefit |
|------------|-------------|---------|
| **Pattern Detection** | Identifies legacy patterns (EJB, Struts, JDBC) | Accurate migration scope |
| **Dependency Mapping** | Maps all dependencies and transitive deps | Risk identification |
| **Complexity Scoring** | Calculates migration complexity per module | Prioritization |
| **Impact Analysis** | Determines blast radius of changes | Risk mitigation |

### 4.2 Multi-File Refactoring

| Capability | Description | Benefit |
|------------|-------------|---------|
| **Cross-File Updates** | Updates all related files atomically | Consistency |
| **Rename Propagation** | Propagates renames across codebase | Accuracy |
| **Import Management** | Updates imports automatically | Clean code |
| **Reference Updates** | Updates all references to changed APIs | Completeness |

### 4.3 Test Generation

| Capability | Description | Benefit |
|------------|-------------|---------|
| **Unit Test Generation** | Creates JUnit 5 tests for all classes | Coverage |
| **Mock Generation** | Generates Mockito mocks for dependencies | Isolation |
| **Edge Case Detection** | Identifies and tests edge cases | Robustness |
| **Integration Tests** | Creates `@SpringBootTest` tests | Validation |

### 4.4 Configuration Migration

| Capability | Description | Benefit |
|------------|-------------|---------|
| **XML to YAML** | Converts Spring XML to application.yml | Modern config |
| **Properties Migration** | Converts .properties to YAML | Consistency |
| **Annotation Generation** | Generates `@Configuration` classes | Type safety |
| **Profile Setup** | Creates environment-specific profiles | Flexibility |

---

## 5. Migration Patterns Catalog

### 5.1 Java Language Patterns

```java
// BEFORE: Anonymous class (Java 8)
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// AFTER: Lambda (Java 17)
Runnable task = () -> System.out.println("Hello");
```

```java
// BEFORE: Data class (Java 8)
public class Person {
    private final String name;
    private final int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
    // equals, hashCode, toString...
}

// AFTER: Record (Java 17)
public record Person(String name, int age) {}
```

### 5.2 Spring Framework Patterns

```xml
<!-- BEFORE: XML Configuration -->
<beans>
    <bean id="userService" class="com.example.UserServiceImpl">
        <property name="userRepository" ref="userRepository"/>
    </bean>
    <bean id="userRepository" class="com.example.JdbcUserRepository">
        <property name="dataSource" ref="dataSource"/>
    </bean>
</beans>
```

```java
// AFTER: Java Configuration
@Configuration
public class AppConfig {
    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }
    
    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new JdbcUserRepository(dataSource);
    }
}
```

### 5.3 Data Access Patterns

```xml
<!-- BEFORE: Hibernate HBM -->
<hibernate-mapping>
    <class name="com.example.User" table="users">
        <id name="id" column="id">
            <generator class="native"/>
        </id>
        <property name="name" column="name"/>
        <property name="email" column="email"/>
    </class>
</hibernate-mapping>
```

```java
// AFTER: JPA Annotations
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByNameContaining(String name);
}
```

### 5.4 Testing Patterns

```java
// BEFORE: JUnit 4
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    public void testFindUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        User result = userService.findUser(1L);
        assertNotNull(result);
    }
    
    @Test(expected = UserNotFoundException.class)
    public void testFindUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        userService.findUser(1L);
    }
}
```

```java
// AFTER: JUnit 5
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void findUser_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        User result = userService.findUser(1L);
        assertNotNull(result);
    }
    
    @Test
    void findUser_shouldThrowException_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUser(1L));
    }
    
    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    void findUser_shouldWork_forMultipleIds(Long id) {
        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));
        assertDoesNotThrow(() -> userService.findUser(id));
    }
}
```

---

## 6. Metrics & KPIs

### 6.1 Migration Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Migration Speed** | 10x faster | vs. manual migration |
| **Code Coverage** | 80%+ | Auto-generated tests |
| **Defect Rate** | < 1% | Post-migration defects |
| **Rollback Rate** | 0% | Successful deployments |
| **Performance** | Same or better | Response time benchmarks |

### 6.2 Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Test Coverage** | > 80% | JaCoCo coverage report |
| **Code Quality** | A rating | SonarQube analysis |
| **Security Score** | No critical/high | Vulnerability scan |
| **Technical Debt** | < 5% | SonarQube debt ratio |
| **Documentation** | 100% | API docs coverage |

### 6.3 Cost Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Cost Reduction** | 60% | vs. manual migration |
| **Time to Market** | 50% faster | Sprint completion rate |
| **Developer Productivity** | +40% | Story points per sprint |
| **Maintenance Reduction** | 30% | Post-migration support |

---

## 7. Implementation Checklist

### Pre-Migration

- [ ] Complete codebase assessment
- [ ] Define migration strategy
- [ ] Set up CI/CD pipeline
- [ ] Configure Factory.AI workspace
- [ ] Create sprint backlog
- [ ] Set up monitoring/alerting

### During Migration

- [ ] Java version upgrade
- [ ] Spring Framework migration
- [ ] Data access modernization
- [ ] Build system update
- [ ] Test migration and generation
- [ ] Configuration migration
- [ ] API modernization
- [ ] Security hardening

### Post-Migration

- [ ] Run full test suite
- [ ] Perform security scan
- [ ] Execute performance benchmarks
- [ ] Deploy to staging
- [ ] Conduct UAT
- [ ] Deploy to production
- [ ] Monitor and optimize
- [ ] Document lessons learned

---

## 8. Risk Mitigation

### Common Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Breaking Changes** | High | Comprehensive test coverage, phased rollout |
| **Data Migration Errors** | High | Backup strategy, validation scripts |
| **Performance Regression** | Medium | Performance benchmarks, load testing |
| **Security Vulnerabilities** | High | Security scanning, dependency updates |
| **Team Knowledge Gap** | Medium | Training, documentation, pair programming |
| **Schedule Overrun** | Medium | Buffer time, prioritization, parallel work |

---

## 9. Sample Pipeline Configuration

### Azure DevOps Pipeline for Migration

```yaml
# azure-pipelines-modernization.yml
trigger:
  branches:
    include:
      - modernization/*

variables:
  JAVA_VERSION: '17'
  SPRING_BOOT_VERSION: '3.2.0'

stages:
  - stage: Analyze
    displayName: 'Analyze Legacy Code'
    jobs:
      - job: FactoryAIAnalysis
        steps:
          - task: FactoryAI@1
            displayName: 'Run Factory.AI Analysis'
            inputs:
              action: 'analyze'
              targetPath: '$(Build.SourcesDirectory)'
              outputPath: '$(Build.ArtifactStagingDirectory)/analysis'

  - stage: Migrate
    displayName: 'Execute Migration'
    dependsOn: Analyze
    jobs:
      - job: MigrationJob
        steps:
          - task: FactoryAI@1
            displayName: 'Run Factory.AI Migration'
            inputs:
              action: 'migrate'
              migrationPlan: '$(Build.ArtifactStagingDirectory)/analysis/plan.json'
              
          - task: Maven@3
            displayName: 'Build Modernized Code'
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'clean compile'
              javaHomeOption: 'JDKVersion'
              jdkVersionOption: '1.17'

  - stage: Test
    displayName: 'Validate Migration'
    dependsOn: Migrate
    jobs:
      - job: TestJob
        steps:
          - task: Maven@3
            displayName: 'Run Tests'
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'test'
              
          - task: PublishCodeCoverageResults@1
            inputs:
              codeCoverageTool: 'JaCoCo'
              summaryFileLocation: '**/site/jacoco/jacoco.xml'

  - stage: SecurityScan
    displayName: 'Security Validation'
    dependsOn: Test
    jobs:
      - job: SecurityJob
        steps:
          - task: dependency-check@1
            displayName: 'OWASP Dependency Check'
            
          - task: SonarQubePrepare@5
            displayName: 'Prepare SonarQube'
          
          - task: Maven@3
            displayName: 'SonarQube Analysis'
            inputs:
              goals: 'sonar:sonar'

  - stage: Deploy
    displayName: 'Deploy Modernized App'
    dependsOn: SecurityScan
    condition: succeeded()
    jobs:
      - deployment: DeployJob
        environment: 'modernization-staging'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: Docker@2
                  displayName: 'Build Container'
                  inputs:
                    command: 'buildAndPush'
                    
                - task: KubernetesManifest@0
                  displayName: 'Deploy to K8s'
```

---

## 10. Appendix

### A. Glossary

| Term | Definition |
|------|------------|
| **Strangler Fig** | Migration pattern where legacy is gradually replaced |
| **Blue/Green** | Deployment strategy with two identical environments |
| **Feature Flag** | Toggle to enable/disable features without deployment |
| **Technical Debt** | Cost of rework caused by choosing quick solutions |
| **Droid** | Factory.AI's autonomous AI agent |

### B. Tool Versions

| Tool | Version | Purpose |
|------|---------|---------|
| Factory.AI | Latest | AI-powered migration |
| Java | 17/21 | Target runtime |
| Spring Boot | 3.2.x | Application framework |
| JUnit | 5.x | Testing framework |
| Maven/Gradle | 3.9+/8.x | Build tools |

### C. Support Contacts

| Role | Contact | Responsibility |
|------|---------|----------------|
| Factory.AI Support | support@factory.ai | Migration assistance |
| DevOps Team | devops@company.com | Pipeline support |
| Architecture Team | arch@company.com | Design decisions |

---

**Document Version:** 1.0  
**Date:** March 2026  
**Author:** Factory.AI Migration Team

*This template provides a comprehensive guide for Java modernization projects using Factory.AI. Customize based on your specific legacy stack and target architecture.*
