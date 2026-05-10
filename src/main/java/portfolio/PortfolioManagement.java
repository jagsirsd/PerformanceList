package portfolio;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * Manages a portfolio of assets, categorized by their specific class.
 * This implementation is thread-safe and optimized for high-concurrency additions
 * using a segmented data structure.
 */
public class PortfolioManagement implements PortfolioManagementI {
    @Override
    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private final Map<Class<? extends Asset>, SegmentedList<Asset>> assets = new ConcurrentHashMap<>();

    private Iterable<Asset> getAllAssets() {
        return assets.get(Stock.class);//TODO: should be all assets
    }
    /**
     * Adds an asset to the portfolio.
     * @param asset The asset to add.
     */
    @Override
    public void addAsset(Asset asset) {
        if (asset == null) return;
        assets.computeIfAbsent(asset.getClass(), k -> new SegmentedList<>()).add(asset);
    }



    /**
     * Removes an asset from the portfolio.
     * @param asset The asset to remove.
     * @return true if the asset was found and removed, false otherwise.
     */
    @Override
    public boolean removeAsset(Asset asset) {
        if (asset == null) return false;
        SegmentedList<Asset> assetList = assets.get(asset.getClass());
        if (assetList != null) {
            return assetList.remove(asset);
        }
        return false;
    }

    /**
     * Checks if the portfolio contains the specified asset.
     * @param asset The asset to check.
     * @return true if the asset exists in the portfolio.
     */
    @Override
    public boolean containsAsset(Asset asset) {
        if (asset == null) return false;
        SegmentedList<Asset> assetList = assets.get(asset.getClass());
        return assetList != null && assetList.contains(asset);
    }

    /**
     * Calculates the total value of all assets in the portfolio.
     * Uses parallel streams to process all asset categories and segments concurrently.
     * @return The sum of the values of all assets.
     */
    @Override
    public BigDecimal getTotalValue() {
        return assets.values().parallelStream()
                .flatMap(SegmentedList::parallelStream)
                .map(Asset::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long getAssetsSize() {
        return assets.values().stream().mapToLong(SegmentedList::size).sum();
    }

    /**
     * Returns an unmodifiable view of the assets map.
     * Each value is a SegmentedList acting as an Iterable collection of assets.
     * @return The assets map.
     */
    @Override
    public Map<Class<? extends Asset>, ? extends Iterable<Asset>> getAssets() {
        return Collections.unmodifiableMap(assets);
    }
}
