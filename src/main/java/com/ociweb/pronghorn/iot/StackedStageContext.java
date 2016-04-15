package com.ociweb.pronghorn.iot;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO:
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public final class StackedStageContext {
//Private//////////////////////////////////////////////////////////////////////

    private static final Logger logger = LoggerFactory.getLogger(StackedStageContext.class);

    private final Map<Object, Object> contextVariables = new HashMap<>();

    private final StackedStage stage;

//Public///////////////////////////////////////////////////////////////////////

    public StackedStageContext(StackedStage stage) {
        this.stage = stage;
    }

    public void set(Class<?> classy, Object object) {
        if (classy.isAssignableFrom(object.getClass())) {
            contextVariables.put(classy, object);
        } else {
            logger.error("Context variable [" + object.getClass().getSimpleName() + "] is not an instance of [" + classy.getSimpleName() + "].");
        }
    }

    public void set(String name, Object object) {
        contextVariables.put(name, object);
    }

    public <T> T get(Class<T> classy) {
        try {
            return (T) contextVariables.get(classy);
        } catch (Exception e) {
            logger.debug("Failed to return value of [" + classy.getSimpleName() + "] from the context.", e);
            return null;
        }
    }

    public <T> T get(String name) {
        try {
            return (T) contextVariables.get(name);
        } catch (ClassCastException e) {
            logger.debug("Failed to cast value of [" + name + "] to desired return type.", e);
            return null;
        }
    }

    public Pipe[] getInputPipes() {
        return PronghornStage.NONE;
    }

    public Pipe[] getOutputPipes() {
        return PronghornStage.NONE;
    }

    public StackedStage getStage() {
        return stage;
    }
}
