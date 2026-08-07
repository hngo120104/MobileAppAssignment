package com.example.quanlynhansu.data.local.dao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.quanlynhansu.data.local.ClassroomDatabase;

import java.util.List;

final class SqliteBatchDelete {
    private SqliteBatchDelete() {
    }

    static boolean deleteByIds(
            ClassroomDatabase database,
            String table,
            List<Long> ids
    ) {
        if (ids.isEmpty()) {
            return false;
        }

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            for (long id : ids) {
                int deleted = writableDatabase.delete(
                        table,
                        "id=?",
                        new String[]{String.valueOf(id)}
                );
                if (deleted != 1) {
                    throw new SQLiteException("Entity no longer exists");
                }
            }
            writableDatabase.setTransactionSuccessful();
            return true;
        } catch (SQLiteException exception) {
            return false;
        } finally {
            writableDatabase.endTransaction();
        }
    }
}
