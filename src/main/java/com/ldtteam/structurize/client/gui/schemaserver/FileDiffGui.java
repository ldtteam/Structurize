package com.ldtteam.structurize.client.gui.schemaserver;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.blockout.views.ScrollingList.DataProvider;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileTreeSnapshotDiff;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileTreeSnapshotDiff.Diff.DiffType;
import com.ldtteam.structurize.util.LanguageHandler;

public class FileDiffGui extends Window
{
    public FileDiffGui(final String rootPath, final FileTreeSnapshotDiff fileDiff)
    {
        super(Constants.MOD_ID + ":gui/schemaserver/filediff.xml");

        findPaneOfTypeByID("title", Label.class).setLabelText(LanguageHandler.format("structurize.gui.ssfilediff.title", rootPath));
        for (final DiffType dt : DiffType.values())
        {
            findPaneOfTypeByID(dt.name().toLowerCase(), Label.class)
                .setLabelText(LanguageHandler.translateKey(dt.getTranslationKey()) + ": " + fileDiff.getCountOf(dt));
        }
        findPaneOfTypeByID("file_diff_list", ScrollingList.class).setDataProvider(new DataProvider()
        {
            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("difftype", Label.class)
                    .setLabelText(LanguageHandler.translateKey(fileDiff.get(index).getStatus().getTranslationKey()));
                rowPane.findPaneOfTypeByID("path", Label.class).setLabelText(fileDiff.get(index).getPath());
                /*
                 * unfortunately we can't revert local file changes, however we can remove them from diff view but that may confuse users
                 * rowPane.findPaneOfTypeByID("remove", ButtonImage.class).setHandler(button -> {
                 * fileDiff.remove(index);
                 * });
                 */
            }

            @Override
            public int getElementCount()
            {
                return fileDiff.size();
            }
        });
    }
}
