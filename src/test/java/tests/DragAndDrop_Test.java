package tests;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.BoardPage;
import pages.CardPage;
import utils.BaseTest;

import java.util.List;

public class DragAndDrop_Test extends BaseTest {

    private static final String BOARD_NAME = "Trello Project";

    /**
     * Runs once before all tests in this class.
     * Opens one browser, logs in, and navigates to the board.
     * Overrides BaseTest @BeforeMethod / @AfterMethod so that the browser
     * is NOT recreated between tests.
     */
    @BeforeClass
    @Override
    public void setUp() {
        super.setUp();                   // initialises config, driver, loginPage, dashboardPage
        performLogin();
        dismissCookieBannerIfPresent();
        dashboardPage.openBoard(BOARD_NAME);
    }

    /** Runs once after all tests in this class — quits the shared browser. */
    @AfterClass
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ── TC-1: verify the project board opens correctly ───────────────────────
    @Test(priority = 1)
    public void openBoard() {
        BoardPage boardPage = new BoardPage(driver);
        Assert.assertEquals(boardPage.getBoardTitle(), BOARD_NAME,
                "Board title should match after opening");
    }

    // ── TC-2: drag a card from one list to another ───────────────────────────
    @Test(priority = 2)
    public void DragAndDrop_CardFromOneListToOther() {
        String cardName   = "Create test cases";
        String sourceList = "To Do";
        String targetList = "Doing";

        CardPage cardPage = new CardPage(driver);
        cardPage.dragCardToList(cardName, sourceList, targetList);

        Assert.assertEquals(
                cardPage.countCardsInList(cardName, sourceList), 0,
                "Card '" + cardName + "' should no longer appear in '" + sourceList + "'"
        );
        Assert.assertTrue(
                cardPage.isCardInList(cardName, targetList),
                "Card '" + cardName + "' should now appear in '" + targetList + "'"
        );
    }

    // ── TC-3: drag a card within the same list to reorder it ─────────────────
    @Test(priority = 3, enabled = true)
    public void DragAndDrop_CardInAList() {
        String sourceCard = "implement test cases";
        String targetCard = "Prepare test script";
        String listName   = "To Do";

        CardPage cardPage = new CardPage(driver);
        int indexBefore = cardPage.getCardIndexInList(sourceCard, listName);

        cardPage.dragCardInList(sourceCard, targetCard, listName);

        int indexAfter = cardPage.getCardIndexInList(sourceCard, listName);
        Assert.assertNotEquals(indexAfter, indexBefore,
                "Card '" + sourceCard + "' should have changed position within the list");
    }

    // ── TC-4: drag an entire list to reorder it on the board ─────────────────
    @Test(priority = 4, enabled = true)
    public void DragAndDrop_EntireList() {
        String sourceList = "Doing";
        String destList   = "Done";

        BoardPage boardPage = new BoardPage(driver);
        List<String> namesBefore = boardPage.getListOrder();
        int indexBefore = namesBefore.indexOf(sourceList);

        boardPage.dragListToPosition(sourceList, destList);
        boardPage.waitForListReorder(sourceList, indexBefore);

        List<String> namesAfter = boardPage.getListOrder();
        int indexAfter = namesAfter.indexOf(sourceList);

        Assert.assertTrue(namesAfter.contains(sourceList),
                "List '" + sourceList + "' should still exist after reordering");
        Assert.assertTrue(namesAfter.contains(destList),
                "List '" + destList + "' should still exist after reordering");
        Assert.assertNotEquals(indexAfter, indexBefore,
                "List '" + sourceList + "' should have changed position on the board");
    }

    // ── TC-5: drag card to invalid zone — should return to original position ──
    @Test(priority = 5, enabled = true)
    public void DragCard_OutsideScope_ReturnsToPosition() {
        String cardText = "Prepare test script";

        CardPage cardPage = new CardPage(driver);

        // Find which list the card is currently in (does not assume a fixed list)
        String listName = cardPage.findListContainingCard(cardText);
        Assert.assertNotNull(listName,
                "Card '" + cardText + "' was not found on the board before drag");

        int indexBefore = cardPage.getCardIndexInList(cardText, listName);

        cardPage.dragCardToInvalidTarget(cardText, listName);

        waitUntil(d -> cardPage.getCardIndexInList(cardText, listName) >= 0);

        int indexAfter = cardPage.getCardIndexInList(cardText, listName);
        Assert.assertTrue(indexAfter >= 0, "Card should still be present in the original list");
        Assert.assertEquals(indexAfter, indexBefore,
                "Card should return to its original position after invalid drag");
    }
}





