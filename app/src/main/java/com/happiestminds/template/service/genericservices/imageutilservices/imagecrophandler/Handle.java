package com.happiestminds.template.service.genericservices.imageutilservices.imagecrophandler;

import android.graphics.Rect;

/**
 * Created by Barun.Gupta on 1/11/2016.
 */
public enum Handle {
    TOP_LEFT(new CornerHandleHelper(Edge.TOP, Edge.LEFT)),
    TOP_RIGHT(new CornerHandleHelper(Edge.TOP, Edge.RIGHT)),
    BOTTOM_LEFT(new CornerHandleHelper(Edge.BOTTOM, Edge.LEFT)),
    BOTTOM_RIGHT(new CornerHandleHelper(Edge.BOTTOM, Edge.RIGHT)),
    LEFT(new VerticalHandleHelper(Edge.LEFT)),
    TOP(new HorizontalHandleHelper(Edge.TOP)),
    RIGHT(new VerticalHandleHelper(Edge.RIGHT)),
    BOTTOM(new HorizontalHandleHelper(Edge.BOTTOM)),
    CENTER(new CenterHandleHelper());

    // Member Variables ////////////////////////////////////////////////////////

    private HandleHelper mHelper;

    // Constructors ////////////////////////////////////////////////////////////

    Handle(HandleHelper helper) {
        mHelper = helper;
    }

    // Public Methods //////////////////////////////////////////////////////////

    public void updateCropWindow(float x,
                                 float y,
                                 Rect imageRect,
                                 float snapRadius) {

        mHelper.updateCropWindow(x, y, imageRect, snapRadius);
    }

    public void updateCropWindow(float x,
                                 float y,
                                 float targetAspectRatio,
                                 Rect imageRect,
                                 float snapRadius) {

        mHelper.updateCropWindow(x, y, targetAspectRatio, imageRect, snapRadius);
    }
}
