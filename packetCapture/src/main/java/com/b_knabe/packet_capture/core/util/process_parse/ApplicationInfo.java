package com.b_knabe.packet_capture.core.util.process_parse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.b_knabe.packet_capture.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ApplicationInfo implements Serializable {
    private static final String TAG = "AppInfo";
    private static final LruCache<String, IconInfo> iconCache = new LruCache<>(50);
    private static Drawable defaultIcon = null;
    public final String allAppName;
    public final String leaderAppName;
    public final AppPackageNames appPackageNames;

    private ApplicationInfo(String leaderAppName, String allAppName, String[] packageNames) {
        this.leaderAppName = leaderAppName;
        this.allAppName = allAppName;
        this.appPackageNames = AppPackageNames.newInstance(packageNames);
    }

    public static ApplicationInfo createFromUid(Context ctx, int uid) {
        PackageManager pm = ctx.getPackageManager();
        ArrayList<Entry> list = new ArrayList<>();
        if (uid > 0) {
            try {
                String[] pkgNames = pm.getPackagesForUid(uid);
                if (pkgNames == null || pkgNames.length <= 0) {
                    list.add(new Entry("System", "nonpkg.noname"));
                } else {
                    for (String pkgName : pkgNames) {
                        if (pkgName != null) {
                            try {
                                PackageInfo appPackageInfo = pm.getPackageInfo(pkgName, 0);
                                String appName = null;
                                if (appPackageInfo != null) {
                                    appName = appPackageInfo.applicationInfo.loadLabel(pm).toString();
                                }
                                if (appName == null || appName.equals("")) {
                                    // If the app name is empty, replace it with the package name
                                    appName = pkgName;
                                }
                                list.add(new Entry(appName, pkgName));
                            } catch (PackageManager.NameNotFoundException e) {
                                // When the app corresponding to the package name is not found on the mobile phone system
                                list.add(new Entry(pkgName, pkgName));
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                Log.i(TAG, "error getPackagesForUid(). package manager has died");
                return null;
            }
        }
        if (list.size() == 0) {
            // list.size() is 0 only when uid <= 0, and the default is system
            list.add(new Entry("System", "root.uid=0"));
        }
        Collections.sort(list, new Comparator<Entry>() {
            public int compare(Entry lhs, Entry rhs) {
                int ret = lhs.appName.compareToIgnoreCase(rhs.appName);
                if (ret == 0) {
                    return lhs.pkgName.compareToIgnoreCase(rhs.pkgName);
                }
                return ret;
            }
        });
        String[] pkgs = new String[list.size()];
        String[] apps = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            pkgs[i] = list.get(i).pkgName;
            apps[i] = list.get(i).appName;
        }
        return new ApplicationInfo(apps[0], TextUtils.join(",", apps), pkgs);
    }

    public static Drawable getIcon(Context ctx, String pkgName) {
        return getIcon(ctx, pkgName, false);
    }

    public static synchronized Drawable getIcon(Context ctx, String pkgName, boolean onlyPeek) {
        Drawable drawable = null;
        synchronized (ApplicationInfo.class) {
            IconInfo iconInfo;
            if (defaultIcon == null) {
                defaultIcon = ctx.getResources().getDrawable(R.drawable.ic_android);
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo appPackageInfo = null;
            try {
                appPackageInfo = pm.getPackageInfo(pkgName, 0);
                long lastUpdate = appPackageInfo.lastUpdateTime;
                iconInfo = (IconInfo) iconCache.get(pkgName);
                if (iconInfo != null && iconInfo.date == lastUpdate && iconInfo.icon != null) {
                    drawable = iconInfo.icon;
                }
            } catch (PackageManager.NameNotFoundException ignore) {
            }
            if (appPackageInfo != null) {
                if (!onlyPeek) {
                    drawable = appPackageInfo.applicationInfo.loadIcon(pm);
                    iconInfo = new IconInfo();
                    iconInfo.date = appPackageInfo.lastUpdateTime;
                    iconInfo.icon = drawable;
                    iconCache.put(pkgName, iconInfo);
                }
            } else {
                iconCache.remove(pkgName);
                drawable = defaultIcon;
            }
        }
        return drawable;
    }

    static class Entry {
        final String appName;
        final String pkgName;

        public Entry(String appName, String pkgName) {
            this.appName = appName;
            this.pkgName = pkgName;
        }
    }

    static class IconInfo {
        long date;
        Drawable icon;

        IconInfo() {
        }
    }
}
