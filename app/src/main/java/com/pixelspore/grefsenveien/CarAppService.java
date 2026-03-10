package com.pixelspore.grefsenveien;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public class CarAppService extends androidx.car.app.CarAppService {

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // Allow all hosts for testing. For production, you should validate specific hosts.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                return new MainCarScreen(getCarContext());
            }
        };
    }
}
