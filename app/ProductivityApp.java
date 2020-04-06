public class ProductivityApp extends Application {

    public ProductivityApp() {
        // If a certain permission is not granted <------ FIX ASAP
        Log.i(TAG, "Checking for usage stats permission...");
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), this.getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            Log.i(TAG, "Usage stats permission not granted. Asking for permission...");
            // Permission is not granted
            // Display dialog message
            confirmSettingsDialog(
                    "You have not granted this app permission to track app usage on your phone. This feature is necessary for us to track the app you are currently using and help you stay productive in that way. The application will not employ your usage data in ways you may not wish to happen, and the permission can be deactivated at any time in Settings.",
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                    false);
        } else {
            Log.i(TAG, "Usage stats permission granted!");
        }
        Log.i(TAG, "Checking for window overlay permission...");
        if(!Settings.canDrawOverlays(this)) {
            Log.i(TAG, "Window overlay permission not granted. Asking for permission...");
            confirmSettingsDialog(
                    "You have not granted this app permission to draw over other apps. This feature is optional, but will increase the functionality of the productivity system. If you choose to allow the permission, we can create visual indicators on any other app, to help you stay focused on your task. The application will never use this permission for reasons other than helping you stay focused, and it can be deactivated at any time in settings. Do you wish to allow this permission?",
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                    true);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // this method fires once as well as constructor
        // but also application has context here

        Log.i("main", "onCreate fired");
    }
}