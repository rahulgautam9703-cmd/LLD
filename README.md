# LLD (Meeting Scheduler)

This repository contains the Meeting Scheduler project. A local VS Code workspace settings file containing Java/JDK configuration was intentionally removed from the repository and ignored to keep local developer environments private.

## Configure JDK globally in VS Code (recommended for personal machines)

1. Install OpenJDK (Homebrew example):

```bash
brew install openjdk
```

2. Find the installed JDK home (example):

```bash
/usr/libexec/java_home -V
```

3. Open VS Code user settings JSON:
- `Cmd + ,` then click the `{}` icon
- or use `Preferences: Open Settings (JSON)` from the Command Palette

4. Add the following (update the `path` if your JDK is installed elsewhere):

```json
{
  "java.home": "/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home",
      "default": true
    }
  ]
}
```

5. Verify installation in a terminal:

```bash
java -version
which java
/usr/libexec/java_home -V
```

If `java -version` fails, install OpenJDK and ensure the configured `java.home` points to the correct JDK installation directory.

---

If you want, I can also add instructions for project contributors or create a `.devcontainer` that includes a JDK for reproducible development environments.
