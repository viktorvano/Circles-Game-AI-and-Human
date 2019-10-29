package CirclesGame;

import FFNN.NeuralNetwork;
import FFNN.TrainingData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedList;

import static FFNN.GeneralFunctions.showVectorValues;
import static FFNN.Variables.*;
import static FFNN.Variables.result;
import static FFNN.Weights.*;

public class Game extends Application {
    public static void main(String[] args)
    {
        launch(args);
    }

    private Pane pane;
    private Button btnHunter;
    private Button btnFood;
    private Label lblScore;
    private Label lblMoves;
    private final int movesInitValue = 1200;
    private int score = 0;
    private int moves = movesInitValue;
    private final int width = 750;
    private final int height = 750;
    private int hunterX = width/2;
    private int hunterY = height/2;
    private final int hunterRadius = 15;
    private final int foodRadius = 5;
    private Timeline timelineNeuralNetTrain;
    private Timeline timelineNeuralNetRun;
    private NeuralNetwork myNet;

    @Override
    public void start(Stage stage)
    {
        pane = new Pane();
        Scene scene = new Scene(pane, width, height);

        stage.setTitle("Circles AI");
        stage.setScene(scene);
        stage.show();
        stage.setMaxWidth(stage.getWidth());
        stage.setMinWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setMinHeight(stage.getHeight());
        stage.setResizable(false);

        lblScore = new Label("Score: " + score);
        lblMoves = new Label("Moves: " + moves);
        lblMoves.setLayoutY(20);

        btnHunter = new Button("");
        btnHunter.setShape(new Circle(hunterRadius));
        btnHunter.setMaxSize(hunterRadius, hunterRadius);
        btnHunter.setMinSize(hunterRadius, hunterRadius);
        btnHunter.setLayoutX(hunterX);
        btnHunter.setLayoutY(hunterY);
        btnHunter.setStyle("-fx-background-color: #ff0000; ");

        btnFood = new Button("");
        btnFood.setShape(new Circle(foodRadius));
        btnFood.setMaxSize(foodRadius, foodRadius);
        btnFood.setMinSize(foodRadius, foodRadius);
        respawnFood();
        btnFood.setStyle("-fx-background-color: #0000ff; ");

        pane.getChildren().addAll(btnHunter, btnFood, lblScore, lblMoves);


        timelineNeuralNetRun = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(5), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(moves>0) runCycle();
            }
        })});

        timelineNeuralNetTrain = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                trainNeuralNet();
                timelineNeuralNetRun.setCycleCount(Timeline.INDEFINITE);
                timelineNeuralNetRun.play();
            }
        })});

        timelineNeuralNetTrain.setCycleCount(1);
        timelineNeuralNetTrain.play();
    }

    private void respawnFood()
    {
        double foodX, foodY, hX, hY;
        hX = btnHunter.getLayoutX();
        hY = btnHunter.getLayoutY();

        do
        {
            foodX = Math.random()*(width-hunterRadius*2) + hunterRadius;
        }while (foodX>foodRadius && foodX<(hX-foodRadius) && foodX>(hX+hunterRadius*2+foodRadius));

        do
        {
            foodY = Math.random()*(height-hunterRadius*2) + hunterRadius;
        }while (foodY>foodRadius && foodY<(hY-foodRadius) && foodY>(hY+hunterRadius*2+foodRadius));

        btnFood.setLayoutX(foodX);
        btnFood.setLayoutY(foodY);
    }

    private boolean detectCollision()
    {
        if((btnFood.getLayoutY()+foodRadius)>=btnHunter.getLayoutY() && (btnHunter.getLayoutY()+hunterRadius)>=btnFood.getLayoutY()
                && (btnFood.getLayoutX()+foodRadius)>=btnHunter.getLayoutX() && (btnHunter.getLayoutX()+hunterRadius)>=btnFood.getLayoutX())
        {
            return true;
        }
        return false;
    }

    private void updateStuff()
    {
        if(detectCollision())
        {
            respawnFood();
            moves = movesInitValue;
            score++;
            lblScore.setText("Score: " + score);
        }

        lblMoves.setText("Moves: " + moves);

        if(moves == 0)
        {
            Label lblGameOver = new Label("GAME OVER");
            lblGameOver.setLayoutX((width-170)/2);
            lblGameOver.setLayoutY((height-50)/2);
            lblGameOver.setFont(Font.font(32));
            pane.getChildren().add(lblGameOver);
        }

    }

    private void trainNeuralNet()
    {
        TrainingData trainData = new TrainingData();
        loadTopology();
        if (topology.size() < 3)
        {
            System.out.println("Topology ERROR:\nTopology is too short, may miss some layer.");
            return;
        }

        myNet = new NeuralNetwork(topology);

        input = new LinkedList<>();
        target = new LinkedList<>();
        result = new LinkedList<>();
        input.clear();
        target.clear();
        result.clear();

        if(weights.size() != get_number_of_weights_from_file())
        {
            load_training_data_from_file();

            System.out.println("Training started\n");
            while (true)
            {
                trainingPass++;
                System.out.println("Pass: " + trainingPass);

                //Get new input data and feed it forward:
                trainData.getNextInputs(input);
                showVectorValues("Inputs:", input);
                myNet.feedForward(input);

                // Train the net what the outputs should have been:
                trainData.getTargetOutputs(target);
                showVectorValues("Targets: ", target);
                assert(target.size() == topology.peekLast());
                myNet.backProp(target);//This function alters neurons

                // Collect the net's actual results:
                myNet.getResults(result);
                showVectorValues("Outputs: ", result);


                // Report how well the training is working, averaged over recent samples:
                System.out.println("Net recent average error: " + myNet.getRecentAverageError() + "\n\n");

                if (myNet.getRecentAverageError() < 0.001 && trainingPass>2000)
                {
                    System.out.println("Exit due to low error :D\n\n");
                    myNet.saveNeuronWeights();
                    break;
                }
            }
            System.out.println("Training done.\n");
        }else
        {
            myNet.loadNeuronWeights();
            System.out.println("Weights were loaded from file.\n");
        }

        System.out.println("Run mode begin\n");
        trainingPass = 0;
    }

    private void runCycle()
    {
        //trainingPass++;
        //System.out.println("Run: " + trainingPass);

        //Get new input data and feed it forward:
        //Make sure that your input data are the same size as InputNodes
        input.clear();
        if(btnFood.getLayoutY()<btnHunter.getLayoutY())
            input.add(1.0);
        else
            input.add(0.0);

        if(btnFood.getLayoutX()>btnHunter.getLayoutX())
            input.add(1.0);
        else
            input.add(0.0);

        //showVectorValues("Inputs:", input);
        myNet.feedForward(input);

        // Collect the net's actual results:
        myNet.getResults(result);
        //showVectorValues("Outputs: ", result);

        --moves;

        if(result.get(0) >= 0.5)
        {
            if(hunterY>0 && moves>=0)
            {
                btnHunter.setLayoutY(--hunterY);
                updateStuff();
            }
        }
        if(result.get(0) < 0.5)
        {
            if(hunterY<(height-hunterRadius) && moves>=0)
            {
                btnHunter.setLayoutY(++hunterY);
                updateStuff();
            }
        }
        if(result.get(1) >= 0.5)
        {
            if(hunterX<(width-hunterRadius) && moves>=0)
            {
                btnHunter.setLayoutX(++hunterX);
                updateStuff();
            }
        }
        if(result.get(1) < 0.5)
        {
            if(hunterX>0 && moves>=0)
            {
                btnHunter.setLayoutX(--hunterX);
                updateStuff();
            }
        }
    }
}
