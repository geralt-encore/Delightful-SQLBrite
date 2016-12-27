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

import com.google.auto.value.AutoValue;

import android.database.Cursor;
import android.os.Parcelable;

import com.squareup.sqldelight.RowMapper;

import geralt_encore.delightfulsqlbrite.todo.data.TodoListModel;
import rx.functions.Func1;

// Note: normally I wouldn't prefix table classes but I didn't want 'List' to be overloaded.
@AutoValue
public abstract class TodoList implements TodoListModel, Parcelable {

    public static Factory<TodoList> FACTORY = new Factory<>(AutoValue_TodoList::new);

    public static RowMapper<String> NAME_MAPPER = FACTORY.select_name_by_idMapper();

    public static RowMapper<ListsItem> LISTS_ITEM_MAPPER =
            FACTORY.select_lists_with_item_countsMapper(AutoValue_TodoList_ListsItem::new);

    public static Func1<Cursor, ListsItem> LISTS_ITEM_MAPPER_FUNCTION = LISTS_ITEM_MAPPER::map;

    @AutoValue
    public static abstract class ListsItem implements Select_lists_with_item_countsModel {}
}
