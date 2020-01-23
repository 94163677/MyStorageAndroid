package air.kanna.mystorage.android.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import air.kanna.mystorage.android.ServiceFactory;
import air.kanna.mystorage.model.DiskDescription;
import air.kanna.mystorage.model.FileType;
import kanna.air.mystorage.android.R;

public class SearchAlertDialog {
    private Activity context;
    private AlertDialog dialog;

    private View searchDialogRoot = null;
    private EditText fileName = null;
    private Spinner fileType = null;
    private Spinner diskSelected = null;

    public SearchAlertDialog(Activity context, List<DiskDescription> diskList){
        if(context == null){
            throw new NullPointerException("Activity Context is null");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        searchDialogRoot = context.getLayoutInflater().inflate(R.layout.dialog_search_param, null);
        fileName = searchDialogRoot.findViewById(R.id.dialog_search_file_name_edit);
        fileType = searchDialogRoot.findViewById(R.id.dialog_search_type_spinner);
        diskSelected = searchDialogRoot.findViewById(R.id.dialog_search_disk_spinner);

        List<String> fileTypeData = Arrays.asList(
                context.getString(R.string.dialog_search_all),
                context.getString(R.string.dialog_search_type_file),
                context.getString(R.string.dialog_search_type_path)
        );

        List<String> diskData = new ArrayList<>();
        diskData.add(context.getString(R.string.dialog_search_all));
        if(diskList != null) {
            for (DiskDescription disk : diskList) {
                diskData.add(disk.getBasePath());
            }
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_dropdown_item, fileTypeData);
        ArrayAdapter<String> diskAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_dropdown_item, diskData);

        fileType.setAdapter(typeAdapter);
        diskSelected.setAdapter(diskAdapter);

        builder.setView(searchDialogRoot)
                .setCancelable(false)
                .setTitle(R.string.dialog_search_title)
                .setNeutralButton(R.string.clear_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.setSearchFileName("");
                        fileName.setText("");

                        config.setSearchFileType("");
                        fileType.setSelection(0);

                        config.setSearchDiskPath("");
                        diskSelected.setSelection(0);
                        ServiceFactory.getConfigService().saveConfig(config);
                        doSearch();
                    }
                })
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        config.setSearchFileName(fileName.getText().toString());
                        if(fileType.getSelectedItemPosition() == 1) {
                            config.setSearchFileType("" + FileType.TYPE_FILE.getType());
                        }else if(fileType.getSelectedItemPosition() == 2){
                            config.setSearchFileType("" + FileType.TYPE_DICECTORY.getType());
                        }else{
                            config.setSearchFileType("");
                        }
                        int diskIndex = diskSelected.getSelectedItemPosition() - 1;
                        if(diskIndex >= 0 && diskIndex < diskList.size()){
                            config.setSearchDiskPath(diskList.get(diskIndex).getBasePath());
                        }
                        ServiceFactory.getConfigService().saveConfig(config);
                        doSearch();
                    }
                })
                .setNegativeButton(R.string.cancel_button, null);

        dialog = builder.create();
    }

}
