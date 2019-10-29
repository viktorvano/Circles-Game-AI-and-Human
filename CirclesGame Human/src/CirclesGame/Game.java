package CirclesGame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private Timeline timelineMovementHandler;
    private boolean up = false,
                    down = false,
                    right = false,
                    left = false;

    @Override
    public void start(Stage stage)
    {
        pane = new Pane();
        Scene scene = new Scene(pane, width, height);

        stage.setTitle("Circles Human");
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
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.W)
                {
                    up = true;
                }
                if(keyEvent.getCode() == KeyCode.S)
                {
                    down = true;
                }
                if(keyEvent.getCode() == KeyCode.A)
                {
                    left = true;
                }
                if(keyEvent.getCode() == KeyCode.D)
                {
                    right = true;
                }

                if(up && down)
                {
                    up = false;
                    down = false;
                }

                if(right && left)
                {
                    right = false;
                    left = false;
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.W)
                {
                    up = false;
                }
                if(keyEvent.getCode() == KeyCode.S)
                {
                    down = false;
                }
                if(keyEvent.getCode() == KeyCode.A)
                {
                    left = false;
                }
                if(keyEvent.getCode() == KeyCode.D)
                {
                    right = false;
                }
            }
        });

        timelineMovementHandler = new Timeline(new KeyFrame[]{new KeyFrame(Duration.millis(5), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                movementHandler();
            }
        })});

        timelineMovementHandler.setCycleCount(Timeline.INDEFINITE);
        timelineMovementHandler.play();
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

    private void movementHandler()
    {
        if(up)
        {
            if(hunterY>0 && moves>0)
            {
                btnHunter.setLayoutY(--hunterY);
                moves--;
                updateStuff();
            }
        }
        if(down)
        {
            if(hunterY<(height-hunterRadius) && moves>0)
            {
                btnHunter.setLayoutY(++hunterY);
                moves--;
                updateStuff();
            }
        }
        if(left)
        {
            if(hunterX>0 && moves>0)
            {
                btnHunter.setLayoutX(--hunterX);
                moves--;
                updateStuff();
            }
        }
        if(right)
        {
            if(hunterX<(width-hunterRadius) && moves>0)
            {
                btnHunter.setLayoutX(++hunterX);
                moves--;
                updateStuff();
            }
        }
    }

}
