### `file_1.js`
```javascript
automation-project/
├── .env
├── package.json
├── playwright.config.js
├── data/
│   └── testData.json
├── pages/
│   ├── BasePage.js
│   ├── LoginPage.js
│   ├── DashboardPage.js
│   └── TransfersPage.js
├── tests/
│   ├── auth.setup.js
│   └── banking.spec.js
└── utils/
    └── envUtils.js
```

### `.env`
```javascript
BASE_URL=https://simple-banking.example.com
ADMIN_USERNAME=AdminUser01
ADMIN_PASSWORD=[REDACTED_CREDENTIAL]
```

### `package.json`
```json
{
  "name": "simple-banking-automation",
  "version": "1.0.0",
  "devDependencies": {
    "@playwright/test": "^1.40.0"
  },
  "scripts": {
    "test": "npx playwright test"
  }
}
```

### `playwright.config.js`
```javascript
const { defineConfig, devices } = require('@playwright/test');
const EnvUtils = require('./utils/envUtils');
const path = require('path');

const runId = new Date().getTime();
const STORAGE_STATE = path.join(__dirname, 'playwright/.auth/user.json');

module.exports = defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 200000,
  expect: { timeout: 60000 },
  reporter: [['html', { outputFolder: `playwright-report/run-${runId}` }]],
  outputDir: `test-results/run-${runId}`,
  use: {
    baseURL: EnvUtils.BASE_URL,
    actionTimeout: 50000,
    trace: 'on',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'setup', testMatch: /.*.setup.js/ },
    { 
      name: 'chromium', 
      use: { ...devices['Desktop Chrome'], storageState: STORAGE_STATE }, 
      dependencies: ['setup'] 
    }
  ],
});
```

### `utils/envUtils.js`
```javascript
require('dotenv').config();

class EnvUtils {
  static get BASE_URL() { return process.env.BASE_URL; }
  static get ADMIN_USERNAME() { return process.env.ADMIN_USERNAME; }
  static get ADMIN_PASSWORD() { return process.env.ADMIN_PASSWORD; }
}

module.exports = EnvUtils;
```

### `data/testData.json`
```json
{
  "loginData": { "username": "AdminUser01", "password": "[REDACTED_CREDENTIAL]" },
  "transferData": { "source": "Account_A", "invalidAmount": "-500.00", "expectedError": "Invalid amount" },
  "balanceConstraints": { "expectedBalance": "1000.00", "alignment": "center", "font": "Helvetica-Bold" }
}
```

### `pages/BasePage.js`
```javascript
const { expect } = require('@playwright/test');

class BasePage {
  constructor(page) {
    this.page = page;
  }
}
module.exports = BasePage;
```

### `pages/LoginPage.js`
```javascript
const BasePage = require('./BasePage');
const EnvUtils = require('../utils/envUtils');

class LoginPage extends BasePage {
  constructor(page) {
    super(page);
    this.userField = page.getByPlaceholder('Username');
    this.passField = page.getByPlaceholder('Password');
    this.loginBtn = page.getByRole('button', { name: 'Login' });
  }

  async login(username, password) {
    await this.userField.fill(username);
    await this.passField.fill(password);
    await this.loginBtn.click();
  }
}
module.exports = LoginPage;
```

### `pages/DashboardPage.js`
```javascript
const BasePage = require('./BasePage');

class DashboardPage extends BasePage {
  constructor(page) {
    super(page);
    this.accountSummary = page.getByTestId('account-summary');
    this.balanceDisplay = page.locator('.balance-value');
  }
}
module.exports = DashboardPage;
```

### `pages/TransfersPage.js`
```javascript
const BasePage = require('./BasePage');

class TransfersPage extends BasePage {
  constructor(page) {
    super(page);
    this.sourceAccount = page.getByLabel('Source Account');
    this.amountInput = page.getByLabel('Amount');
    this.confirmBtn = page.getByRole('button', { name: 'Confirm' });
    this.errorMessage = page.locator('.error-msg');
  }
}
module.exports = TransfersPage;
```

### `tests/auth.setup.js`
```javascript
const { test, expect } = require('@playwright/test');
const LoginPage = require('../pages/LoginPage');
const EnvUtils = require('../utils/envUtils');

test('authenticate and save session', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await page.goto('/login');
  await loginPage.login(EnvUtils.ADMIN_USERNAME, EnvUtils.ADMIN_PASSWORD);
  await expect(page).toHaveURL(/.*dashboard/);
  await page.context().storageState({ path: 'playwright/.auth/user.json' });
});
```

