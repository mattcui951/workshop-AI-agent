#!/usr/bin/env bash
# GA x HSBC "AI for Software Engineers": install everything the Python AND the Java course need on a Mac (Apple Silicon or Intel).
# JDK 21 and Python 3.12 come from Homebrew; Maven 3.9 is downloaded into ~/.ga-course (no sudo). Safe to re-run.
# Works with the bash that ships with macOS (3.2) and changes your shell startup file only between the marked lines.
#
# Optional settings (environment variables):
#   INSTALL_HOMEBREW=yes        install Homebrew if it is missing (it will ask for your password)
#   INSTALL_VSCODE=no           skip VS Code and its extensions
#   REPO_PYTHON=... REPO_JAVA=... where the course folders are (default: ~/hsbc-ai-for-se-<lang>-4-hours or ~/HSBC-AI-for-SE-<Lang>-4-hours)
#   VENV=...                    where the Python environment goes (default ~/ga-python-venv)
#   MAVEN_VERSION=3.9.9         MAVEN_BASE_URL=...
set -u -o pipefail

if [ "${SKIP_OS_CHECK:-}" != "yes" ] && [ "$(uname -s)" != "Darwin" ]; then
  echo "This script is for macOS. On Ubuntu use setup-skillable-workspace.sh instead." >&2; exit 1
fi

pick_dir() { local d; for d in "$@"; do [ -d "$d/curriculum" ] && { echo "$d"; return; }; done; echo "$1"; }
REPO_PYTHON="${REPO_PYTHON:-$(pick_dir "$HOME/hsbc-ai-for-se-python-4-hours" "$HOME/HSBC-AI-for-SE-Python-4-hours")}"
REPO_JAVA="${REPO_JAVA:-$(pick_dir "$HOME/hsbc-ai-for-se-java-4-hours" "$HOME/HSBC-AI-for-SE-Java-4-hours")}"
VENV="${VENV:-$HOME/ga-python-venv}"
TOOLS="${TOOLS:-$HOME/.ga-course}"
CHECK_DIR="${CHECK_DIR:-$HOME/java-libs-check}"
MAVEN_VERSION="${MAVEN_VERSION:-3.9.9}"
MAVEN_BASE_URL="${MAVEN_BASE_URL:-https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries}"
INSTALL_VSCODE="${INSTALL_VSCODE:-yes}"
ACTIVATE_BY_DEFAULT="${ACTIVATE_BY_DEFAULT:-yes}"
case "${SHELL:-}" in */bash) RC="$HOME/.bash_profile";; *) RC="$HOME/.zshrc";; esac     # zsh is the macOS default
RC_BEGIN="# >>> ga-course (GA x HSBC AI for Software Engineers) >>>"; RC_END="# <<< ga-course <<<"
FAILED=()

# The Python versions the course was tested with. Exact pins, so every machine gets the same combination.
PACKAGES=("anthropic==1.13.0" "pydantic==2.14.1" "fastapi==0.143.0" "httpx==0.28.1" "pytest==9.1.1" "bandit==1.9.4" "PyYAML==6.0.3" "openapi-spec-validator==0.9.0")

has() { grep -q -- "$1" <<<"$2"; }                 # text test without a pipe (a pipe plus pipefail can report a false failure)
step() { printf '\n==> %s\n' "$*"; }
ok()   { printf '    ok: %s\n' "$*"; }
warn() { printf '    WARNING: %s\n' "$*"; }
fail() { printf '    FAILED: %s\n' "$*"; FAILED+=("$*"); }

find_brew() {
  local b; for b in "$(command -v brew 2>/dev/null)" /opt/homebrew/bin/brew /usr/local/bin/brew; do [ -n "$b" ] && [ -x "$b" ] && { echo "$b"; return 0; }; done; return 1
}

