package com.kazurayam.materials

import java.awt.image.BufferedImage

class ImageMetrics {

    private int width_
    private int height_
    
    ImageMetrics(BufferedImage bufferedImage) {
        this.width_ = bufferedImage.getWidth()
        this.height_ = bufferedImage.getHeight()
    }
    
    int getWidth() {
        return width_
    }
    
    int getHeight() {
        return height_
    }
}