### `tests/banking.spec.js`
```javascript
const { test, expect } = require('@playwright/test');
const DashboardPage = require('../pages/DashboardPage');
const TransfersPage = require('../pages/TransfersPage');
const testData = require('../data/testData.json');

test('TC-TS-AI-4057-01: Verify User Authentication via Global Login Bypass', async ({ page }) => {
  const dashboard = new DashboardPage(page);
  await page.goto('/dashboard');
  await expect(dashboard.accountSummary).toBeVisible();
});

test('TC-TS-AI-4057-02: Validate Fund Transfer with Invalid Amount', async ({ page }) => {
  const transfers = new TransfersPage(page);
  await page.goto('/transfers');
  await transfers.sourceAccount.selectOption(testData.transferData.source);
  await transfers.amountInput.fill(testData.transferData.invalidAmount);
  await transfers.confirmBtn.click();
  await expect(transfers.errorMessage).toContainText(testData.transferData.expectedError);
});

test('TC-TS-AI-4057-03: Validate Account Balance UI Display Constraints', async ({ page }) => {
  const dashboard = new DashboardPage(page);
  await page.goto('/dashboard');
  const balance = dashboard.balanceDisplay;
  await expect(balance).toHaveText(testData.balanceConstraints.expectedBalance);
  // Verify UI Constraints
  const styles = await balance.evaluate((el) => {
    return {
      textAlign: getComputedStyle(el).textAlign,
      fontWeight: getComputedStyle(el).fontWeight
    };
  });
  expect(styles.textAlign).toBe(testData.balanceConstraints.alignment);
});
```

### `file_1_2.js`
```javascript
automation-project/
├── .env
├── package.json
├── playwright.config.js
├── data/
│   └── testData.json
├── pages/
│   ├── BasePage.js
│   ├── LoginPage.js
│   └── TransactionPage.js
├── tests/
│   ├── auth.setup.js
│   └── transaction.spec.js
└── utils/
    └── envUtils.js
```

### `pages/LoginPage_2.js`
```javascript
const BasePage = require('./BasePage');
const EnvUtils = require('../utils/envUtils');

class LoginPage extends BasePage {
  constructor(page) {
    super(page);
    this.usernameInput = page.getByRole('textbox', { name: /username/i });
    this.passwordInput = page.getByRole('textbox', { name: /password/i });
    this.loginButton = page.getByRole('button', { name: /login/i });
  }

  async login() {
    await this.page.goto('/login');
    await this.usernameInput.fill(EnvUtils.USERNAME);
    await this.passwordInput.fill(EnvUtils.PASSWORD);
    await this.loginButton.click();
  }
}
module.exports = LoginPage;
```

### `pages/TransactionPage.js`
```javascript
const BasePage = require('./BasePage');
const { expect } = require('@playwright/test');

class TransactionPage extends BasePage {
  constructor(page) {
    super(page);
    this.transactionTab = page.getByRole('link', { name: /transaction history/i });
    this.searchInput = page.getByPlaceholder(/search by id/i);
    this.searchIcon = page.getByRole('button', { name: /search/i });
    this.noResultsMessage = page.getByText(/no results/i);
  }

  async navigateToTransactionHistory() {
    await this.transactionTab.click();
  }

  async searchTransaction(id) {
    await this.searchInput.fill(id);
    await this.searchIcon.click();
  }
}
module.exports = TransactionPage;
```

### `data/testData_2.json`
```json
[
  {
    "testCaseId": "TC-TS-AI-4057-04",
    "transactionId": "INVALID999",
    "expectedMessage": "No Results"
  }
]
```

### `tests/auth.setup_batch_2.js`
```javascript
const { test, expect } = require('@playwright/test');
const LoginPage = require('../pages/LoginPage');
const path = require('path');

test('authenticate', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.login();
  await page.context().storageState({ path: path.join(__dirname, '../playwright/.auth/user.json') });
});
```

### `tests/transaction.spec_batch_2.js`
```javascript
const { test, expect } = require('@playwright/test');
const TransactionPage = require('../pages/TransactionPage');
const testDataset = require('../data/testData.json');

test.describe('Transaction History Module', () => {
  testDataset.forEach((data) => {
    test(`Verify Transaction History Search with Non-Existent ID - ${data.testCaseId}`, async ({ page }) => {
      const transactionPage = new TransactionPage(page);

      // Step 2: Navigate to the Transaction History tab
      await transactionPage.navigateToTransactionHistory();

      // Step 3 & 4: Enter ID and Click Search
      await transactionPage.searchTransaction(data.transactionId);

      // Step 5: Verify no results message
      await expect(transactionPage.noResultsMessage).toBeVisible();
    });
  });
});
```