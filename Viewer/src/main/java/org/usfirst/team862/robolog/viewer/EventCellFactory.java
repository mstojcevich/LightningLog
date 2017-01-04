package org.usfirst.team862.robolog.viewer;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.usfirst.team862.robolog.shared.LoggerEvent;

import java.text.DateFormat;

public class EventCellFactory implements Callback<ListView<LoggerEvent>, ListCell<LoggerEvent>> {

    private final Background errorBackground = new Background(new BackgroundFill(new Color(1, 0.6, 0.6, 1), CornerRadii.EMPTY, Insets.EMPTY));
    private final Background warningBackground = new Background(new BackgroundFill(new Color(1, 0.8, 0.55, 1), CornerRadii.EMPTY, Insets.EMPTY));
    private final Background exceptionBackground = new Background(new BackgroundFill(new Color(1, 0.6, 0.4, 1), CornerRadii.EMPTY, Insets.EMPTY));
    private final Background infoBackground = new Background(new BackgroundFill(new Color(0.6, 0.6, 0.8, 1), CornerRadii.EMPTY, Insets.EMPTY));
    private final Background otherBackground = new Background(new BackgroundFill(new Color(0.7, 0.7, 0.7, 1), CornerRadii.EMPTY, Insets.EMPTY));

    public EventCellFactory() {
        super();
    }

    @Override
    public ListCell<LoggerEvent> call(ListView<LoggerEvent> param) {
        return new ListCell<LoggerEvent>() {
            @Override
            protected void updateItem(LoggerEvent item, boolean empty) {
                super.updateItem(item, empty);

                if(empty || item == null) {
                    setText(null);
                } else {
                    switch(item.getType()) {
                        case ERROR:
                            setBackground(errorBackground);
                            break;
                        case WARNING:
                            setBackground(warningBackground);
                            break;
                        case EXCEPTION:
                            setBackground(exceptionBackground);
                            break;
                        case INFO:
                            setBackground(infoBackground);
                            break;
                        default:
                            setBackground(otherBackground);
                            break;
                    }

                    setText(String.format("%s\n%s\n%s", item.getType(), item.getTitle(), DateFormat.getInstance().format(item.getTime())));
                }
            }
        };
    }
}