install_homebrew() {
  step "1/6  Homebrew"
  local b; b="$(find_brew)"
  if [ -z "$b" ] && [ "${INSTALL_HOMEBREW:-}" = "yes" ]; then
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" || { fail "Homebrew installer failed"; return 1; }
    b="$(find_brew)"
  fi
  if [ -z "$b" ]; then
    fail "Homebrew is not installed. Install it from https://brew.sh (one command, asks for your password), then re-run this script. Or re-run with INSTALL_HOMEBREW=yes to let this script do it."
    return 1
  fi
  eval "$("$b" shellenv)"
  ok "$(brew --version | head -1)"
}

brew_ensure() {   # $1 = formula
  if brew list --versions "$1" >/dev/null 2>&1; then ok "$1 already installed"
  else brew install "$1" >/tmp/ga-brew.log 2>&1 && ok "installed $1" || { fail "brew install $1 (see /tmp/ga-brew.log)"; tail -5 /tmp/ga-brew.log; return 1; }; fi
}

install_java() {
  step "2/6  JDK 21 (Homebrew openjdk@21)"
  brew_ensure openjdk@21 || return 1
  JAVA_HOME_21="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
  [ -x "$JAVA_HOME_21/bin/javac" ] || { fail "no JDK found at $JAVA_HOME_21"; return 1; }
  export JAVA_HOME="$JAVA_HOME_21"; export PATH="$JAVA_HOME/bin:$PATH"
  local jv; jv="$(java -version 2>&1)"
  has '"21' "$jv" && ok "$(head -1 <<<"$jv")" || { fail "java 21 is not the default java (got: $(head -1 <<<"$jv"))"; return 1; }
  ok "$(javac -version 2>&1)"
}

install_python_env() {
  step "3/6  Python 3.12 and the course packages ($VENV)"
  brew_ensure python@3.12 || return 1
  local py312; py312="$(brew --prefix python@3.12)/bin/python3.12"
  [ -x "$py312" ] || { fail "no python3.12 at $py312"; return 1; }
  ok "$("$py312" --version)"
  if [ ! -x "$VENV/bin/python" ]; then "$py312" -m venv "$VENV" || { fail "python -m venv $VENV"; return 1; }; fi
  "$VENV/bin/python" -m pip install --quiet --upgrade pip || { fail "upgrade pip (is pypi.org reachable?)"; return 1; }
  "$VENV/bin/python" -m pip install --quiet "${PACKAGES[@]}" || { fail "pip install (see the error above)"; return 1; }
  "$VENV/bin/python" -m pip check >/dev/null && ok "pip check: no conflicting requirements" || fail "pip check found conflicting requirements"
  if "$VENV/bin/python" -m pip freeze > "$HOME/ga-python-venv-freeze.txt" 2>/dev/null; then ok "full package list saved to $HOME/ga-python-venv-freeze.txt"; fi
  local p; for p in "${PACKAGES[@]}"; do "$VENV/bin/python" -m pip show "${p%%==*}" 2>/dev/null | sed -n '1p;2p' | paste -s -d ' ' - | sed 's/^/    /'; done
  "$VENV/bin/python" -c "import anthropic, pydantic, fastapi, httpx, pytest, yaml, bandit, openapi_spec_validator" 2>/dev/null && ok "all 8 course packages import" || fail "a course package does not import"
  return 0
}

