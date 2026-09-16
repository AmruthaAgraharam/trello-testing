# Trello Selenium Automation - Login & Authentication Module

Selenium WebDriver + TestNG automation framework for testing Trello. This repository currently contains the **framework core** and the **Login & Authentication module**. Teammates add their own test modules (boards, cards, lists, etc.) on top of the shared base.

## How It Works

The project follows the **Page Object Model** with a shared test base. Responsibilities are split so each layer has exactly one reason to change:

| Layer | Location | Responsibility |
|-------|----------|----------------|
| Tests | `src/test/java/tests/` | What to verify - scenarios and assertions only |
| Page Objects | `src/main/java/pages/` | How to drive a page - locators and actions, one class per page |
| Test Base | `src/main/java/utils/BaseTest.java` | Browser lifecycle, login helpers, shared config |
| Config | `src/main/java/utils/TestConfig.java` | Reads settings: environment variables first, then `config.properties` |
| Failure Evidence | `src/main/java/utils/ScreenshotListener.java` | Saves a screenshot on every failed test |

Execution flow of a single test:

1. `@BeforeMethod` in `BaseTest` starts a fresh browser (configured via `browser` key, default Firefox) and initializes the page objects.
2. The test calls `performLogin()` - credentials are preflighted, the two-step Trello login runs, and dashboard load is verified.
3. The test asserts state through page objects.
4. `@AfterMethod` closes the browser, so every test runs in a clean session with no state bleed.
5. If the test fails, `ScreenshotListener` writes a PNG to `test-output/screenshots/` before the browser closes.

There is no `testng.xml`. Maven auto-discovers test classes whose names end in `Tests`, and IntelliJ runs any class containing `@Test` directly - both workflows behave identically.

## Prerequisites

