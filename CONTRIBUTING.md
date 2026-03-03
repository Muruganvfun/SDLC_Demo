# Contributing to Order Management System

Thank you for your interest in contributing to the Order Management System (OMS)! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Branch Naming Convention](#branch-naming-convention)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing Requirements](#testing-requirements)

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the issue, not the person
- Help others learn and grow

## Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.9+
- Docker (optional, for containerized development)

### Setting Up Development Environment

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd SDLC_Demo
   ```

2. Build the backend:
   ```bash
   cd backend
   mvn clean install -DskipTests
   ```

3. Install frontend dependencies:
   ```bash
   cd frontend
   npm install
   ```

4. Run services locally (see `docs/06-runbook.md`)

## Development Workflow

We follow a **GitFlow-inspired** workflow:

```
main (production)
  │
  ├── develop (integration branch)
  │     │
  │     ├── feature/OMS-123-add-payment-method
  │     ├── feature/OMS-124-user-profile
  │     └── bugfix/OMS-125-fix-login-error
  │
  ├── release/v1.1.0
  │
  └── hotfix/v1.0.1-critical-fix
```

### Branch Types

| Branch Type | Naming Pattern | Purpose |
|-------------|----------------|---------|
| `main` | `main` | Production-ready code |
| `develop` | `develop` | Integration branch |
| `feature` | `feature/OMS-XXX-description` | New features |
| `bugfix` | `bugfix/OMS-XXX-description` | Bug fixes |
| `hotfix` | `hotfix/vX.X.X-description` | Critical production fixes |
| `release` | `release/vX.X.X` | Release preparation |

## Branch Naming Convention

Format: `<type>/OMS-<ticket-number>-<short-description>`

**Examples:**
- `feature/OMS-101-add-payment-gateway`
- `bugfix/OMS-102-fix-order-validation`
- `hotfix/v1.0.1-security-patch`

**Rules:**
- Use lowercase
- Use hyphens to separate words
- Keep descriptions short (3-5 words)
- Always include ticket number when available

## Commit Message Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format
```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### Types
| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Code style (formatting, semicolons, etc.) |
| `refactor` | Code refactoring |
| `perf` | Performance improvement |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks |
| `ci` | CI/CD changes |

### Scopes
- `auth` - Auth service
- `catalog` - Catalog service
- `order` - Order service
- `inventory` - Inventory service
- `payment` - Payment service
- `shipping` - Shipping service
- `gateway` - API Gateway
- `frontend` - Frontend application
- `common` - Common library
- `infra` - Infrastructure

### Examples
```bash
feat(order): add order cancellation endpoint

fix(auth): resolve token expiration issue

docs(readme): update installation instructions

test(catalog): add integration tests for product API

chore(deps): update Spring Boot to 3.2.4
```

## Pull Request Process

### Before Creating a PR

1. **Update your branch:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout your-branch
   git rebase develop
   ```

2. **Run tests:**
   ```bash
   # Backend
   cd backend && mvn test
   
   # Frontend
   cd frontend && npm test
   ```

3. **Check code quality:**
   ```bash
   cd backend && mvn sonar:sonar
   ```

### PR Requirements

- [ ] Branch is up to date with `develop`
- [ ] All tests pass
- [ ] Code coverage maintained (minimum 60%)
- [ ] No SonarQube critical issues
- [ ] Documentation updated (if needed)
- [ ] PR description explains changes

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Feature
- [ ] Bug fix
- [ ] Documentation
- [ ] Refactoring

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings introduced
```

### Review Process

1. Create PR against `develop` branch
2. Request review from at least 1 team member
3. Address review feedback
4. Squash and merge when approved

## Code Style Guidelines

### Java (Backend)

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok annotations to reduce boilerplate
- Maximum line length: 120 characters
- Use meaningful variable/method names

```java
// Good
public OrderResponse createOrder(CreateOrderRequest request, String userId) {
    log.info("Creating order for user: {}", userId);
    // ...
}

// Bad
public OrderResponse create(CreateOrderRequest r, String u) {
    // ...
}
```

### TypeScript (Frontend)

- Follow [Airbnb JavaScript Style Guide](https://airbnb.io/javascript/)
- Use TypeScript strict mode
- Prefer functional components with hooks
- Use meaningful component/variable names

```typescript
// Good
const ProductListScreen: React.FC<Props> = ({ navigation }) => {
  const [products, setProducts] = useState<Product[]>([]);
  // ...
};

// Bad
const PLS = (p: any) => {
  const [d, setD] = useState([]);
  // ...
};
```

## Testing Requirements

### Backend Testing

| Test Type | Coverage Target | Location |
|-----------|----------------|----------|
| Unit Tests | 70% | `src/test/java/**/service/*Test.java` |
| Integration Tests | Key flows | `src/test/java/**/integration/*Test.java` |

**Running tests:**
```bash
# All tests
mvn test

# With coverage
mvn test jacoco:report

# Specific module
mvn test -pl auth-service
```

### Frontend Testing

| Test Type | Coverage Target | Location |
|-----------|----------------|----------|
| Unit Tests | 60% | `src/__tests__/*.test.ts(x)` |

**Running tests:**
```bash
# All tests
npm test

# With coverage
npm test -- --coverage

# Watch mode
npm test -- --watch
```

## Questions?

- Check existing documentation in `/docs`
- Create an issue for questions
- Reach out to the team lead

---

Happy Contributing! 🚀
