package com.ociweb.pronghorn.iot;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper API to make writing linked ("stacked") stages easier.
 *
 * You can either extend this class directly when writing stages,
 * or wrap existing {@link PronghornStage}s in this class to
 * make them easily compatible with other stacked stages.
 *
 * You should not use this class when writing a single-stage
 * application; for that, just extend {@link PronghornStage} directly.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class StackedStage extends PronghornStage {
//Private//////////////////////////////////////////////////////////////////////

    //Context of this stage.
    private final StackedStageContext context = new StackedStageContext(this);
    private final Map<String, Pipe> inputPipes = new HashMap<>();
    private final Map<String, Pipe> outputPipes = new HashMap<>();
    private final Map<String, StackedStage> inputStages;
    private final Map<String, StackedStage> outputStages;

    //Methods.
    private StackedStageCreator creator;
    private StackedStageUpdater updater;
    private StackedStageDestroyer destroyer;

    //TODO: These functions could be deduplicated using some Java 8 APIs and lambdas.
    private static Pipe[] processAllInputPipesForStages(Map<String, StackedStage> stages) {
        int size = 0;
        for (StackedStage stackedStage : stages.values()) {
            size += stackedStage.getInputPipes().values().size();
        }

        Pipe[] pipes = new Pipe[size];
        int cursor = 0;
        for (StackedStage stackedStage : stages.values()) {
            for (Pipe pipe : stackedStage.getInputPipes().values()) {
                pipes[cursor] = pipe;
                cursor += 1;
            }
        }

        return pipes;
    }

    private static Pipe[] processAllOutputPipesForStages(Map<String, StackedStage> stages) {
        int size = 0;
        for (StackedStage stackedStage : stages.values()) {
            size += stackedStage.getOutputPipes().values().size();
        }

        Pipe[] pipes = new Pipe[size];
        int cursor = 0;
        for (StackedStage stackedStage : stages.values()) {
            for (Pipe pipe : stackedStage.getOutputPipes().values()) {
                pipes[cursor] = pipe;
                cursor += 1;
            }
        }

        return pipes;
    }

    private StackedStage(GraphManager gm,
                         Map<String, StackedStage> inputs, Map<String, StackedStage> outputs,
                         Pipe[] inputPipes, Pipe[] outputPipes,
                         StackedStageCreator creator,
                         StackedStageUpdater updater,
                         StackedStageDestroyer destroyer) {
        super(gm, inputPipes, outputPipes);
        this.inputStages = inputs;
        this.outputStages = outputs;
        this.creator = creator;
        this.updater = updater;
        this.destroyer = destroyer;
    }

//Protected////////////////////////////////////////////////////////////////////

    public void onCreate(StackedStageContext context) {
        if (creator != null) {
            creator.create(context);
        }
    }

    public void onUpdate(StackedStageContext context) {
        if (updater != null) {
            updater.update(context);
        }
    }

    public void onDestroy(StackedStageContext context) {
        if (destroyer != null) {
            destroyer.destroy(context);
        }
    }

//Public///////////////////////////////////////////////////////////////////////

    public static final class Builder {
        private StackedStageCreator creator = null;
        private StackedStageUpdater updater = null;
        private StackedStageDestroyer destroyer = null;
        private Map<String, StackedStage> inputs = new HashMap<>();
        private Map<String, StackedStage> outputs = new HashMap<>();

        public Builder input(String name, StackedStage stage) {
            inputs.put(name, stage);
            return this;
        }

        public Builder output(String name, StackedStage stage) {
            outputs.put(name, stage);
            return this;
        }

        public Builder create(StackedStageCreator creator) {
            this.creator = creator;
            return this;
        }

        public Builder update(StackedStageUpdater updater) {
            this.updater = updater;
            return this;
        }

        public Builder destroy(StackedStageDestroyer destroyer) {
            this.destroyer = destroyer;
            return this;
        }

        public StackedStage build(GraphManager gm) {
            //These are intentionally flipped; the output pipes for our input stages are our inputs pipes, and vice-versa.
            return new StackedStage(gm, inputs, outputs,
                                    processAllOutputPipesForStages(inputs),
                                    processAllInputPipesForStages(outputs),
                                    creator, updater, destroyer);
        }
    }

    public final Map<String, Pipe> getInputPipes() {
        return inputPipes;
    }

    public final Map<String, Pipe> getOutputPipes() {
        return outputPipes;
    }

    @Override public final void startup() {
        if (creator != null) {
            creator.create(context);
        }
    }

    @Override public final void run() {
        if (updater != null) {
            updater.update(context);
        }
    }

    @Override public final void shutdown() {
        if (destroyer != null) {
            destroyer.destroy(context);
        }
    }
}