- Java 21+ (project targets 26)
- Firefox (default browser; Chrome/Edge optional via config)
- Maven 3.6+ (or IntelliJ's bundled Maven)
- A Trello test account without 2FA

## Quick Start

1. **Clone the repository**

   ```bash
   git clone git@github.com:alimaazen/trello-testing.git
   cd trello-testing
   ```

2. **Set your credentials**

   ```cmd
   setx TRELLO_EMAIL "your_email@example.com"
   setx TRELLO_PASSWORD "your_password"
   ```

   Or run the helper script:

   ```cmd
   setup-env.bat
   ```

3. **Restart your IDE and terminals** - environment variables only load on startup.

4. **Run the tests**

   ```bash
   mvn clean test
   ```

## Project Structure

```
trello-testing/
├── pom.xml                          # Maven configuration
├── setup-env.bat                    # Credential setup helper
├── verify-env.bat                   # Verify environment variables
├── src/
│   ├── main/java/
│   │   ├── pages/
│   │   │   ├── LoginPage.java       # Login page object
│   │   │   └── DashboardPage.java   # Dashboard page object
│   │   └── utils/
│   │       ├── BaseTest.java        # Base test: driver setup + login helpers
│   │       ├── TestConfig.java      # Configuration reader (env vars > config file)
│   │       └── ScreenshotListener.java  # Screenshot on test failure
│   └── test/
│       ├── java/tests/
│       │   └── LoginTests.java      # Login test cases
│       └── resources/
│           ├── config.properties    # Local config (gitignored)
│           └── config.properties.example  # Template
```

## Login Module Coverage

| Test | Scenario |
|------|----------|
| `testValidLogin` | Valid credentials reach the dashboard |
| `testInvalidEmailLogin` | Unregistered email never reaches the password step; signup redirect or error |
| `testInvalidPasswordLogin` | Wrong password keeps the user on the login page / shows an error |
| `testEmptyCredentialsLogin` | Empty email cannot advance to the password step |
| `testLogout` | Logout completes and the session is gone |

## UI/UX & Cross-Browser Validation Coverage

Rendering and layout validation across viewport widths (1920 desktop, 1280 laptop/tablet, 375 mobile) and browsers (Chrome, Firefox, Edge - via the `browser` config key). Each test below runs once per viewport through a TestNG `@DataProvider`, asserting key elements stay visible and clickable and that the page has no unintended horizontal overflow.

| Test | Scenario |
|------|----------|
| `testLoginPageLayout` | Email input and continue button stay visible/clickable on the login page at every viewport width |
| `testDashboardLayout` | Header and create-board button stay visible/clickable on the dashboard at every viewport width |
| `testBoardPageLayout` | Board title and add-list control stay visible/clickable on a board at every viewport width |
| `testCardModalLayout` | Card modal (description area, close button) stays fully visible and closeable at every viewport width |
| `testChecklistPanelLayout` | An existing checklist item's row stays visible, clickable, and fully within the viewport at every viewport width |
| `testCoverColorPickerLayout` | The cover color swatch grid stays visible/clickable and unclipped, and doesn't cause horizontal overflow, at every viewport width |

To cover all three browsers, rerun the class with a different `BROWSER` environment variable each time (no code changes needed - `TestConfig` already reads it):

```bash
setx BROWSER chrome  && mvn test -Dtest=UIUXCrossBrowserTests
setx BROWSER firefox && mvn test -Dtest=UIUXCrossBrowserTests
setx BROWSER edge    && mvn test -Dtest=UIUXCrossBrowserTests
```

## For Teammates - Building Your Module

Extend `BaseTest` to inherit browser setup, config, and login:

```java
package tests;

import org.testng.annotations.Test;
import utils.BaseTest;
import pages.DashboardPage;

public class CardTests extends BaseTest {

    @Test
    public void testCreateCard() {
        // Verified login - safe to proceed when this returns
        DashboardPage dashboard = performLogin();

        // Your page objects and assertions here
    }
}
```

### Available Methods

- `performLogin()` - Login with your configured credentials. **Verifies the dashboard actually loaded** and fails fast with a clear message if credentials are missing or login does not work.
- `performLogin(email, password)` - Login with explicit credentials, **no success verification**. Use for negative tests where failing is the expected outcome.
- `navigateToLoginPage()` - Navigate to the login page without logging in.
- `waitUntil(condition)` - Wait up to 15s for a condition; returns false on timeout instead of throwing.
- `getDriver()` - Access the WebDriver instance.

### Available Objects

- `driver` - WebDriver instance
- `loginPage` - LoginPage object
- `dashboardPage` - DashboardPage object
- `config` - TestConfig object

Failed tests automatically save a screenshot to `test-output/screenshots/` (path printed to the console).

### Rules

1. Test class names must end in `Tests` (e.g. `CardTests`, `BoardTests`) - this is what Maven auto-discovery matches.
2. Never hardcode credentials - use environment variables or `config.properties` (gitignored).
3. Locators live in page objects, never in test methods. If a locator is missing, add a page object method.
4. Branch from `master`, submit your work as a pull request. Do not create parallel repositories or zip files.

## Configuration

Settings resolve in priority order: **environment variable** > **config.properties** > built-in default. Key mapping is automatic: `trello.url` -> `TRELLO_URL`.

### Environment Variables (recommended for credentials)

- `TRELLO_EMAIL` - test account email
- `TRELLO_PASSWORD` - test account password

Why environment variables: never committed to git, each member uses their own account, no credential conflicts, CI/CD friendly.

### Config File

Copy `config.properties.example` to `config.properties` and customize:

```properties
trello.url=https://trello.com/login
browser=firefox
headless=false
```

- `browser` - `firefox` (default), `chrome`, or `edge`
- `headless` - `true` for CI/no-window runs, `false` (default) otherwise

A missing `config.properties` is not an error - the framework falls back to environment variables alone. A missing *required key* fails fast with a message naming the exact variable to set.

## Running Tests

### Quick Test Suites

```bash
# Core tests (no special prerequisites)
# Excludes: LoginTests, CollaborationTests, DragAndDrop_Test
mvn test -DsuiteXmlFile=testng-exclude-login.xml

# All tests (requires all prerequisites below)
mvn clean test

# Specific test class
mvn test -Dtest=BoardManagementTest

# Specific test method
mvn test -Dtest=LoginTests#testValidLogin
```

In IntelliJ IDEA: right-click a test class or method and select Run - no suite file needed.

### Test Prerequisites

Some tests require specific setup:

| Test Class | Prerequisites | How to Run |
|------------|--------------|------------|
| **LoginTests** | None - tests login functionality | Always runs |
| **SeleniumTest** | None - simple smoke test | Always runs |
| **BoardManagementTest** | None - creates board dynamically | Always runs |
| **CardDetailsTest** | None - creates fixture board/list/card | Always runs |
| **ListTests** | None - creates fixture board | Always runs |
| **UIUXCrossBrowserTests** | None - creates fixture board/list/card | Always runs |
| **CollaborationTests** | **Second Trello account** (see below) | Skips if `TRELLO_EMAIL_SECOND` not set |
| **DragAndDrop_Test** | ⚠️ **Pre-configured board + EXPERIMENTAL** (see below) | Skips if board not found |

#### CollaborationTests Setup

Multi-user collaboration tests (inviting members, roles, @mentions) need a second Trello account:

```cmd
setx TRELLO_EMAIL_SECOND "second_account@example.com"
setx TRELLO_PASSWORD_SECOND "second_password"
```

**Important:** The second account's display name must be "Rashmi" (configured in `CollaborationTests.secondAccountName()`). Change this in the test if your second account has a different name.

Tests will **skip gracefully** if these variables aren't set - your build stays green.

#### DragAndDrop_Test - Experimental (Hardened but Still Risky)

⚠️ **This test is now ENABLED but experimental** after applying extensive hardening:

**Hardening Applied:**
- ✅ **Scroll-into-view** - Both source/target elements centered in viewport before drag
- ✅ **Element re-location** - Re-finds elements after scroll (fixes staleness)
- ✅ **JavaScript fallback** - Card drags fall back to JS dragstart/drop events if Actions API fails
- ✅ **Viewport constraint handling** - List drags detect distance and use offset-based drag if >800px apart
- ✅ **Increased pauses** - 1000ms → 1500ms to allow React re-renders
- ✅ **Larger headless viewport** - Firefox headless now 1920x1080 (was 1366x683)

**Prerequisite Setup:**
1. **Create board:** "Trello Project"
2. **Create lists:** "To Do", "Doing", "Done"
3. **Create cards in "To Do" list:**
   - "Create test cases"
   - "implement test cases"
   - "Prepare test script"

Test will **skip with a clear message** if the board/lists/cards are missing.

**Known Remaining Risks:**
- Selenium Actions API fundamentally fragile with React SPAs (not a code bug)
- Headless mode still more flaky than headed mode
- Trello's optimistic UI can cause race conditions despite hardening

**To disable if flakiness returns:** Set `SKIP_ALL_DRAG_TESTS = true` in `DragAndDrop_Test.java`.

### Running Core Tests Only

Use `testng-exclude-login.xml` to skip tests with special prerequisites:

```bash
mvn test -DsuiteXmlFile=testng-exclude-login.xml
```

This suite excludes:
- LoginTests (standalone auth tests)
- CollaborationTests (needs second account)
- DragAndDrop_Test (needs pre-configured board)

All other tests create their fixtures dynamically and run on fresh accounts.

## Test Reporting - Allure

Every test run (any suite, any single class/method) records results into `target/allure-results` via the `AllureTestNg` listener registered on `BaseTest`. Generate the HTML report after running tests:

```bash
mvn clean test                                        # or: mvn test -DsuiteXmlFile=testng-exclude-login.xml
mvn allure:report                                     # builds target/site/allure-maven-plugin/index.html
mvn allure:serve                                      # or: build + open a live report in your browser
```

`allure:report`/`allure:serve` read whatever is currently in `target/allure-results`, so re-running `mvn test` for a different suite (e.g. switching between the full suite and `testng-exclude-login.xml`) before regenerating the report reflects only the latest run. Raw results and the generated site both live under `target/`, which is already gitignored.

## Writing New Page Objects

Follow the existing pattern - locators as fields, actions as methods, explicit waits throughout:

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CardPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private By addCardButton = By.cssSelector("button[data-testid='add-card']");

    public CardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void createCard(String title) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(addCardButton));
        button.click();
        // ... enter title, submit
    }
}
```

Guidelines:

- Prefer `data-testid` locators over generated ids (`username-uid1`-style ids can shift between Trello builds).
- Never use `Thread.sleep` - use explicit waits (`WebDriverWait`, or `waitUntil()` from `BaseTest`).
- Methods that probe state return booleans instead of throwing; action methods fail loudly.

## Troubleshooting

### "Cannot find Firefox binary"
- Install Firefox, or switch browsers in `config.properties`: `browser=chrome` or `browser=edge`.

### "Environment variable null" error
- Set the variables, then restart your terminal/IDE: `setx TRELLO_EMAIL "..."`.
- Verify: `echo %TRELLO_EMAIL%`, or run `verify-env.bat`.

### "Missing configuration for ..." error
- The framework names the exact key that is missing. Set the matching environment variable (e.g. `TRELLO_URL`) or add the key to `config.properties`.

### "Cannot resolve symbol" in IntelliJ
- Maven dependencies not loaded: Maven panel -> Reload, or run `mvn clean install`.

### Login tests fail with a 2FA prompt
- Disable 2FA on your Trello test account.

### Java version issues
- Project targets Java 26; Java 21+ works. IntelliJ: File -> Project Structure -> Project SDK.

## Security Practices

- Store credentials as environment variables, never in code or git.
- `config.properties` is gitignored - keep real passwords out of `config.properties.example` too.
- Use a dedicated test account, not a personal one. Disable 2FA on test accounts only.
- One account per person - avoids conflicts when tests run in parallel.
- Never share credentials in Slack, email, or commit messages.

## Contributing

1. Set up your test account and environment variables (see Quick Start).
2. Branch from `master`: `git checkout -b feature/board-tests`.
3. Write tests following the existing patterns (extend `BaseTest`, name the class `*Tests`).
4. Run `mvn clean test` before pushing.
5. Open a pull request - do not push branches with unrelated history or share code as zip files.

## Resources

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/doc/documentation-main.html)
- [Page Object Model](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/)
- [Trello API Documentation](https://developer.atlassian.com/cloud/trello/)


