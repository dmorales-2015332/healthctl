# healthctl

Lightweight shell-based health check orchestrator for containerized services with configurable retry policies.

---

## Installation

```bash
git clone https://github.com/yourorg/healthctl.git
cd healthctl && ./mvnw clean install -q
```

## Usage

Run a health check against a target service using a named policy:

```bash
java -jar healthctl.jar --target http://localhost:8080/health --policy retry-3x
```

**Example policy configuration (`healthctl.yaml`):**

```yaml
policies:
  retry-3x:
    interval: 5s
    retries: 3
    timeout: 2s
    expect:
      status: 200
```

**Example output:**

```
[healthctl] Checking http://localhost:8080/health...
[healthctl] Attempt 1/3 → 200 OK (342ms)
[healthctl] Service is healthy.
```

Available flags:

| Flag | Description |
|------|-------------|
| `--target` | Service endpoint to probe |
| `--policy` | Named retry policy from config |
| `--config` | Path to custom `healthctl.yaml` |
| `--quiet` | Suppress output, exit code only |

Exit code `0` indicates a healthy service; `1` indicates failure after all retries.

## Requirements

- Java 17+
- Docker (optional, for container-aware checks)

## License

[MIT](LICENSE)