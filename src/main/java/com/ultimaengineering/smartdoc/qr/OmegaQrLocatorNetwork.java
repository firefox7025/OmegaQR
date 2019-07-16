package com.ultimaengineering.smartdoc.qr;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.zoo.model.TinyYOLO;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;

import java.io.IOException;

@Data
@Slf4j
public class OmegaQrLocatorNetwork {

    private ComputationGraph network;
    private static final int INPUT_WIDTH = 416;
    private static final int INPUT_HEIGHT = 416;
    private static final int CHANNELS = 3;
    private static final int GRID_WIDTH = 13;
    private static final int GRID_HEIGHT = 13;
    private static final int CLASSES_NUMBER = 1;
    private static final int BOXES_NUMBER = 5;
    private static final double[][] PRIOR_BOXES = {{1.5, 1.5}, {2, 2}, {3,3}, {3.5, 8}, {4, 9}};//anchors boxes
    private static final int BATCH_SIZE = 4;
    private static final int EPOCHS = 50;
    private static final double LEARNING_RATE = 0.0001;
    private static final int SEED = 1234;
    private static final double LAMDBA_COORD = 1.0;
    private static final double LAMDBA_NO_OBJECT = 0.5;

    public OmegaQrLocatorNetwork() throws IOException {

        ComputationGraph pretrained = (ComputationGraph) TinyYOLO.builder().build().initPretrained();

        INDArray priors = Nd4j.create(PRIOR_BOXES);
        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .seed(SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
                .gradientNormalizationThreshold(1.0)
                .updater(new RmsProp(LEARNING_RATE))
                .activation(Activation.IDENTITY).miniBatch(true)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .build();

        this.network = new TransferLearning.GraphBuilder(pretrained)
                .fineTuneConfiguration(fineTuneConf)
                .setInputTypes(InputType.convolutional(INPUT_HEIGHT, INPUT_WIDTH, CHANNELS))
                .removeVertexKeepConnections("conv2d_9") // remove his results layer
                .addLayer("convolution2d_9", // replace it with my own
                        new ConvolutionLayer.Builder(1, 1)
                                .nIn(1024)
                                .nOut(BOXES_NUMBER * (5 + CLASSES_NUMBER))
                                .stride(1, 1)
                                .convolutionMode(ConvolutionMode.Same)
                                .weightInit(WeightInit.UNIFORM)
                                .hasBias(false)
                                .activation(Activation.IDENTITY)
                                .build(), "leaky_re_lu_8")
                .addLayer("outputs",
                        new Yolo2OutputLayer.Builder()
                                .lambbaNoObj(LAMDBA_NO_OBJECT)
                                .lambdaCoord(LAMDBA_COORD)
                                .boundingBoxPriors(priors)
                                .build(), "convolution2d_9")
                .setOutputs("outputs")
                .build();

        log.info("\n Model Summary \n" + network.summary());

    }


}
