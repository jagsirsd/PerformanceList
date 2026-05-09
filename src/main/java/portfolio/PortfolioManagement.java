package portfolio;

import java.math.BigDecimal;
import java.util.*;

/**
 * Manages a portfolio of assets, categorized by their specific class.
 */
public class PortfolioManagement {
    private final Map<Class<? extends Asset>, List<Asset>> assets = new HashMap<>();

    /**
     * Adds an asset to the portfolio.
     * @param asset The asset to add.
     */
    public void addAsset(Asset asset) {
        if (asset == null) return;
        assets.computeIfAbsent(asset.getClass(), k -> new ArrayList<>()).add(asset);
    }

    /**
     * Removes an asset from the portfolio.
     * @param asset The asset to remove.
     * @return true if the asset was found and removed, false otherwise.
     */
    public boolean removeAsset(Asset asset) {
        if (asset == null) return false;
        List<Asset> assetList = assets.get(asset.getClass());
        if (assetList != null) {
            boolean removed = assetList.remove(asset);
            if (assetList.isEmpty()) {
                assets.remove(asset.getClass());
            }
            return removed;
        }
        return false;
    }

    /**
     * Checks if the portfolio contains the specified asset.
     * @param asset The asset to check.
     * @return true if the asset exists in the portfolio.
     */
    public boolean containsAsset(Asset asset) {
        if (asset == null) return false;
        List<Asset> assetList = assets.get(asset.getClass());
        return assetList != null && assetList.contains(asset);
    }

    /**
     * Calculates the total value of all assets in the portfolio.
     * @return The sum of the values of all assets.
     */
    public BigDecimal getTotalValue() {
        return assets.values().stream()
                .flatMap(Collection::stream)
                .map(Asset::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns an unmodifiable view of the assets map.
     * @return The assets map.
     */
    public Map<Class<? extends Asset>, List<Asset>> getAssets() {
        return Collections.unmodifiableMap(assets);
    }
}
