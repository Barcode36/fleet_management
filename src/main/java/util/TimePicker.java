package main.java.util;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceBox;
import main.java.model.TimePeriod;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimePicker {
    private ChoiceBox<String> hourChoiceBox, minuteChoiceBox;
    private ChoiceBox<TimePeriod> timePeriodChoiceBox;
    public TimePicker(ChoiceBox<String> hourChoiceBox, ChoiceBox<String> minuteChoiceBox, ChoiceBox<TimePeriod>
            timePeriodChoiceBox) {
        this.hourChoiceBox = hourChoiceBox;
        this.minuteChoiceBox = minuteChoiceBox;
        this.timePeriodChoiceBox = timePeriodChoiceBox;
        minuteChoiceBox.setItems(DateUtil.generateMinutes());
        hourChoiceBox.setItems(DateUtil.generateHours());
        timePeriodChoiceBox.setItems(FXCollections.observableArrayList(TimePeriod.values()));
    }

    public void configureTime() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {

            Task<Map<String, String>> task = new Task<Map<String, String>>() {
                @Override
                protected Map<String, String> call() throws Exception {
                    return DateUtil.tokenizeTime(LocalTime.now());
                }
            };

            task.setOnSucceeded(event -> {
                Map<String, String> map = task.getValue();
                minuteChoiceBox.getSelectionModel().select(map.get("Minute"));
                hourChoiceBox.getSelectionModel().select(map.get("Hour"));

                timePeriodChoiceBox.getSelectionModel().select(TimePeriod.valueOf(map.get("Period")));

            });
            new Thread(task).start();
        }, 0, 1, TimeUnit.MINUTES);
    }
    public LocalTime getSelectedTime() {
        if (hourChoiceBox.getValue() == null || minuteChoiceBox.getValue() == null || timePeriodChoiceBox.getValue() == null) {
            return null;
        }
        String time = hourChoiceBox.getValue() + ":" + minuteChoiceBox.getValue() + " " + timePeriodChoiceBox
                .getValue();
        return DateUtil.parseTime(time);
    }

    public void setTime(LocalTime timeCreated) {
        String [] tokens = DateUtil.formatTime(timeCreated).split(" ");
        hourChoiceBox.setValue(tokens[0].split(":")[0]);
        minuteChoiceBox.setValue(tokens[0].split(":")[1]);
        timePeriodChoiceBox.setValue(TimePeriod.valueOf(tokens[1]));
    }
}
