/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geralt_encore.delightfulsqlbrite.todo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import geralt_encore.delightfulsqlbrite.todo.data.TodoItemModel;
import geralt_encore.delightfulsqlbrite.todo.data.TodoListModel;

final class DbOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    public DbOpenHelper(Context context) {
        super(context, "todo.db", null /* factory */, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TodoList.CREATE_TABLE);
        db.execSQL(TodoItem.CREATE_TABLE);

        final TodoList.Insert_todo_list insertTodoList = new TodoListModel.Insert_todo_list(db);
        final TodoList.Insert_archived_todo_list insertArchivedTodoList = new TodoListModel
                .Insert_archived_todo_list(db);
        final TodoItem.Insert_todo_item insertTodoItem = new TodoItemModel.Insert_todo_item(db);
        final TodoItem.Insert_completed_todo_item insertCompletedTodoItem = new TodoItemModel
                .Insert_completed_todo_item(db);

        insertTodoList.bind("Grocery List");
        long groceryListId = insertTodoList.program.executeInsert();
        insertTodoItem.bind(groceryListId, "Beer");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(groceryListId, "Point Break on DVD");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(groceryListId, "Bad Boys 2 on DVD");
        insertTodoItem.program.executeInsert();

        insertTodoList.bind("Holiday Presents");
        long holidayPresentsListId = insertTodoList.program.executeInsert();
        insertTodoItem.bind(holidayPresentsListId, "Pogo Stick for Jake W.");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(holidayPresentsListId, "Jack-in-the-box for Alec S.");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(holidayPresentsListId, "Pogs for Matt P.");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(holidayPresentsListId, "Coal for Jesse W.");
        insertTodoItem.program.executeInsert();

        insertTodoList.bind("Work Items");
        long workListId = insertTodoList.program.executeInsert();
        insertCompletedTodoItem.bind(workListId, "Finish SqlBrite library", true);
        insertCompletedTodoItem.program.executeInsert();
        insertTodoItem.bind(workListId, "Finish SqlBrite sample app");
        insertTodoItem.program.executeInsert();
        insertTodoItem.bind(workListId, "Publish SqlBrite to GitHub");
        insertTodoItem.program.executeInsert();

        insertArchivedTodoList.bind("Birthday Presents", true);
        long birthdayPresentsListId = insertArchivedTodoList.program.executeInsert();
        insertCompletedTodoItem.bind(birthdayPresentsListId, "New car", true);
        insertCompletedTodoItem.program.executeInsert();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
