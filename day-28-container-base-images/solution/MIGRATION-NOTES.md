# Migration notes: Boot 2.7.18 → 3.5.16

Read from `build/reports/rewrite/rewrite.patch` (`./gradlew rewriteDryRun`, recipe `UpgradeSpringBoot_3_5`). Nothing applied yet.

## What the patch changes

| File | Change |
|---|---|
| `build.gradle.kts` | Spring Boot plugin `2.7.18` → `3.5.16` |
| `build.gradle.kts` | Toolchain `JavaLanguageVersion.of(11)` → `of(17)` |
| `model/Account.java` | `javax.persistence.*` → `jakarta.persistence.*` |
| `dto/CreateAccountRequest.java`, `controller/AccountController.java` | `javax.validation.*` → `jakarta.validation.*` |
| `config/SecurityConfig.java` | No more `WebSecurityConfigurerAdapter`: a `@Bean SecurityFilterChain filterChain(HttpSecurity http)` that ends with `return http.build();` |
| `config/SecurityConfig.java` | `authorizeRequests()` → `authorizeHttpRequests(...)`, `antMatchers` → `requestMatchers`, and the `.and()` chains become lambdas (`csrf(csrf -> csrf.disable())`, `httpBasic(withDefaults())`) |

## What looks wrong or missing

- **An odd new line:** `runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")`. We don't use JAXB anywhere, and it's a `javax` library in a `jakarta` upgrade. Remove it after applying.
- **Our `extra[...]` overrides aren't touched.** `spring-security.version = 5.7.13`, `spring-framework.version = 5.3.39`, `tomcat.version = 9.0.123`, `logback.version = 1.2.13` and `snakeyaml.version = 2.2` would still pin Boot 2.7-era versions under Boot 3.5. They must go.
- **`gradle.lockfile` isn't touched.** It still lists the Boot 2.7 versions, so the first build will fail until it's regenerated.
- **Tests aren't touched.** `AccountControllerTest` still uses `@MockBean`, which is deprecated in Boot 3.x and removed in Boot 4.
- **Behaviour changes aren't flagged.** The patch only rewrites code; it can't tell us whether Spring Security 6 behaves the same. Our tests (`SecurityHardeningTest`, `H2ConsoleTest`) will have to tell us.
