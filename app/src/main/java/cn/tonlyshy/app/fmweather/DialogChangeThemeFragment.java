package cn.tonlyshy.app.fmweather;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaowm5 on 17/3/24.
 */

public class DialogChangeThemeFragment extends DialogFragment {
    private RecyclerView mRecyclerView;

    private List<Theme> themesList=new ArrayList<>();

    SharedPreferences prefs;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_theme,container,false);
        themesList.add(new Theme("黑",R.color.colorPrimaryDarkM,R.style.Dark));
        themesList.add(new Theme("灰",R.color.colorPrimarylightM,R.style.AppTheme));
        themesList.add(new Theme("绿",R.color.colorPrimary,R.style.Green));
        themesList.add(new Theme("白",R.color.backgound_light,R.style.Light));
        themesList.add(new Theme("纯黑",android.R.color.background_dark,R.style.TrueDark));
        themesList.add(new Theme("网易红",R.color.colorPrimaryRed,R.style.Red));
        themesList.add(new Theme("基佬紫",android.R.color.holo_purple,R.style.Purple));
        ThemeAdapter themeAdapter=new ThemeAdapter(themesList);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.theme_recycler_view);
        // you can use LayoutInflater.from(getContext()).inflate(...) if you have xml layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        mRecyclerView.setAdapter(themeAdapter);
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());

        return view;
    }
    /*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        themesList.add(new Theme("绿",R.color.colorPrimary));
        themesList.add(new Theme("红",R.color.colorPrimaryRed));
        themesList.add(new Theme("白",R.color.primary_material_darkM));
        themesList.add(new Theme("黑",R.color.primary_material_lightM));
        ThemeAdapter themeAdapter=new ThemeAdapter(themesList);
        View view=inflater.inflate(R.layout.choose_area,container,false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.theme_recycler_view);
        // you can use LayoutInflater.from(getContext()).inflate(...) if you have xml layout
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        mRecyclerView.setAdapter(themeAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle("ThemeDialog")
                .setView(mRecyclerView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do something
                            }
                        }
                ).create();
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v=getView();
    }


    class Theme{
        String themeName;
        int colorResId;
        int themeId;

        public int getThemeId() {
            return themeId;
        }

        public void setThemeId(int themeId) {
            this.themeId = themeId;
        }

        public Theme(String themeName, int colorResId,int themeId){
            this.themeName=themeName;
            this.colorResId=colorResId;
            this.themeId=themeId;
        }

        public String getThemeName() {
            return themeName;
        }

        public void setThemeName(String themeName) {
            this.themeName = themeName;
        }

        public int getColorResId() {
            return colorResId;
        }

        public void setColorResId(int colorResId) {
            this.colorResId = colorResId;
        }
    }
     class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

        private Context mContext;

        private List<Theme> mthemeList;

         class ViewHolder extends RecyclerView.ViewHolder{
            CardView cardView;
            ImageView themeImage;
            TextView themeText;

            public ViewHolder(View view){
                super(view);
                cardView=(CardView)view;
                themeImage=(ImageView)view.findViewById(R.id.theme_image);
                themeText=(TextView)view.findViewById(R.id.theme_name);
            }
        }

        public ThemeAdapter(List<Theme> themeList){
            this.mthemeList=themeList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mContext==null){
                mContext=parent.getContext();
            }
            View view= LayoutInflater.from(mContext).inflate(R.layout.theme_item,parent,false);
            final ViewHolder holder=new ViewHolder(view);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=holder.getAdapterPosition();
                    Theme theme=mthemeList.get(position);
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    editor.putInt("theme",theme.getThemeId());
                    editor.apply();
                    //recreate();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    getActivity().finish();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Theme theme=mthemeList.get(position);
            holder.themeText.setText(theme.getThemeName());
            holder.themeImage.setBackgroundResource(theme.getColorResId());
        }

        @Override
        public int getItemCount() {
            return mthemeList.size();
        }
    }

}
