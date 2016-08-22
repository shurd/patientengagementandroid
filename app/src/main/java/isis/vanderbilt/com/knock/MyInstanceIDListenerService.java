package isis.vanderbilt.com.knock;

/**
 * Created by Sam on 3/23/2016.
 */
import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        //TODO: update registration
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}