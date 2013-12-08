package net.cs50.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.cs50.recipes.util.HttpHelper;
import net.cs50.recipes.util.ImageHelper;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public final class CreateActivity extends BaseActivity {

    private static final String TAG = "create_activity";

    private static final int POPUP_HEIGHT = 122;

    private static final int GROUP_INGREDIENTS = 0;
    private static final int GROUP_INSTRUCTIONS = 1;

    private final OnClickListener onAdd = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int group = (Integer) v.getTag(R.id.TAG_GROUP);

            PopupEditor popup = new PopupEditor(group);
            popup.show(v);
        }
    };
    private final OnClickListener onEdit = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int group = (Integer) v.getTag(R.id.TAG_GROUP);
            int index = (Integer) v.getTag(R.id.TAG_INDEX);

            PopupEditor popup = new PopupEditor(group, index);
            popup.show(v);
        }
    };
    private final OnClickListener onDelete = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int group = (Integer) v.getTag(R.id.TAG_GROUP);
            int index = (Integer) v.getTag(R.id.TAG_INDEX);

            switch (group) {
            case GROUP_INGREDIENTS:
                ingredients.remove(index);
                break;
            case GROUP_INSTRUCTIONS:
                instructions.remove(index);
                break;
            default:
                throw new IllegalArgumentException();
            }

            listAdapter.notifyDataSetChanged();
        }
    };
    private final OnFocusChangeListener onUnfocusHideKeyboard = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        }
    };

    LayoutInflater inflater;

    ImageView imageView;
    EditText titleText;
    ExpandableListView detailsListView;
    ExpandableListAdapter listAdapter;

    Bitmap image;

    private final List<String> ingredients = new ArrayList<String>();
    private final List<String> instructions = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(R.layout.activity_create);

        imageView = (ImageView) findViewById(R.id.image_create_recipe);

        titleText = (EditText) findViewById(R.id.text_create_title);
        titleText.setOnFocusChangeListener(onUnfocusHideKeyboard);

        detailsListView = (ExpandableListView) findViewById(R.id.list_create_details);
        listAdapter = new ExpandableListAdapter();
        detailsListView.setAdapter(listAdapter);
        
        detailsListView.expandGroup(0);
        detailsListView.expandGroup(1);

        Button createButton = (Button) findViewById(R.id.btn_create);
        createButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpload();
                finish();
            }
        });

        showImage();
    }

    private void startUpload() {
        final String name = titleText.getText().toString();
        new HttpHelper.AddRecipeAsyncTask().execute(getContentResolver(), image, name, ingredients,
                instructions);
    }

    private void showImage() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                if (uri.getScheme().equalsIgnoreCase("content")) {
                    String url = ImageHelper.getMediaPath(this, uri);
                    if (url != null) {
                        uri = Uri.parse("file://" + url);
                    }
                }
                image = ImageHelper.imageFromUri(this, uri, ImageHelper.IMAGE_LENGTH);
                imageView.setImageBitmap(image);
            } catch (IOException e) {
            }
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return ingredients.get(childPosition);
            case GROUP_INSTRUCTIONS:
                return instructions.get(childPosition);
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            final String itemText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.create_list_item, null);
            }

            TextView listItem = (TextView) convertView.findViewById(R.id.text_list_item);
            listItem.setText(itemText);

            ImageButton buttonEdit = (ImageButton) convertView.findViewById(R.id.btn_create_edit);
            buttonEdit.setTag(R.id.TAG_GROUP, groupPosition);
            buttonEdit.setTag(R.id.TAG_INDEX, childPosition);
            buttonEdit.setOnClickListener(onEdit);

            ImageButton buttonDelete = (ImageButton) convertView
                    .findViewById(R.id.btn_create_remove);
            buttonDelete.setTag(R.id.TAG_GROUP, groupPosition);
            buttonDelete.setTag(R.id.TAG_INDEX, childPosition);
            buttonDelete.setOnClickListener(onDelete);

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return ingredients.size();
            case GROUP_INSTRUCTIONS:
                return instructions.size();
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return R.string.create_group_ingredients;
            case GROUP_INSTRUCTIONS:
                return R.string.create_group_instructions;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            int groupTitleId = (Integer) getGroup(groupPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.create_list_group, null);
            }

            TextView textListGroup = (TextView) convertView.findViewById(R.id.text_list_group);
            textListGroup.setText(groupTitleId);

            ImageButton buttonAdd = (ImageButton) convertView.findViewById(R.id.btn_create_add);
            buttonAdd.setTag(R.id.TAG_GROUP, groupPosition);
            buttonAdd.setOnClickListener(onAdd);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private class PopupEditor extends PopupWindow implements OnClickListener {

        private View view;
        private EditText text;

        private boolean editing;
        private int group;
        private int index;

        private PopupEditor(String buttonAction) {
            view = inflater.inflate(R.layout.create_popup, null);

            setContentView(view);
            setWidth(LayoutParams.WRAP_CONTENT);
            setHeight(LayoutParams.WRAP_CONTENT);
            setFocusable(true);
            setTouchable(true);
            setOutsideTouchable(true);
            setHeight(LayoutParams.WRAP_CONTENT);
            setBackgroundDrawable(new BitmapDrawable((Resources) null, (Bitmap) null));

            text = (EditText) view.findViewById(R.id.text_popup);
            text.setOnFocusChangeListener(onUnfocusHideKeyboard);

            Button doneButton = (Button) view.findViewById(R.id.btn_popup_done);
            doneButton.setText(buttonAction);
            doneButton.setOnClickListener(this);

            Button cancelButton = (Button) view.findViewById(R.id.btn_popup_cancel);
            cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        public PopupEditor(int group) {
            this("Add");
            this.group = group;

            editing = false;
        }

        public PopupEditor(int group, int index) {
            this("Edit");
            this.group = group;
            this.index = index;

            String s;
            switch (group) {
            case GROUP_INGREDIENTS:
                s = ingredients.get(index);
                break;
            case GROUP_INSTRUCTIONS:
                s = instructions.get(index);
                break;
            default:
                throw new IllegalArgumentException();
            }

            text.setText(s);
            text.selectAll();

            editing = true;
        }

        @Override
        public void onClick(View v) {
            String s = text.getText().toString();
            switch (group) {
            case GROUP_INGREDIENTS:
                if (editing) {
                    ingredients.set(index, s);
                } else {
                    ingredients.add(s);
                }
                break;
            case GROUP_INSTRUCTIONS:
                if (editing) {
                    instructions.set(index, s);
                } else {
                    instructions.add(s);
                }
                break;
            default:
                throw new IllegalArgumentException();
            }

            listAdapter.notifyDataSetChanged();
            dismiss();
        }

        public void show(View v) {
            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POPUP_HEIGHT,
                    r.getDisplayMetrics());
            showAsDropDown(v, 0, -(px + v.getHeight()));
        }
    }

}
