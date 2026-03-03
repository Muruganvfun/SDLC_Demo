# Git Workflow & Branch Protection Guidelines

## Branch Structure

This project follows a **GitFlow-inspired** workflow:

```
main (production)
  │
  ├── develop (integration branch)
  │     │
  │     ├── feature/* (new features)
  │     ├── bugfix/* (bug fixes)
  │     └── refactor/* (code improvements)
  │
  ├── release/* (release preparation)
  │
  └── hotfix/* (critical production fixes)
```

## Branch Descriptions

| Branch | Purpose | Base Branch | Merges Into |
|--------|---------|-------------|-------------|
| `main` | Production-ready code | - | - |
| `develop` | Integration branch | `main` | `main` |
| `feature/*` | New features | `develop` | `develop` |
| `bugfix/*` | Bug fixes | `develop` | `develop` |
| `release/*` | Release preparation | `develop` | `main` + `develop` |
| `hotfix/*` | Critical fixes | `main` | `main` + `develop` |

## Branch Protection Rules (GitHub)

### `main` Branch

```yaml
Protection Rules:
  - Require pull request before merging: YES
  - Required approving reviews: 2
  - Dismiss stale reviews: YES
  - Require review from code owners: YES
  - Require status checks to pass:
    - build
    - unit-tests
    - integration-tests
    - sonarqube-quality-gate
  - Require branches to be up to date: YES
  - Require signed commits: RECOMMENDED
  - Include administrators: YES
  - Restrict pushes: Only release managers
  - Allow force pushes: NO
  - Allow deletions: NO
```

### `develop` Branch

```yaml
Protection Rules:
  - Require pull request before merging: YES
  - Required approving reviews: 1
  - Dismiss stale reviews: YES
  - Require status checks to pass:
    - build
    - unit-tests
  - Require branches to be up to date: YES
  - Include administrators: NO
  - Allow force pushes: NO
  - Allow deletions: NO
```

## Workflow Examples

### Feature Development

```bash
# 1. Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/OMS-123-add-wishlist

# 2. Make changes and commit
git add .
git commit -m "feat(catalog): add wishlist functionality"

# 3. Push and create PR
git push -u origin feature/OMS-123-add-wishlist
# Create PR to develop branch

# 4. After PR approval and merge, delete branch
git checkout develop
git pull origin develop
git branch -d feature/OMS-123-add-wishlist
```

### Release Process

```bash
# 1. Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# 2. Bump versions, update changelog
# Update pom.xml versions
# Update package.json version
git commit -m "chore(release): bump version to 1.2.0"

# 3. Create PR to main
git push -u origin release/v1.2.0
# Create PR to main branch

# 4. After merge to main, tag the release
git checkout main
git pull origin main
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0

# 5. Merge back to develop
git checkout develop
git merge main
git push origin develop
```

### Hotfix Process

```bash
# 1. Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/v1.1.1-critical-fix

# 2. Apply fix
git commit -m "fix(auth): resolve critical authentication bypass"

# 3. Create PRs to both main and develop
git push -u origin hotfix/v1.1.1-critical-fix
# Create PR to main (priority)
# Create PR to develop

# 4. Tag after merge to main
git checkout main
git pull origin main
git tag -a v1.1.1 -m "Hotfix v1.1.1"
git push origin v1.1.1
```

## Git Hooks (Recommended)

### Pre-commit Hook

Create `.git/hooks/pre-commit`:

```bash
#!/bin/sh

# Run backend tests for changed modules
CHANGED_JAVA=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")
if [ -n "$CHANGED_JAVA" ]; then
    echo "Running Java tests..."
    cd backend && mvn test -DskipITs || exit 1
fi

# Run frontend tests for changed files
CHANGED_TS=$(git diff --cached --name-only --diff-filter=ACM | grep -E "\.(ts|tsx)$")
if [ -n "$CHANGED_TS" ]; then
    echo "Running TypeScript tests..."
    cd frontend && npm test -- --watchAll=false || exit 1
fi

# Check for secrets
if git diff --cached | grep -iE "(password|secret|api_key|token).*=.*['\"]" | grep -v "test"; then
    echo "WARNING: Possible secrets detected in commit!"
    exit 1
fi
```

### Commit-msg Hook

Create `.git/hooks/commit-msg`:

```bash
#!/bin/sh

# Validate commit message format
commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci)(\([a-z-]+\))?: .{1,72}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "ERROR: Invalid commit message format!"
    echo "Expected: <type>(<scope>): <subject>"
    echo "Example: feat(order): add order cancellation endpoint"
    exit 1
fi
```

## Git Aliases (Recommended)

Add to `~/.gitconfig`:

```ini
[alias]
    # Status shortcuts
    s = status -sb
    
    # Branch shortcuts
    co = checkout
    br = branch
    
    # Feature branch helpers
    feature = "!f() { git checkout develop && git pull && git checkout -b feature/$1; }; f"
    bugfix = "!f() { git checkout develop && git pull && git checkout -b bugfix/$1; }; f"
    
    # Log visualization
    lg = log --oneline --graph --decorate -20
    
    # Sync with develop
    sync = "!git fetch origin && git rebase origin/develop"
    
    # Clean merged branches
    cleanup = "!git branch --merged develop | grep -v develop | xargs -n 1 git branch -d"
```

## Code Owners

Create `CODEOWNERS` file in repository root:

```
# Default owners
* @team-lead @tech-lead

# Backend services
/backend/auth-service/ @backend-team @security-team
/backend/catalog-service/ @backend-team
/backend/order-service/ @backend-team @order-team
/backend/payment-service/ @backend-team @payment-team
/backend/inventory-service/ @backend-team
/backend/shipping-service/ @backend-team
/backend/notification-service/ @backend-team
/backend/api-gateway/ @backend-team @platform-team
/backend/common/ @backend-team

# Frontend
/frontend/ @frontend-team

# Infrastructure
/docker-compose.yml @devops-team
/sonarqube/ @devops-team
/scripts/ @devops-team

# Documentation
/docs/ @tech-lead
*.md @tech-lead
```

## Best Practices

### Do's

- ✅ Keep commits small and focused
- ✅ Write meaningful commit messages
- ✅ Pull from base branch frequently
- ✅ Run tests before pushing
- ✅ Review your own code before requesting review
- ✅ Respond to review comments promptly

### Don'ts

- ❌ Never commit directly to `main` or `develop`
- ❌ Never force push to shared branches
- ❌ Don't commit secrets or credentials
- ❌ Don't commit large binary files
- ❌ Avoid merge commits (prefer rebase)
- ❌ Don't approve your own PRs

## Troubleshooting

### Resolve Merge Conflicts

```bash
# Update your branch
git fetch origin
git rebase origin/develop

# Resolve conflicts in each file
# Then continue
git add .
git rebase --continue

# Or abort if needed
git rebase --abort
```

### Undo Last Commit (Not Pushed)

```bash
# Keep changes staged
git reset --soft HEAD~1

# Discard changes
git reset --hard HEAD~1
```

### Cherry-pick Specific Commit

```bash
git cherry-pick <commit-hash>
```
