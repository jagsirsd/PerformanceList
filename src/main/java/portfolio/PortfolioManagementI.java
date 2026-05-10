package portfolio;

import java.math.BigDecimal;
import java.util.Map;

public interface PortfolioManagementI {
    int getAvailableProcessors();

    void addAsset(Asset asset);

    boolean removeAsset(Asset asset);

    boolean containsAsset(Asset asset);

    BigDecimal getTotalValue();

    long getAssetsSize();

    Map<Class<? extends Asset>, ? extends Iterable<Asset>> getAssets();
}
