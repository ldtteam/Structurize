package com.ldtteam.blockout.views;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TreeView extends ZoomDragView
{
    private List<TreeViewNode> treeRoots;
    private List<PaneParams> templates;
    private int nodeVerticalDistance = 10;
    private int nodeToLineDistance = 10;
    private int lineSize = 2;
    private final int[] color = new int[4];
    private final int nodeHorizontalDistance;
    private Pair<BufferBuilder.DrawState, ByteBuffer> lineBuffer;

    /**
     * Required default constructor.
     */
    public TreeView()
    {
        super();
        nodeHorizontalDistance = 0;
    }

    public TreeView(final PaneParams params)
    {
        super(params);
        nodeVerticalDistance = params.getIntAttribute("nodeVertDist", nodeVerticalDistance);
        nodeToLineDistance = params.getIntAttribute("nodeToLineHorizDist", nodeToLineDistance);
        lineSize = params.getIntAttribute("lineSize", lineSize);
        final int linePackedColor = params.getIntAttribute("linePackedColor", 0xffffffff);
        nodeHorizontalDistance = 2 * nodeToLineDistance + lineSize;
        color[0] = linePackedColor >> 24;
        color[1] = linePackedColor >> 16 & 0xff;
        color[2] = linePackedColor >> 8 & 0xff;
        color[3] = linePackedColor & 0xff;
    }

    public void setRoots(final List<TreeViewNode> treeRootsIn)
    {
        treeRoots = treeRootsIn;
        reconstruct();
    }

    public void reconstruct()
    {
        final List<List<TreeViewNode>> layers = new ArrayList<>();

        // generate panes
        for (final TreeViewNode root : treeRoots)
        {
            construct(layers, 0, root);
        }

        // find tallest layer, setup layer widths
        int max = Integer.MIN_VALUE;
        int maxId = -1;
        final int[] layerOffsets = new int[layers.size() + 1];

        for (int i = 0; i < layers.size(); i++)
        {
            int height = 0;
            for (final TreeViewNode node : layers.get(i))
            {
                final Pane pane = node.pane;
                height += pane.getHeight();
                height += nodeVerticalDistance;
                if (layerOffsets[i + 1] < pane.getWidth())
                {
                    layerOffsets[i + 1] = pane.getWidth();
                }
            }
            height -= nodeVerticalDistance;

            if (height >= max)
            {
                max = height;
                maxId = i;
            }
        }

        // transform to offsets
        for (int i = 1; i < layerOffsets.length; i++)
        {
            layerOffsets[i] += nodeHorizontalDistance;
            if (i + 1 < layerOffsets.length)
            {
                layerOffsets[i + 1] += layerOffsets[i];
            }
        }

        final BufferBuilder bufferBuilder = new BufferBuilder(512);

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // set up positions and lines of tallest layer
        int curY = 0;
        for (final TreeViewNode node : layers.get(maxId))
        {
            final Pane pane = node.pane;
            pane.setPosition(layerOffsets[maxId], curY);
            curY += pane.getHeight() + nodeVerticalDistance;
        }
        // set up positions and lines before tallest layer
        for (int i = maxId - 1; i >= 0; i--)
        {
            curY = 0;
            Pane postponesPane = null;
            for (final TreeViewNode node : layers.get(i))
            {
                final Pane pane = node.pane;
                // no anchor
                if (node.childs.isEmpty())
                {
                    if (postponesPane != null)
                    {
                        postponesPane.setPosition(layerOffsets[i], curY);
                        curY += postponesPane.getHeight() + nodeVerticalDistance;
                    }
                    postponesPane = pane;
                    continue;
                }

                final int childTop = node.childs.get(0).pane.getY();
                final Pane childBotNode = node.childs.get(node.childs.size() - 1).pane;
                final int childBot = childBotNode.getY() + childBotNode.getHeight();
                int newCurY = (childTop + childBot - pane.getHeight()) / 2;
                newCurY = Math.max(curY, newCurY);

                if (postponesPane != null)
                {
                    int postY = (curY + newCurY - nodeVerticalDistance - postponesPane.getHeight()) / 2;
                    if (postY < curY)
                    {
                        postY = curY;
                        newCurY = curY + postponesPane.getHeight() + nodeVerticalDistance;
                    }
                    postponesPane.setPosition(layerOffsets[i], postY);
                    postponesPane = null;
                }

                // set pos
                pane.setPosition(layerOffsets[i], newCurY);

                int x = pane.getX() + pane.getWidth();
                hLine(bufferBuilder, x, x + nodeToLineDistance + lineSize / 2, newCurY + pane.getHeight() / 2);
                x += nodeToLineDistance + lineSize / 2;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                for (final TreeViewNode child : node.childs)
                {
                    final Pane childPane = child.pane;
                    final int y = childPane.getY() + childPane.getHeight() / 2;
                    hLine(bufferBuilder, x, childPane.getX(), y);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
                vLine(bufferBuilder, x, minY - lineSize / 2, maxY + lineSize / 2);

                curY = newCurY + pane.getHeight() + nodeVerticalDistance;
            }
        }
        // set up positions and lines after tallest layer
        for (int i = maxId; i < layers.size(); i++)
        {
            curY = 0;
            for (final TreeViewNode node : layers.get(i))
            {
                if (node.childs.isEmpty())
                {
                    continue;
                }

                final Pane pane = node.pane;
                int childBranchHeight = 0;
                for (final TreeViewNode child : node.childs)
                {
                    childBranchHeight += child.pane.getHeight() + nodeVerticalDistance;
                }
                childBranchHeight -= nodeVerticalDistance;
                curY = Math.max(curY, pane.getY() + pane.getHeight() / 2 - childBranchHeight / 2);

                int x = pane.getX() + pane.getWidth();
                hLine(bufferBuilder, x, x + nodeToLineDistance + lineSize / 2, pane.getY() + pane.getHeight() / 2);
                x += nodeToLineDistance + lineSize / 2;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                for (final TreeViewNode child : node.childs)
                {
                    final Pane childPane = child.pane;
                    childPane.setPosition(layerOffsets[i + 1], curY);

                    final int y = curY + childPane.getHeight() / 2;
                    hLine(bufferBuilder, x, childPane.getX(), y);

                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    curY += childPane.getHeight() + nodeVerticalDistance;
                }
                vLine(bufferBuilder, x, minY - lineSize / 2, maxY + lineSize / 2);
            }
        }

        // update super
        computeContentSize();

        // complete line buffer
        bufferBuilder.finishDrawing();
        lineBuffer = bufferBuilder.getNextBuffer();
    }

    private void hLine(final BufferBuilder bufferBuilder, final int fromX, final int toX, final int y)
    {
        bufferBuilder.pos(fromX, y - lineSize / 2, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(fromX, y + lineSize / 2, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(toX, y + lineSize / 2, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(toX, y - lineSize / 2, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
    }

    private void vLine(final BufferBuilder bufferBuilder, final int x, final int fromY, final int toY)
    {
        bufferBuilder.pos(x - lineSize / 2, fromY, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(x + lineSize / 2, fromY, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(x + lineSize / 2, toY, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
        bufferBuilder.pos(x - lineSize / 2, toY, 0.0F).color(color[0], color[1], color[2], color[3]).endVertex();
    }

    private void construct(final List<List<TreeViewNode>> layers, final int layer, final TreeViewNode root)
    {
        if (root == null)
        {
            return;
        }

        List<TreeViewNode> paneLayer = layer < layers.size() ? layers.get(layer) : null;
        if (paneLayer == null)
        {
            paneLayer = new ArrayList<>();
            layers.add(paneLayer);
        }

        if (templates.get(root.typeId) == null)
        {
            throw new RuntimeException("Invalid type id for node, type id: " + root.typeId);
        }

        if (root.pane == null)
        {
            final Pane rootPane = Loader.createFromPaneParams(templates.get(root.typeId), this);
            super.treeViewHelperAddChild(rootPane);
            root.pane = rootPane;
            root.paneUpdater.accept(rootPane);
        }
        paneLayer.add(root);

        for (final TreeViewNode child : root.childs)
        {
            construct(layers, layer + 1, child);
        }
    }

    @Override
    public void parseChildren(@NotNull final PaneParams params)
    {
        final List<PaneParams> childNodes = params.getChildren();
        if (childNodes == null)
        {
            return;
        }

        // Get the PaneParams for this child, because we'll need it in the future to create more nodes
        // make their id match array position
        templates = new ArrayList<>();
        for (final PaneParams paneParams : childNodes)
        {
            templates.add(paneParams);
        }
    }

    private void onUpdate(final TreeViewNode root)
    {
        if (root == null)
        {
            return;
        }

        if (root.updatable)
        {
            root.paneUpdater.accept(root.pane);
        }

        for (final TreeViewNode child : root.childs)
        {
            onUpdate(child);
        }
    }

    @Override
    public void onUpdate()
    {
        for (final TreeViewNode root : treeRoots)
        {
            onUpdate(root);
        }

        super.onUpdate();
    }

    @Override
    protected void abstractDrawSelfPost(final int mx, final int my)
    {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0.0f);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        lineBuffer.getSecond().clear();
        if (lineBuffer.getFirst().getVertexCount() > 0)
        {
            lineBuffer.getFirst().getFormat().setupBufferState(MemoryUtil.memAddress(lineBuffer.getSecond()));
            GlStateManager.drawArrays(lineBuffer.getFirst().getDrawMode(), 0, lineBuffer.getFirst().getVertexCount());
            lineBuffer.getFirst().getFormat().clearBufferState();
        }
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    public static class TreeViewNode
    {
        public static final Consumer<Pane> NOOP_PANE_UPDATER = (pane) -> {};
        private final List<TreeViewNode> childs;
        private final Consumer<Pane> paneUpdater;
        private final boolean updatable;
        private final int typeId;
        private Pane pane;

        /**
         * Tree node representing one pane.
         *
         * @param paneUpdater called during onUpdate method and when pane is constructed
         * @param updatable   whether paneUpdater should be called during onUpdate method
         * @param typeId      id of template, each template must have it's numerical id otherwise it's not recognised
         */
        public TreeViewNode(final Consumer<Pane> paneUpdater, final boolean updatable, final int typeId)
        {
            this(paneUpdater, updatable, typeId, new ArrayList<>());
        }

        /**
         * Tree node representing one pane.
         *
         * @param paneUpdater called during onUpdate method and when pane is constructed
         * @param updatable   whether paneUpdater should be ever called (when is mentioned by paneUpdater desc.)
         * @param typeId      id of template, each template must have it's numerical id otherwise it's not recognised
         * @param childs      starting list with childs
         */
        public TreeViewNode(final Consumer<Pane> paneUpdater, final boolean updatable, final int typeId, final List<TreeViewNode> childs)
        {
            this.paneUpdater = paneUpdater;
            this.updatable = updatable;
            this.typeId = typeId;
            this.childs = childs;
        }

        /**
         * @return pane represing node in the tree view
         */
        public Pane getPane()
        {
            return pane;
        }

        /**
         * @return list of childs, used for node swapping/tree building etc.
         */
        public List<TreeViewNode> getChilds()
        {
            return childs;
        }
    }
}
