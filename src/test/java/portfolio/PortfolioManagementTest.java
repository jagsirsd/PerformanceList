package portfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioManagementTest {
    private PortfolioManagement portfolio;
    private int additionsPerThread;

    @BeforeEach
    void setUp() {
        portfolio = new PortfolioManagement();
    }

    @ParameterizedTest
    @CsvSource({
            "16, 1000",
            "16, 10000",
            "16, 100000",
            //"16, 1000000",
            "8, 1000",
            "8, 10000",
            "8, 100000",
            "8, 1000000",
            "4, 1000",
            "4, 10000",
            "4, 100000",
            "4, 1000000",
            "2, 1000",
            "2, 10000",
            "2, 100000",
            "2, 1000000",

    })
    void testConcurrentAdditions(int numThreads, int additionsPerThread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long start = System.currentTimeMillis();
        IntStream.range(0, numThreads).forEach(t -> {
            executor.submit(() -> {
                for (int i = 0; i < additionsPerThread; i++) {
                    portfolio.addAsset(new Stock("Stock-" + t + "-" + i, new BigDecimal("10.00")));
                }
            });
        });

        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        // Total should be 10 threads * 1000 additions * 10.00 = 100,000.00
        BigDecimal expectedTotal = new BigDecimal("10.00").multiply(new BigDecimal(numThreads)).multiply(new BigDecimal(additionsPerThread));
        assertEquals(0, expectedTotal.compareTo(portfolio.getTotalValue()), 
            "Concurrent total value should be %s, but was ".formatted(expectedTotal) + portfolio.getTotalValue());
        
        long count = StreamSupport.stream(portfolio.getAssets().get(Stock.class).spliterator(), false).count();
        assertEquals((additionsPerThread*numThreads), count, "Should have %d stocks in total".formatted((additionsPerThread*numThreads)));
        long end = System.currentTimeMillis();
        System.out.println("threads: %s, total additions: %s, time taken:%.2f".formatted(numThreads, additionsPerThread, ((double)(end-start))/1000.0));
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
        
        long stockCount = StreamSupport.stream(portfolio.getAssets().get(Stock.class).spliterator(), false).count();
        assertEquals(2, stockCount, "Should have 2 stocks");
        
        long bondCount = StreamSupport.stream(portfolio.getAssets().get(Bond.class).spliterator(), false).count();
        assertEquals(1, bondCount, "Should have 1 bond");
    }
}