install_maven() {
  step "4/6  Apache Maven $MAVEN_VERSION (into $TOOLS, no sudo)"
  local mv; mv="$(mvn -v 2>/dev/null)"
  if has "Apache Maven 3\.9" "$mv"; then ok "already installed: $(head -1 <<<"$mv")"; MAVEN_BIN_DIR="$(dirname "$(command -v mvn)")"; return 0; fi
  if [ -x "$TOOLS/maven/bin/mvn" ]; then   # installed by an earlier run but not on this shell's PATH yet
    export PATH="$TOOLS/maven/bin:$PATH"; hash -r; mv="$(mvn -v 2>/dev/null)"
    if has "Apache Maven 3\.9" "$mv"; then ok "already installed in $TOOLS: $(head -1 <<<"$mv")"; MAVEN_BIN_DIR="$TOOLS/maven/bin"; return 0; fi
  fi
  mkdir -p "$TOOLS"; local tmp; tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/maven.tgz" "$MAVEN_BASE_URL/apache-maven-$MAVEN_VERSION-bin.tar.gz" || { fail "download Maven from $MAVEN_BASE_URL"; return 1; }
  curl -fsSL -o "$tmp/maven.sha512" "$MAVEN_BASE_URL/apache-maven-$MAVEN_VERSION-bin.tar.gz.sha512" || { fail "download Maven checksum"; return 1; }
  (cd "$tmp" && echo "$(cut -d' ' -f1 maven.sha512)  maven.tgz" | shasum -a 512 -c - >/dev/null 2>&1) || { fail "Maven checksum mismatch"; return 1; }
  rm -rf "$TOOLS/apache-maven-$MAVEN_VERSION"
  tar -xzf "$tmp/maven.tgz" -C "$TOOLS" || { fail "unpack Maven"; return 1; }
  ln -sfn "$TOOLS/apache-maven-$MAVEN_VERSION" "$TOOLS/maven"
  export PATH="$TOOLS/maven/bin:$PATH"; MAVEN_BIN_DIR="$TOOLS/maven/bin"; hash -r
  mv="$(mvn -v 2>/dev/null)"; has "Apache Maven $MAVEN_VERSION" "$mv" && ok "$(head -1 <<<"$mv")" || { fail "mvn does not report version $MAVEN_VERSION"; return 1; }
  has 'Java version: 21' "$mv" && ok "Maven is using Java 21" || warn "Maven is not reporting Java 21: check JAVA_HOME ($JAVA_HOME)"
}

write_shell_profile() {
  step "5/6  Make the tools available in every new Terminal ($RC)"
  if grep -qF "$RC_BEGIN" "$RC" 2>/dev/null; then ok "already set up (the ga-course block in $RC)"; return 0; fi
  {
    printf '\n%s\n' "$RC_BEGIN"
    printf 'export JAVA_HOME="%s"\n' "${JAVA_HOME_21:-}"
    printf 'export PATH="$JAVA_HOME/bin:%s:$PATH"\n' "${MAVEN_BIN_DIR:-$TOOLS/maven/bin}"
    if [ "$ACTIVATE_BY_DEFAULT" = "yes" ]; then printf 'source "%s/bin/activate"\n' "$VENV"; fi
    printf '%s\n' "$RC_END"
  } >> "$RC" && ok "added JAVA_HOME, Maven and the Python environment to $RC (open a NEW Terminal to use them)" || fail "could not write $RC"
}

install_vscode() {
  if [ "$INSTALL_VSCODE" != "yes" ]; then step "VS Code skipped (INSTALL_VSCODE=$INSTALL_VSCODE)"; return; fi
  step "Optional: VS Code, Python and Java extensions, GitHub Copilot"
  local app="/Applications/Visual Studio Code.app" cli="/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code"
  if [ ! -d "$app" ]; then brew install --cask visual-studio-code >/tmp/ga-brew-cask.log 2>&1 && ok "installed VS Code" || { fail "brew install --cask visual-studio-code (see /tmp/ga-brew-cask.log)"; return; }; else ok "VS Code already installed"; fi
  [ -x "$cli" ] || { warn "VS Code command line tool not found at $cli: install the extensions from inside VS Code"; return; }
  local ext; for ext in ms-python.python ms-python.vscode-pylance vscjava.vscode-java-pack GitHub.copilot GitHub.copilot-chat; do
    "$cli" --install-extension "$ext" --force >/dev/null 2>&1 && ok "extension $ext" || warn "could not install extension $ext (needs extension-marketplace access)"
  done
  local settings="$HOME/Library/Application Support/Code/User/settings.json"
  if [ ! -f "$settings" ]; then
    mkdir -p "$(dirname "$settings")"
    printf '{\n  "python.defaultInterpreterPath": "%s/bin/python",\n  "java.jdt.ls.java.home": "%s"\n}\n' "$VENV" "${JAVA_HOME_21:-}" > "$settings" && ok "VS Code will use the course Python and JDK 21"
  else warn "VS Code settings already exist: select the Python interpreter $VENV/bin/python yourself (Cmd+Shift+P, Python: Select Interpreter)"; fi
}

