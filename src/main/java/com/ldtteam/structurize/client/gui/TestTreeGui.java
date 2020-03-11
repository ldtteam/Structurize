package com.ldtteam.structurize.client.gui;

import java.util.ArrayList;
import java.util.List;
import com.ldtteam.blockout.views.TreeView;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.blockout.views.TreeView.TreeViewNode;
import com.ldtteam.structurize.api.util.constant.Constants;

public class TestTreeGui extends Window
{
    private final TreeView treeView;

    public TestTreeGui()
    {
        super(Constants.MOD_ID + ":gui/testtreewindow.xml");
        treeView = findPaneOfTypeByID("tree", TreeView.class);
        final List<TreeViewNode> treeRoots = new ArrayList<>();

        final TreeViewNode firstTree = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        final TreeViewNode firstTreeSub0 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1);
        firstTreeSub0.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub0.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub0.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub0.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTree.getChilds().add(firstTreeSub0);
        final TreeViewNode firstTreeSub1 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1);
        final TreeViewNode firstTreeSub10 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub10.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub1.getChilds().add(firstTreeSub10);
        firstTreeSub1.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTree.getChilds().add(firstTreeSub1);
        final TreeViewNode firstTreeSub2 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1);
        firstTreeSub2.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTree.getChilds().add(firstTreeSub2);
        treeRoots.add(firstTree);

        final TreeViewNode secondTree = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1);
        final TreeViewNode secondTreeSub0 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        secondTreeSub0.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        secondTree.getChilds().add(secondTreeSub0);
        final TreeViewNode secondTreeSub1 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        secondTree.getChilds().add(secondTreeSub1);
        final TreeViewNode secondTreeSub2 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        secondTreeSub2.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        secondTreeSub2.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        final TreeViewNode firstTreeSub20 = new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0);
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 1));
        firstTreeSub20.getChilds().add(new TreeViewNode(TreeViewNode.NOOP_PANE_UPDATER, false, 0));
        secondTreeSub2.getChilds().add(firstTreeSub20);
        secondTree.getChilds().add(secondTreeSub2);
        treeRoots.add(secondTree);

        treeView.setRoots(treeRoots);
    }
}
