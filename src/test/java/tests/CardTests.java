package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BoardPage;
import pages.ListPage;
import pages.CardPage;
import utils.BaseTest;

import java.time.Duration;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BoardPage;
import pages.ListPage;
import pages.CardPage;
import utils.BaseTest;

import java.time.Duration;

public class CardTests extends BaseTest {

    private BoardPage     boardPage;
    private ListPage      listPage;
    private CardPage      cardPage;
    private WebDriverWait wait;

    private static final String EXISTING_BOARD_NAME = "My Trello Board";
    private static final String EXISTING_LIST_NAME  = "Today";

    // ─────────────────────────────────────────────────────
    // LOCATORS
    // ─────────────────────────────────────────────────────
    private By boardTileLocator(String boardName) {
        return By.xpath(
                "//a[@title='" + boardName + "' and @aria-label='" + boardName + "']"
        );
    }

    private By listHeaderLocator(String listName) {
        return By.xpath(
                "//span[normalize-space(text())='" + listName + "']"
        );
    }

    private By cardNameLocator(String cardTitle) {
        return By.xpath(
                "//a[@data-testid='card-name' and text()='" + cardTitle + "']"
        );
    }

    // ─────────────────────────────────────────────────────
    // BEFORE METHOD
    // Runs before every @Test — BaseTest.setUp() has already
    // created a fresh driver and maximised the window.
    // This method logs in and navigates to the board + list.
    // ─────────────────────────────────────────────────────
    @BeforeMethod
    public void navigateToBoard() {
        // Call parent setup first (creates driver, initializes pages)
        super.setUp();
        
        System.out.println("=================================================");
        System.out.println("SETUP: Logging in and ensuring fixture exists...");
        System.out.println("=================================================");

        performLogin();
        dismissCookieBannerIfPresent();

        wait      = new WebDriverWait(driver, Duration.ofSeconds(20));
        boardPage = new BoardPage(driver);
        listPage  = new ListPage(driver);
        cardPage  = new CardPage(driver);

        // Ensure board exists (uses POM method)
        boolean boardCreated = false;
        if (!dashboardPage.isBoardPresent(EXISTING_BOARD_NAME)) {
            boardCreated = true;
            boardPage.createNewBoard(EXISTING_BOARD_NAME);
        } else {
            dashboardPage.openBoard(EXISTING_BOARD_NAME);
        }

        // Wait for board to load
        wait.until(ExpectedConditions.urlContains("/b/"));
        System.out.println("SETUP: Board opened. URL: " + driver.getCurrentUrl());

        // Ensure list exists (uses POM method)
        listPage.ensureListExists(EXISTING_LIST_NAME);

        System.out.println("=================================================");
        System.out.println("SETUP COMPLETE. Fixture ready.");
        System.out.println("=================================================");
    }


    // ─────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────

