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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import geralt_encore.delightfulsqlbrite.R;
import geralt_encore.delightfulsqlbrite.todo.TodoApp;
import geralt_encore.delightfulsqlbrite.todo.data.TodoListModel;
import geralt_encore.delightfulsqlbrite.todo.db.TodoList;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static butterknife.ButterKnife.findById;

public final class NewListFragment extends DialogFragment {
    public static NewListFragment newInstance() {
        return new NewListFragment();
    }

    private final PublishSubject<String> createClicked = PublishSubject.create();
    private TodoList.Insert_todo_list insertTodoList;

    @Inject BriteDatabase db;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        TodoApp.getComponent(activity).inject(this);
        insertTodoList = new TodoListModel.Insert_todo_list(db.getWritableDatabase());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        View view = LayoutInflater.from(context).inflate(R.layout.new_list, null);

        EditText name = findById(view, android.R.id.input);
        Observable.combineLatest(createClicked, RxTextView.textChanges(name),
                (ignored, text) -> text.toString())
                .observeOn(Schedulers.io())
                .subscribe(name1 -> {
                    insertTodoList.bind(name1);
                    db.executeInsert(insertTodoList.table, insertTodoList.program);
                });

        return new AlertDialog.Builder(context) //
                .setTitle(R.string.new_list)
                .setView(view)
                .setPositiveButton(R.string.create, (dialog, which) -> createClicked.onNext("clicked"))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create();
    }
}