write_libs_check_files() {   # $1 = target directory: a tiny Maven project that touches every Java library the course uses
  local d="$1"; mkdir -p "$d/src/main/java" "$d/src/test/java"
  cat > "$d/pom.xml" << 'POM_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.ga</groupId><artifactId>java-libs-check</artifactId><version>1.0.0</version>
  <properties><maven.compiler.release>21</maven.compiler.release><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding></properties>
  <dependencies>
    <dependency><groupId>io.vertx</groupId><artifactId>vertx-core</artifactId><version>4.5.10</version></dependency>
    <dependency><groupId>io.vertx</groupId><artifactId>vertx-web</artifactId><version>4.5.10</version></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><version>2.2.224</version></dependency>
    <dependency><groupId>org.apache.logging.log4j</groupId><artifactId>log4j-core</artifactId><version>2.14.1</version></dependency>
    <dependency><groupId>org.apache.commons</groupId><artifactId>commons-text</artifactId><version>1.9</version></dependency>
    <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.10.2</version><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><version>5.11.0</version><scope>test</scope></dependency>
  </dependencies>
  <build><plugins>
    <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.12.1</version></plugin>
    <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.2.5</version></plugin>
    <plugin><groupId>org.codehaus.mojo</groupId><artifactId>exec-maven-plugin</artifactId><version>3.2.0</version><configuration><mainClass>LibsCheck</mainClass></configuration></plugin>
  </plugins></build>
</project>
POM_EOF
  cat > "$d/src/main/java/LibsCheck.java" << 'CHECK_JAVA_EOF'
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import java.sql.*;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;

public class LibsCheck {
    interface Check { String run() throws Exception; }
    static int failures = 0;
    static void check(String name, Check c) {
        try { System.out.printf("    LIB-OK   %-22s %s%n", name, c.run()); }
        catch (Throwable t) { failures++; System.out.printf("    LIB-FAIL %-22s %s%n", name, t); }
    }
    public static void main(String[] args) {
        check("Vert.x core and web", () -> { Vertx v = Vertx.vertx(); try { Router.router(v); return "a router was created"; } finally { v.close(); } });
        check("H2 embedded SQL", () -> { try (Connection c = DriverManager.getConnection("jdbc:h2:mem:libscheck"); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT H2VERSION()")) { rs.next(); return "H2 " + rs.getString(1); } });
        check("commons-text 1.9", () -> WordUtils.capitalize("the library loads"));
        check("log4j-core 2.14.1", () -> { Class<?> core = Class.forName("org.apache.logging.log4j.core.LoggerContext"); LogManager.getLogger(LibsCheck.class); return core.getSimpleName() + " loaded"; });
        System.exit(failures == 0 ? 0 : 1);
    }
}
CHECK_JAVA_EOF
  cat > "$d/src/test/java/LibsCheckTest.java" << 'CHECK_TEST_EOF'
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;

class LibsCheckTest {
    @Test void junitWorks() { assertEquals(4, 2 + 2); }
    @Test @SuppressWarnings("unchecked") void mockitoWorks() { List<String> l = mock(List.class); when(l.size()).thenReturn(3); assertEquals(3, l.size()); }
}
CHECK_TEST_EOF
}

fill_maven_repository() {
  step "6/6  Download every Java library and plugin into the local Maven repository (~/.m2)"
  command -v mvn >/dev/null 2>&1 || { fail "Maven is not available, so the Java libraries were not downloaded"; return; }
  write_libs_check_files "$CHECK_DIR"
  if (cd "$CHECK_DIR" && mvn -B -ntp -q test); then ok "JUnit 5 and Mockito tests pass (also downloads the compiler plugin, surefire and its JUnit provider)"; else fail "mvn test in $CHECK_DIR"; fi
  local out rc; out="$(cd "$CHECK_DIR" && mvn -B -ntp -q compile exec:java 2>&1)"; rc=$?
  grep -E "LIB-(OK|FAIL)" <<<"$out"
  if [ "$rc" -eq 0 ]; then ok "Vert.x, H2, commons-text and log4j-core all load (also downloads the exec plugin)"; else fail "a Java library did not load (see the LIB-FAIL lines, or the Maven error: $(echo "$out" | tail -2 | tr '\n' ' '))"; fi
  if [ ! -d "$REPO_JAVA/curriculum" ] && [ -f "$HOME/hsbc-ai-for-se-java-4-hours.zip" ]; then (cd "$HOME" && unzip -q -o hsbc-ai-for-se-java-4-hours.zip) && ok "unpacked $HOME/hsbc-ai-for-se-java-4-hours.zip"; fi
  if [ ! -d "$REPO_JAVA/curriculum" ]; then
    warn "Java course folder not found at $REPO_JAVA: its projects' own plugins were NOT downloaded. Put the folder in your home folder (or pass REPO_JAVA=...) and re-run."; return
  fi
  local pom dir
  while IFS= read -r pom; do
    dir="$(dirname "$pom")"
    if (cd "$dir" && mvn -B -ntp -q test-compile > /tmp/mvn-warmup.log 2>&1); then ok "${dir#$REPO_JAVA/}"; else fail "could not resolve or compile ${dir#$REPO_JAVA/}"; tail -6 /tmp/mvn-warmup.log | sed 's/^/        /'; fi
  done < <(find "$REPO_JAVA/curriculum" -name pom.xml | sort)
  [ -d "$REPO_PYTHON/curriculum" ] || warn "Python course folder not found at $REPO_PYTHON (pass REPO_PYTHON=... when you run the Python labs)"
}

summary() {
  step "Summary"
  printf '    java     : %s\n' "$(java -version 2>&1 | head -1)"
  printf '    maven    : %s\n' "$(mvn -v 2>/dev/null | head -1)"
  if [ -x "$VENV/bin/python" ]; then printf '    python   : %s (%s)\n' "$("$VENV/bin/python" --version 2>&1)" "$VENV"; else printf '    python   : not installed\n'; fi
  printf '    ~/.m2    : %s\n' "$(du -sh "$HOME/.m2/repository" 2>/dev/null | cut -f1)"
  printf '    ANTHROPIC_API_KEY: %s\n' "$([ -n "${ANTHROPIC_API_KEY:-}" ] && echo 'is set' || echo 'not set in this Terminal (the live Claude demos need it)')"
  if [ "${#FAILED[@]}" -eq 0 ]; then
    printf '\n    Install finished with no failures. Open a NEW Terminal (or run: source %s).\n' "$RC"
    printf '    Optional: so macOS tools and IDEs can also find JDK 21, run once:\n'
    printf '      sudo ln -sfn "%s" /Library/Java/JavaVirtualMachines/openjdk-21.jdk\n' "$(dirname "$(dirname "${JAVA_HOME_21:-/x/y}")")"
  else
    printf '\n    %d step(s) FAILED:\n' "${#FAILED[@]}"; printf '      - %s\n' "${FAILED[@]}"; return 1
  fi
}

main() {
  install_homebrew || { summary; return 1; }
  install_java || { summary; return 1; }
  install_python_env || true
  install_maven || true
  write_shell_profile
  install_vscode
  fill_maven_repository
  summary
}
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then main "$@"; fi
