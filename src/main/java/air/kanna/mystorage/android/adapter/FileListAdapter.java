package air.kanna.mystorage.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import air.kanna.mystorage.model.FileItem;
import air.kanna.mystorage.model.FileType;

import kanna.air.mystorage.android.R;

/**
 * Created by lenovo on 2019-11-21.
 */

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_ITEM = 0;// 第一种ViewType，正常的item
    private static final int VIEW_FOOT = 1;// 第二种ViewType，底部的提示View

    private Context context;
    private List<FileItem> datas;// 数据源

    private boolean hasMore = true;   // 变量，是否有更多数据

    public FileListAdapter(List<FileItem> datas, Context context) {
        this.datas = datas;
        this.context = context;
    }

    public void resetDatas(List<FileItem> newDatas, boolean hasMore) {
        datas.clear();
        updateAndAddList(newDatas, hasMore);
    }

    public void updateAndAddList(List<FileItem> newDatas, boolean hasMore) {
        if(newDatas != null && newDatas.size() > 0){
            datas.addAll(newDatas);
        }
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    //根据条目位置返回ViewType，以供onCreateViewHolder方法内获取不同的Holder
    @Override
    public int getItemViewType(int position) {
        if(position == datas.size()){
            return VIEW_FOOT;
        }else{
            return VIEW_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new NormalHolder(LayoutInflater.from(context).inflate(R.layout.file_list_item, null));
        }else{
            return new FootHolder(LayoutInflater.from(context).inflate(R.layout.file_list_foot, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View root = ((NormalHolder)holder).getRootView();
        if(holder instanceof FootHolder){
            setFootView(root);
        }else{
            setFileItemToView(root, datas.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return datas.size() + 1;
    }

    private void setFileItemToView(View root, FileItem item){
        ImageView icon = root.findViewById(R.id.file_item_type_icon);
        TextView fileName = root.findViewById(R.id.file_item_name);
        TextView filePath = root.findViewById(R.id.file_item_path);
        TextView diskPath = root.findViewById(R.id.file_item_disk);
        TextView fileUpdTime = root.findViewById(R.id.file_item_upd_time);

        if(item.getFileTypeObj() == FileType.TYPE_DICECTORY){
            icon.setImageResource(R.drawable.ic_folder_black_24dp);
        }else{
            icon.setImageResource(R.drawable.ic_file_black_24dp);
        }
        fileName.setText(item.getFileName());
        filePath.setText(item.getFilePath());
        if(item.getDiskDescription() == null){
            diskPath.setText("");
        }else {
            diskPath.setText(item.getDiskDescription().getBasePath());
        }
        fileUpdTime.setText(item.getLastModDateStr());
    }

    private void setFootView(View root){
        TextView text = root.findViewById(R.id.file_list_foot_text);
        if(datas.size() <= 0){
            text.setText(R.string.list_foot_nodata);
            return;
        }
        if (hasMore == true){
            text.setText(R.string.list_foot_loading);
        }else{
            text.setText(R.string.list_foot_nomore);
        }
    }

    private class NormalHolder extends RecyclerView.ViewHolder {
        private View root;

        NormalHolder(View itemView) {
            super(itemView);
            root = itemView;
        }
        public View getRootView(){
            return root;
        }
    }

    private class FootHolder extends NormalHolder {
        FootHolder(View itemView) {
            super(itemView);
        }
    }
}
