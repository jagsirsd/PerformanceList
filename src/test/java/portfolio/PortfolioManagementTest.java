package portfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PortfolioManagementTest {
    private PortfolioManagement portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new PortfolioManagement();
    }

    @Test
    void testAddAndContainsAsset() {
        Asset stock = new Stock("Apple", new BigDecimal("150.00"));
        portfolio.addAsset(stock);
        assertTrue(portfolio.containsAsset(stock), "Portfolio should contain the added stock");
    }

    @Test
    void testRemoveAsset() {
        Asset bond = new Bond("US Treasury", new BigDecimal("1000.00"));
        portfolio.addAsset(bond);
        assertTrue(portfolio.containsAsset(bond));
        
        boolean removed = portfolio.removeAsset(bond);
        assertTrue(removed, "Asset should be successfully removed");
        assertFalse(portfolio.containsAsset(bond), "Portfolio should no longer contain the removed asset");
    }

    @Test
    void testTotalValue() {
        portfolio.addAsset(new Stock("Apple", new BigDecimal("150.00")));
        portfolio.addAsset(new Bond("US Treasury", new BigDecimal("1000.00")));
        portfolio.addAsset(new RestrictedStock("Startup", new BigDecimal("500.00")));
        
        // Expected total: 150 + 1000 + 500 = 1650
        assertEquals(0, new BigDecimal("1650.00").compareTo(portfolio.getTotalValue()), 
            "Total value should be 1650.00");
    }

    @Test
    void testTotalValueEmpty() {
        assertEquals(BigDecimal.ZERO, portfolio.getTotalValue(), "Empty portfolio should have zero value");
    }
    
    @Test
    void testCategorization() {
        Asset stock1 = new Stock("Apple", new BigDecimal("150.00"));
        Asset stock2 = new Stock("Google", new BigDecimal("2800.00"));
        Asset bond = new Bond("Corporate", new BigDecimal("500.00"));
        
        portfolio.addAsset(stock1);
        portfolio.addAsset(stock2);
        portfolio.addAsset(bond);
        
        assertEquals(2, portfolio.getAssets().size(), "Should have 2 categories (Stock and Bond)");
        assertEquals(2, portfolio.getAssets().get(Stock.class).size(), "Should have 2 stocks");
        assertEquals(1, portfolio.getAssets().get(Bond.class).size(), "Should have 1 bond");
    }
}
