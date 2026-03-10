package com.pixelspore.grefsenveien;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Template;

public class ConfirmationScreen extends Screen {

    public ConfirmationScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        return new MessageTemplate.Builder("Request sent successfully!")
            .setTitle("Success")
            .setHeaderAction(Action.BACK)
            .build();
    }
}
