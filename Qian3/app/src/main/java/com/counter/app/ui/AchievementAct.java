package com.counter.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.counter.app.R;
import com.counter.app.lib.db.Database;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
public  abstract class AchievementAct  {


    //leaderboard in 2 ways, total-oneday
    private static void updateTotalLeaderboard(final GoogleApiClient gc, final Context c, int totalSteps) {
        Games.Leaderboards
                .submitScore(gc, c.getString(R.string.leaderboard_most_steps_walked), totalSteps);
    }
    private static void updateOneDayLeaderboard(final GoogleApiClient gc, final Context c, int steps) {

        Games.Leaderboards
                .submitScore(gc, c.getString(R.string.leaderboard_most_steps_walk_in_one_day),
                        steps);
    }

    //achievement part
    public static void achievementsAndLeaderboard(final GoogleApiClient gc, final Context context) {
       if (gc.isConnected()) {
            Database db = Database.getInstance(context);
           // db.removeInvalidEntries();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("achievement_monster_walker1", false)) {
                Cursor c = db.query(new String[]{"steps"}, "steps >= 10 AND date > 0", null, null,
                        null, null, "1"); if (c.getCount() >= 1) {
                    unlockAchievement(gc,
                            context.getString(R.string.achievement_monster_walker_1));
                    prefs.edit().putBoolean("achievement_monster_walker1", true).apply();
                }
                c.close();
            }
            if (!prefs.getBoolean("achievement_monster_walker2", false)) {
                Cursor c = db.query(new String[]{"steps"}, "steps >= 10000 AND date > 0", null, null, null, null, "1");
                if (c.getCount() >= 1) {
                    unlockAchievement(gc,
                            context.getString(R.string.achievement_monster_walker_2));
                    prefs.edit().putBoolean("achievement_monster_walker2", true).apply();
                }
                c.close();
            }
            if (!prefs.getBoolean("achievement_monster_walker3", false)) {
                Cursor c = db.query(new String[]{"steps"}, "steps >= 15000 AND date > 0", null, null, null, null, "1");
                if (c.getCount() >= 1) {
                    unlockAchievement(gc,
                            context.getString(R.string.achievement_monster_walker_3));
                    prefs.edit().putBoolean("achievement_monster_walker3", true).apply();
                }
                c.close();
            }
            if (!prefs.getBoolean("achievement_monster_walker4", false)) {
                Cursor c = db.query(new String[]{"steps"}, "steps >= 20000 AND date > 0", null, null, null, null, "1");
                if (c.getCount() >= 1) {
                    unlockAchievement(gc,
                            context.getString(R.string.achievement_monster_walker_4));
                    prefs.edit().putBoolean("achievement_monster_walker4", true).apply();
                }
                c.close();
            }
            if (!prefs.getBoolean("achievement_monster_walker5", false)) {
                Cursor c = db.query(new String[]{"steps"}, "steps >= 25000 AND date > 0", null, null, null, null, "1");
                if (c.getCount() >= 1) {
                    unlockAchievement(gc,
                            context.getString(R.string.achievement_monster_walker_5));
                    prefs.edit().putBoolean("achievement_monster_walker5", true).apply();
                }
                c.close();
            }

            Cursor c = db.query(new String[]{"COUNT(*)"}, "steps >= 10000 AND date > 0", null, null, null, null, null);
            c.moveToFirst();
            int daysForStamina = c.getInt(0);
            c.close();

            if (!prefs.getBoolean("achievement_stamina1", false)) {
                if (daysForStamina >= 5) {
                    unlockAchievement(gc, context.getString(R.string.achievement_stamina_1));
                    prefs.edit().putBoolean("achievement_stamina1", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_stamina2", false)) {
                if (daysForStamina >= 10) {
                    unlockAchievement(gc, context.getString(R.string.achievement_stamina_2));
                    prefs.edit().putBoolean("achievement_stamina2", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_stamina3", false)) {
                if (daysForStamina >= 30) {
                    unlockAchievement(gc, context.getString(R.string.achievement_rambler_3));
                    prefs.edit().putBoolean("achievement_stamina3", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_stamina4", false)) {
                if (daysForStamina >= 60) {
                    unlockAchievement(gc, context.getString(R.string.achievement_stamina_4));
                    prefs.edit().putBoolean("achievement_stamina4", true).apply();
                }
            }
           unlockAchievement(gc,
                   context.getString(R.string.achievement_monster_walker_1));
           unlockAchievement(gc,
                   context.getString(R.string.achievement_monster_walker_2));
           unlockAchievement(gc,
                   context.getString(R.string.achievement_monster_walker_3));

            int totalSteps =150000;
            //db.getTotalWithoutToday();

            if (!prefs.getBoolean("achievement_rambler1", false)) {
                if (totalSteps > 100000) {
                    unlockAchievement(gc, context.getString(R.string.achievement_rambler_1));
                    prefs.edit().putBoolean("achievement_rambler1", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_rambler2", false)) {
                if (totalSteps > 200000) {
                    unlockAchievement(gc, context.getString(R.string.achievement_rambler_2));
                    prefs.edit().putBoolean("achievement_rambler2", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_rambler3", false)) {
                if (totalSteps > 500000) {
                    unlockAchievement(gc, context.getString(R.string.achievement_rambler_3));
                    prefs.edit().putBoolean("achievement_rambler3", true).apply();
                }
            }
            if (!prefs.getBoolean("achievement_rambler4", false)) {
                if (totalSteps > 1000000) {
                    unlockAchievement(gc, context.getString(R.string.achievement_rambler_4));
                    prefs.edit().putBoolean("achievement_rambler4", true).apply();
                }
            }

            updateTotalLeaderboard(gc, context, totalSteps);

          //  updateOneDayLeaderboard(gc, context, db.getRecord());

            db.close();
        }
    }
    private static void unlockAchievement(GoogleApiClient gc, String achievementName) {
        Games.Achievements.unlock(gc, achievementName);
    }
}
