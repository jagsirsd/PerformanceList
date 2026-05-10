package portfolio;

import org.openjdk.jmh.annotations.*;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PortfolioBenchmark {

    @Param({"10000", "100000", "1000000"})
    public int assetCount;

    private PortfolioManagement portfolio;

    @Setup
    public void setup() {
        portfolio = new PortfolioManagement();
        for (int i = 0; i < assetCount; i++) {
            // Distribute assets across categories
            if (i % 3 == 0) {
                portfolio.addAsset(new Stock("Stock-" + i, new BigDecimal("100.00")));
            } else if (i % 3 == 1) {
                portfolio.addAsset(new Bond("Bond-" + i, new BigDecimal("1000.00")));
            } else {
                portfolio.addAsset(new RestrictedStock("Restricted-" + i, new BigDecimal("500.00")));
            }
        }
    }

    @Benchmark
    public BigDecimal testParallelTotalValue() {
        return portfolio.getTotalValue();
    }

    @Benchmark
    public BigDecimal testSequentialTotalValue() {
        // Accessing the internal implementation for comparison
        return portfolio.getAssets().values().stream()
                .flatMap(s -> StreamSupport.stream(s.spliterator(), false))
                .map(Asset::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
