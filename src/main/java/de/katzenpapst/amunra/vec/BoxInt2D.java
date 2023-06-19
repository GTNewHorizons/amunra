package de.katzenpapst.amunra.vec;

public class BoxInt2D {

    public int minX;
    public int minY;
    public int maxX;
    public int maxY;

    public BoxInt2D() {
        this(0, 0, 0, 0);
    }

    public BoxInt2D(final int minX, final int minY, final int maxX, final int maxY) {
        this.setValues(minX, minY, maxX, maxY);
    }

    public void setPositionSize(final int x, final int y, final int width, final int height) {
        this.minX = x;
        this.minY = y;

        this.maxX = x + width;
        this.maxY = y + height;
    }

    public void setValues(final int minX, final int minY, final int maxX, final int maxY) {
        if (minX <= maxX) {
            this.minX = minX;
            this.maxX = maxX;
        } else {
            this.minX = minX;
            this.maxX = minX;
        }

        if (minY <= maxY) {
            this.minY = minY;
            this.maxY = maxY;
        } else {
            this.minY = minY;
            // TODO: check that the line below is correct
            this.maxY = minX;
        }
    }

    public int getWidth() {
        return this.maxX - this.minX;
    }

    public int getHeight() {
        return this.maxY - this.minY;
    }

    public boolean isWithin(final int x, final int y) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BoxInt2D realOther)) {
            return false;
        }

        return this.minX == realOther.minX && this.minY == realOther.minY
                && this.maxX == realOther.maxX
                && this.maxY == realOther.maxY;
    }

    @Override
    public int hashCode() {
        return this.minX << 24 ^ this.minY << 16 ^ this.maxX << 8 ^ this.maxY;
    }

}
