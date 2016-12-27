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

import geralt_encore.delightfulsqlbrite.todo.data.TodoItemModel;
import rx.functions.Func1;

@AutoValue
public abstract class TodoItem implements TodoItemModel, Parcelable {

    public static final Factory<TodoItem> FACTORY = new Factory<>(AutoValue_TodoItem::new);

    public static final RowMapper<TodoItem> MAPPER = FACTORY.select_all_items_for_listMapper();

    public static final RowMapper<Long> COUNT_MAPPER = FACTORY.count_active_items_for_listMapper();

    public static final Func1<Cursor, TodoItem> MAPPER_FUNCTION = MAPPER::map;
}
