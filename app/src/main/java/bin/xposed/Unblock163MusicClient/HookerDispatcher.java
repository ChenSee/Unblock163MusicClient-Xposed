package bin.xposed.Unblock163MusicClient;

import android.app.ActivityManager;
import android.content.Context;

import net.androidwing.hotxposed.IHookerDispatcher;

import java.util.ArrayList;
import java.util.List;

import bin.xposed.Unblock163MusicClient.hooker.Dislike;
import bin.xposed.Unblock163MusicClient.hooker.Download;
import bin.xposed.Unblock163MusicClient.hooker.Eapi;
import bin.xposed.Unblock163MusicClient.hooker.Gray;
import bin.xposed.Unblock163MusicClient.hooker.HttpMod;
import bin.xposed.Unblock163MusicClient.hooker.Oversea;
import bin.xposed.Unblock163MusicClient.hooker.QualityBox;
import bin.xposed.Unblock163MusicClient.hooker.TipsFor3rd;
import bin.xposed.Unblock163MusicClient.hooker.Transparent;
import bin.xposed.Unblock163MusicClient.ui.SettingsActivity;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class HookerDispatcher implements IHookerDispatcher {

    private String processName;

    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam lpparam) {


        if (Handler.isDomainExpired()) {
            return;
        }

        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            findAndHookMethod(findClass(SettingsActivity.class.getName(), lpparam.classLoader),
                    "getActivatedModuleVersion", new Object[]{XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE)});
        }

        if (lpparam.packageName.equals(CloudMusicPackage.PACKAGE_NAME)) {
            findAndHookMethod(findClass("com.netease.cloudmusic.NeteaseMusicApplication", lpparam.classLoader), "attachBaseContext", new Object[]{Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    Context context = (Context) param.thisObject;

                    if (!isInMainProcess(context) && !isInPlayProcess(context)) {
                        return;
                    }

                    CloudMusicPackage.init(context);
                    hookMainProcess();
                    hookPlayProcess();


                }
            }});
        }
    }


    private void hookMainProcess() {
        List<Hooker> list = new ArrayList<>();
        if (Settings.isUnblockEnabled()) {
            list.add(new Eapi());
            list.add(new Download());
            list.add(new HttpMod());
            list.add(new QualityBox());
            list.add(new TipsFor3rd());

            if (Settings.isOverseaModeEnabled()) {
                list.add(new Oversea());
            }


            if (Settings.isPreventGrayEnabled()) {
                list.add(new Gray());
            }

        }


        if (Settings.isConfirmDislikeEnabled()) {
            list.add(new Dislike());
        }

        if (Settings.isTransparentNavBar()) {
            list.add(new Transparent());
        }

        for (Hooker hooker : list) {
            hooker.startToHook();
        }
    }

    private void hookPlayProcess() {
        List<Hooker> list = new ArrayList<>();
        if (Settings.isUnblockEnabled()) {
            list.add(new Eapi());
            list.add(new Download());
            list.add(new HttpMod());


            if (Settings.isOverseaModeEnabled()) {
                list.add(new Oversea());
            }
        }

        for (Hooker hooker : list) {
            hooker.startToHook();
        }
    }

    private String getCurrentProcessName(Context context) {
        if (processName == null) {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                    if (appProcess.pid == pid) {
                        processName = appProcess.processName;
                    }
                }
            }
        }

        return processName;
    }

    private boolean isInMainProcess(Context content) {
        if (processName == null) {
            processName = getCurrentProcessName(content);
        }

        return processName.equals(CloudMusicPackage.PACKAGE_NAME);
    }


    private boolean isInPlayProcess(Context content) {
        if (processName == null) {
            processName = getCurrentProcessName(content);
        }

        return processName.equals(CloudMusicPackage.PACKAGE_NAME + ":play");
    }
}