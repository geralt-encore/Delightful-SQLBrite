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
package geralt_encore.delightfulsqlbrite.todo.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.sqldelight.hockey.data.TodoItemModel;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.squareup.sqldelight.SqlDelightStatement;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import geralt_encore.delightfulsqlbrite.R;
import geralt_encore.delightfulsqlbrite.todo.TodoApp;
import geralt_encore.delightfulsqlbrite.todo.db.Db;
import geralt_encore.delightfulsqlbrite.todo.db.TodoItem;
import geralt_encore.delightfulsqlbrite.todo.db.TodoList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;
import static com.squareup.sqlbrite.SqlBrite.Query;

public final class ItemsFragment extends Fragment {
    private static final String KEY_LIST_ID = "list_id";

    public interface Listener {
        void onNewItemClicked(long listId);
    }

    private TodoItem.Update_complete updateComplete;

    public static ItemsFragment newInstance(long listId) {
        Bundle arguments = new Bundle();
        arguments.putLong(KEY_LIST_ID, listId);

        ItemsFragment fragment = new ItemsFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Inject BriteDatabase db;

    @BindView(android.R.id.list) ListView listView;
    @BindView(android.R.id.empty) View emptyView;

    private Listener listener;
    private ItemsAdapter adapter;
    private CompositeSubscription subscriptions;

    private long getListId() {
        return getArguments().getLong(KEY_LIST_ID);
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof Listener)) {
            throw new IllegalStateException("Activity must implement fragment Listener.");
        }

        super.onAttach(activity);
        TodoApp.getComponent(activity).inject(this);
        setHasOptionsMenu(true);

        listener = (Listener) activity;
        adapter = new ItemsAdapter(activity);

        updateComplete = new TodoItemModel.Update_complete(db.getWritableDatabase());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.add(R.string.new_item)
                .setOnMenuItemClickListener(clickedItem -> {
                    listener.onNewItemClicked(getListId());
                    return true;
                });
        MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.items, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);

        RxAdapterView.itemClickEvents(listView)
                .observeOn(Schedulers.io())
                .subscribe(event -> {
                    boolean newValue = !adapter.getItem(event.position()).complete();
                    updateComplete.bind(newValue, event.id());
                    db.executeUpdateDelete(updateComplete.table, updateComplete.program);
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        long listId = getListId();

        subscriptions = new CompositeSubscription();

        final SqlDelightStatement itemCountStatement = TodoItem.FACTORY
                .count_active_items_for_list(listId);
        Observable<Long> itemCount = db.createQuery(itemCountStatement.tables,
                itemCountStatement.statement)
                .map(query -> {
                    Cursor cursor = query.run();
                    try {
                        if (!cursor.moveToNext()) {
                            throw new AssertionError("No rows");
                        }
                        return TodoItem.COUNT_MAPPER.map(cursor);
                    } finally {
                        cursor.close();
                    }
                });
        final SqlDelightStatement listNameStatement = TodoList.FACTORY.select_name_by_id(listId);
        Observable<String> listName =
                db.createQuery(listNameStatement.tables, listNameStatement.statement)
                        .map(query -> {
                            Cursor cursor = query.run();
                            try {
                                if (!cursor.moveToNext()) {
                                    throw new AssertionError("No rows");
                                }
                                return TodoList.NAME_MAPPER.map(cursor);
                            } finally {
                                cursor.close();
                            }
                        });
        subscriptions.add(
                Observable.combineLatest(listName, itemCount,
                        (listName1, itemCount1) -> listName1 + " (" + itemCount1 + ")")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(title -> {
                            getActivity().setTitle(title);
                        }));
        final SqlDelightStatement itemsForListStatement = TodoItem.FACTORY
                .select_all_items_for_list(listId);
        subscriptions.add(db.createQuery(itemsForListStatement.tables,
                itemsForListStatement.statement)
                .mapToList(TodoItem.MAPPER_FUNCTION)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter));
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }
}
