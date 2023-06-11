package com.dkanada.gramophone.database;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        List<String> items = Arrays.asList(value.split("\\s*, \\s*"));
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(items);
        return arrayList;
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<String> list) {
        return list.toString().substring(1,list.toString().length()-1);
    }
}
