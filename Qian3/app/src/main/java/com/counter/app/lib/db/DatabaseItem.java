package com.counter.app.lib.db;

/**
 * Created by kimpochu on 15/4/16.*/
 import java.util.Calendar;

 public class DatabaseItem {

 // Public parameters
 public int id;
 public int steps;

 // Constructor
 public DatabaseItem() {
 id = 0;
 steps = 0;
 }
 public DatabaseItem(Calendar c, int s) {
 id = Database.cvtCalendarToID(c);
 steps = s;
 }
 public DatabaseItem(int i, int s) {
 id = i;
 steps = s;
 }

 // Public methods
 public int getYear() {
 return id/10000;
 }
 public int getMonth() {
 return (id/100)%100;
 }
 public int getDay() {
 return id%100;
 }
 }
