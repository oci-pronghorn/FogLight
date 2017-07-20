package com.ociweb.iot.maker;



import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.impl.ImageListenerBase;

/**
 * Listener for responding to image-receive events on a hardware system
 * equipped with a camera or other image-generating systems.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
@FunctionalInterface
public interface ImageListener extends Behavior, ImageListenerBase {

}
