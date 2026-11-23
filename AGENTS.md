# AGENTS.md — Jenkins Built On Column Plugin (`built-on-column`)

## Overview
- **Plugin ID**: `built-on-column`
- **Repository**: `jenkinsci/built-on-column-plugin`
- **Java Package**: `org.jenkins.plugins.builton`
- **Jenkins Baseline**: `2.479.3` (`org.jenkins-ci.plugins:plugin:6.x`, `bom-2.479.x`)

Provides `BuiltOnColumn` (`hudson.views.ListViewColumn`) to render the node(s) where each job's last build ran in Jenkins list views.

---

## Build, Test & Verification Commands

### Prerequisites
- **JDK**: Java 17, 21, or 25
- **Maven**: Apache Maven `3.9+`

### Core Maven Commands
```bash
# Format code & POM, then run full verification (tests, InjectedTest, SpotBugs, Spotless, Enforcer)
mvn spotless:apply clean verify

# Run unit/integration tests quickly (skipping InjectedTest during iteration)
mvn test -Dtest=!InjectedTest

# Run SpotBugs and formatting checks directly
mvn spotbugs:check spotless:check
```

---

## Architecture & Packaging Guardrails

1. **Zero Mandatory Runtime Plugin Dependencies**:
   - `workflow-api` and `workflow-job` are declared `<optional>true</optional>` in `pom.xml`.
   - Never reference `org.jenkinsci.plugins.workflow.*` types directly from `BuiltOnColumn` signatures or unconditional code paths. Keep Pipeline class loading isolated inside `BuiltOnColumn.PipelineNodeResolver` behind `isPipelinePluginInstalled()`.
2. **Safe Rendering for Missing Nodes & Unbuilt Jobs**:
   - Ephemeral/deleted agents (`Node == null` with non-blank name) must render as plain text (`searchUrl == null`), and unbuilt jobs (`lastBuild == null`) must render an empty cell without broken `<a>` links.
3. **JUnit 5 Exclusively**:
   - Write all tests with JUnit 5 (`org.junit.jupiter.api.*`) and `@WithJenkins` (`org.jvnet.hudson.test.junit.jupiter.WithJenkins`). JUnit 4 imports are banned by Maven Enforcer.
4. **JEP-229 Continuous Delivery**:
   - `pom.xml` uses `<version>${changelist}</version>` (`999999-SNAPSHOT`) with `-Dchangelist.format=%d.v%s` in `.mvn/maven.config`. Releases are automated via `.github/workflows/cd.yaml`.
