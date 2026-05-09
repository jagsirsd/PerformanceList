package portfolio;

import java.math.BigDecimal;

public abstract class Asset {
    private String name;
    private BigDecimal value;

    public Asset(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', value=%s}", getClass().getSimpleName(), name, value);
    }
}