    /**
     * Creates a single card in the open list composer.
     * Waits for the textarea, types the title, submits,
     * and waits until the card is visible on the board.
     */
    private void createCard(String cardTitle) {
        System.out.println("PRECONDITION: Creating card '" + cardTitle + "'...");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@data-testid='list-card-composer-textarea']")));
        cardPage.enterCardTitle(cardTitle);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='list-card-composer-add-card-button']")));
        cardPage.clickAddCardSubmit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                cardNameLocator(cardTitle)));

        System.out.println("PRECONDITION: Card '" + cardTitle + "' created.");
    }

    /**
     * Archives a card that is already visible on the board.
     * Opens the card modal, clicks Actions → Archive,
     * closes the modal, and closes the board menu panel.
     */
    private void archiveCard(String cardTitle) {
        System.out.println("PRECONDITION: Archiving card '" + cardTitle + "'...");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                cardNameLocator(cardTitle)));

        cardPage.openCard(cardTitle);
        cardPage.waitForCardModalToOpen();
        cardPage.clickActionsButton();
        cardPage.clickArchiveFromActions();
        cardPage.closeCard();
        cardPage.waitForCardModalToClose();

        System.out.println("PRECONDITION: Card '" + cardTitle + "' archived.");
    }


    // ─────────────────────────────────────────────────────
    // BM-003: Create Single Card
    // ─────────────────────────────────────────────────────
    @Test(description = "BM-003: Create a new card inside the existing list")
    public void test01_createCard() {
        String cardTitle = "My Automated Card";

        System.out.println("=================================================");
        System.out.println("RUNNING: BM-003 — Create Single Card");
        System.out.println("=================================================");

        System.out.println("STEP 1: Entering card title: " + cardTitle);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@data-testid='list-card-composer-textarea']")));

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@data-testid='list-card-composer-textarea']")));
        cardPage.enterCardTitle(cardTitle);

        System.out.println("STEP 2: Submitting card...");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='list-card-composer-add-card-button']")));
        cardPage.clickAddCardSubmit();

        System.out.println("STEP 3: Verifying card on board...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                cardNameLocator(cardTitle)));

        Assert.assertTrue(
                cardPage.isCardCreated(cardTitle),
                "❌ Card '" + cardTitle + "' was NOT found on the board!"
        );

        System.out.println("✅ BM-003 PASSED: Card '" + cardTitle + "' created!");
    }


    // ─────────────────────────────────────────────────────
    // KAN-31: Create Multiple Cards
    // ─────────────────────────────────────────────────────
    @Test(description = "KAN-31: Create multiple cards (3 cards) inside the existing list")
    public void test02_createMultipleCards() {
        System.out.println("=================================================");
        System.out.println("RUNNING: KAN-31 — Create Multiple Cards");
        System.out.println("=================================================");

        String[] cardTitles = {
                "Automated Card 1",
                "Automated Card 2",
                "Automated Card 3"
        };

        for (int i = 0; i < cardTitles.length; i++) {
            String cardTitle = cardTitles[i];
            System.out.println("Creating Card " + (i + 1) + ": " + cardTitle);

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[@data-testid='list-card-composer-textarea']")));
            cardPage.enterCardTitle(cardTitle);

            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-testid='list-card-composer-add-card-button']")));
            cardPage.clickAddCardSubmit();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    cardNameLocator(cardTitle)));

            Assert.assertTrue(
                    cardPage.isCardCreated(cardTitle),
                    "❌ Card '" + cardTitle + "' was NOT found!"
            );
            System.out.println("✅ Card created: " + cardTitle);
        }

        System.out.println("✅ KAN-31 PASSED: All 3 cards created!");
    }


    // ─────────────────────────────────────────────────────
    // BM-004: Archive Single Card
    // ─────────────────────────────────────────────────────
    @Test(description = "BM-004: Archive a single card via Actions and verify in Archived Items")
    public void test03_archiveSingleCard() {
        String cardTitle = "My Automated Card";

        System.out.println("=================================================");
        System.out.println("RUNNING: BM-004 — Archive Single Card");
        System.out.println("=================================================");

        // ── Precondition: card must exist before archiving ────────────────
        createCard(cardTitle);

        System.out.println("STEP 1: Waiting for card '" + cardTitle + "'...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                cardNameLocator(cardTitle)));

        System.out.println("STEP 2: Opening card...");
        cardPage.openCard(cardTitle);

        System.out.println("STEP 3: Clicking Actions...");
        cardPage.clickActionsButton();

        System.out.println("STEP 4: Clicking Archive...");
        cardPage.clickArchiveFromActions();
        cardPage.closeCard();
        cardPage.waitForCardModalToClose();

        System.out.println("STEP 5: Opening Board Menu...");
        boardPage.openBoardMenu();

        System.out.println("STEP 6: Opening Archived Items...");
        boardPage.openArchivedItems();
        boardPage.waitForArchivedItemsPanel();

        System.out.println("STEP 7: Verifying card in Archived Items...");
        Assert.assertTrue(
                cardPage.isArchivedCardListed(cardTitle),
                "❌ Card '" + cardTitle + "' NOT found in Archived Items!"
        );

        // ── Close panel cleanly so browser state is tidy on teardown ─────
        boardPage.closeBoardMenu();

        System.out.println("✅ BM-004 PASSED: Card archived and verified!");
    }


    // ─────────────────────────────────────────────────────
    // KAN-32: Archive Multiple Cards
    // ─────────────────────────────────────────────────────
    @Test(description = "KAN-32: Archive multiple cards via Actions and verify in Archived Items")
    public void test04_archiveMultipleCards() {
        System.out.println("=================================================");
        System.out.println("RUNNING: KAN-32 — Archive Multiple Cards");
        System.out.println("=================================================");

        String[] cardTitles = {
                "Automated Card 1",
                "Automated Card 2",
                "Automated Card 3"
        };

        // ── Precondition: create all cards first ──────────────────────────
        System.out.println("PRECONDITION: Creating all cards...");
        for (String title : cardTitles) {
            createCard(title);
        }
        System.out.println("PRECONDITION: All cards created.");

        // ── Archive and verify each card ──────────────────────────────────
        for (int i = 0; i < cardTitles.length; i++) {
            String cardTitle = cardTitles[i];
            System.out.println("-------------------------------------------");
            System.out.println("Archiving Card " + (i + 1) + ": " + cardTitle);

            System.out.println("STEP 1: Waiting for card...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    cardNameLocator(cardTitle)));

            System.out.println("STEP 2: Opening card...");
            cardPage.openCard(cardTitle);
            cardPage.waitForCardModalToOpen();

            System.out.println("STEP 3: Clicking Actions...");
            cardPage.clickActionsButton();

            System.out.println("STEP 4: Clicking Archive...");
            cardPage.clickArchiveFromActions();
            cardPage.closeCard();
            cardPage.waitForCardModalToClose();

            System.out.println("STEP 5: Opening Board Menu...");
            boardPage.openBoardMenu();

            System.out.println("STEP 6: Opening Archived Items...");
            boardPage.openArchivedItems();
            boardPage.waitForArchivedItemsPanel();

            System.out.println("STEP 7: Verifying card in Archived Items...");
            Assert.assertTrue(
                    cardPage.isArchivedCardListed(cardTitle),
                    "❌ Card '" + cardTitle + "' NOT found in Archived Items!"
            );

            // ── Close panel before next iteration ────────────────────────
            boardPage.closeBoardMenu();

            System.out.println("✅ Card " + (i + 1) + " archived and verified: " + cardTitle);
        }

        System.out.println("✅ KAN-32 PASSED: All 3 cards archived and verified!");
    }


    // ─────────────────────────────────────────────────────
    // BM-005: Delete Single Archived Card
    // ─────────────────────────────────────────────────────
    @Test(description = "BM-005: Delete a single archived card from Archived Items and verify it is gone")
    public void test05_deleteSingleArchivedCard() {
        String cardTitle = "My Automated Card";

        System.out.println("=================================================");
        System.out.println("RUNNING: BM-005 — Delete Single Archived Card");
        System.out.println("=================================================");

        // ── Precondition: card must exist and be archived ─────────────────
        createCard(cardTitle);
        archiveCard(cardTitle);

        // ── Step 1: Open Board Menu ───────────────────────────────────────
        System.out.println("STEP 1: Opening Board Menu...");
        boardPage.openBoardMenu();

        // ── Step 2: Open Archived Items panel ────────────────────────────
        System.out.println("STEP 2: Opening Archived Items panel...");
        boardPage.openArchivedItems();
        boardPage.waitForArchivedItemsPanel();
        System.out.println("STEP 2: Archived Items panel is open.");

        // ── Step 3: Wait for the target archived card to appear ───────────
        System.out.println("STEP 3: Waiting for archived card '" + cardTitle + "' to appear...");
        boardPage.waitForArchivedCardToAppear(cardTitle);
        System.out.println("STEP 3: Archived card '" + cardTitle + "' is visible.");

        // ── Step 4: Click Delete button for the archived card ─────────────
        System.out.println("STEP 4: Clicking Delete button for: " + cardTitle);
        boardPage.clickDeleteButtonForArchivedCard(cardTitle);
        System.out.println("STEP 4: Delete button clicked.");

        // ── Step 5: Confirm deletion in the popup dialog ──────────────────
        System.out.println("STEP 5: Confirming deletion...");
        boardPage.confirmCardDeletion(cardTitle);
        System.out.println("STEP 5: Deletion confirmed.");

        // ── Step 6: Verify the card is gone from Archived Items ───────────
        System.out.println("STEP 6: Verifying card '" + cardTitle + "' is removed from Archived Items...");
        Assert.assertTrue(
                boardPage.isCardDeletedFromArchivedItems(cardTitle),
                "❌ Card '" + cardTitle + "' is STILL in Archived Items after deletion!"
        );

        System.out.println("✅ BM-005 PASSED: Card '" + cardTitle + "' permanently deleted!");
        System.out.println("=================================================");
    }


    // ─────────────────────────────────────────────────────
    // KAN-33: Delete Multiple Archived Cards
    // ─────────────────────────────────────────────────────
    @Test(description = "KAN-33: Delete multiple archived cards from Archived Items and verify each is gone")
    public void test06_deleteMultipleArchivedCards() {
        System.out.println("=================================================");
        System.out.println("RUNNING: KAN-33 — Delete Multiple Archived Cards");
        System.out.println("=================================================");

        String[] cardTitles = {
                "Automated Card 1",
                "Automated Card 2",
                "Automated Card 3"
        };

        // ── Precondition: create then archive all cards ───────────────────
        System.out.println("PRECONDITION: Creating all cards...");
        for (String title : cardTitles) {
            createCard(title);
        }

        System.out.println("PRECONDITION: Archiving all cards...");
        for (String title : cardTitles) {
            archiveCard(title);
        }
        System.out.println("PRECONDITION: All cards archived.");

        // ── Delete and verify each card ───────────────────────────────────
        for (int i = 0; i < cardTitles.length; i++) {
            String cardTitle = cardTitles[i];

            System.out.println("-------------------------------------------");
            System.out.println("Deleting Archived Card " + (i + 1) + ": " + cardTitle);

            // ── Step 1: Open Board Menu ───────────────────────────────────
            System.out.println("STEP 1: Opening Board Menu...");
            boardPage.openBoardMenu();

            // ── Step 2: Open Archived Items panel ────────────────────────
            System.out.println("STEP 2: Opening Archived Items panel...");
            boardPage.openArchivedItems();
            boardPage.waitForArchivedItemsPanel();
            System.out.println("STEP 2: Archived Items panel is open.");

            // ── Step 3: Wait for target archived card to appear ───────────
            System.out.println("STEP 3: Waiting for archived card '" + cardTitle + "'...");
            boardPage.waitForArchivedCardToAppear(cardTitle);
            System.out.println("STEP 3: Card '" + cardTitle + "' found in archived panel.");

            // ── Step 4: Click Delete button ───────────────────────────────
            System.out.println("STEP 4: Clicking Delete button for: " + cardTitle);
            boardPage.clickDeleteButtonForArchivedCard(cardTitle);
            System.out.println("STEP 4: Delete button clicked.");

            // ── Step 5: Confirm deletion ──────────────────────────────────
            System.out.println("STEP 5: Confirming deletion...");
            boardPage.confirmCardDeletion(cardTitle);
            System.out.println("STEP 5: Deletion confirmed.");

            // ── Step 6: Verify card is gone from Archived Items ───────────
            System.out.println("STEP 6: Verifying '" + cardTitle + "' removed from Archived Items...");
            Assert.assertTrue(
                    boardPage.isCardDeletedFromArchivedItems(cardTitle),
                    "❌ Card '" + cardTitle + "' is STILL in Archived Items after deletion!"
            );

            // ── Close panel before next iteration ────────────────────────
            boardPage.closeBoardMenu();

            System.out.println("✅ Card " + (i + 1) + " deleted: " + cardTitle);
        }

        System.out.println("-------------------------------------------");
        System.out.println("✅ KAN-33 PASSED: All 3 archived cards deleted and verified!");
        System.out.println("=================================================");
    }
}