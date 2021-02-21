package com.ldtteam.structurize.generation;

import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.data.DataGenerator;

public class StructurizeTranslations extends ModLanguageProvider
{
    public StructurizeTranslations(final DataGenerator gen, final String modid, final String locale)
    {
        super(gen, modid, locale);
    }

    @Override
    protected void addTranslations()
    {
        super.addTranslations();

        add(ModBlocks.multiBlock.get(), "Multi-Piston");
        add(ModBlocks.blockDecoBarrel_onside.get(), "Barrel Horizontal");
        add(ModBlocks.blockDecoBarrel_standing.get(), "Barrel Vertical");
        add(ModBlocks.blockSubstitution.get(), "Placeholder Block");
        add(ModBlocks.blockSolidSubstitution.get(), "Solid Placeholder");
        add(ModBlocks.blockFluidSubstitution.get(), "Fluid Placeholder");
        add(ModItems.caliper);
        add(ModItems.buildTool.get(), "Build Tool");
        add(ModItems.scanTool.get(), "Scan Tool");
        add(ModItems.tagTool.get(), "Tag Tool");
        add(ModItems.shapeTool.get(), "Shape Tool");
        add(ModItemGroups.STRUCTURIZE, "Structurize");
        add(ModItemGroups.CONSTRUCTION, "Schematic Construction");
        add(ModItemGroups.SHINGLES, "Structurize Shingles");
        add(ModItemGroups.TIMBER_FRAMES, "Structurize Timber Frames");

        // TODO 1.17 make all keys of uniform convention using various add overloads
		add("com.ldtteam.structurize.gui.buildtool.decorations", "Decorations");
		add("com.ldtteam.structurize.gui.buildtool.hut", "Huts");
		add("com.ldtteam.structurize.gui.buildtool.hut.level", "Level %s");
		add("com.ldtteam.structurize.gui.buildtool.scans", "My Schematics");
		add("com.ldtteam.structurize.gui.placeholder.addtag", "Add Tag");
		add("com.ldtteam.structurize.gui.placeholder.inputtag", "Type a tag name below:");
		add("com.ldtteam.structurize.gui.scan.replace.title", "Block Replacement GUI");
		add("com.ldtteam.structurize.gui.scantool.from", "From");
		add("com.ldtteam.structurize.gui.scantool.name", "Scan Name:");
		add("com.ldtteam.structurize.gui.scantool.remove", "Remove");
		add("com.ldtteam.structurize.gui.scantool.replace", "Replace");
		add("com.ldtteam.structurize.gui.scantool.select", "Select");
		add("com.ldtteam.structurize.gui.scantool.showres", "Show Resources");
		add("com.ldtteam.structurize.gui.scantool.to", "To");
		add("com.ldtteam.structurize.gui.shapetool.hollow", "Hollow");
		add("com.ldtteam.structurize.gui.shapetool.ignore", "Ignore Blocks");
		add("com.ldtteam.structurize.gui.shapetool.pickfillblock", "Pick Fill Block");
		add("com.ldtteam.structurize.gui.shapetool.pickmainblock", "Pick Main Block");
		add("com.ldtteam.structurize.gui.shapetool.replace", "Replace Blocks");
		add("com.ldtteam.structurize.gui.shapetool.solid", "Solid");
		add("com.ldtteam.structurize.gui.structure.edit.title", "Structure path:");
		add("com.ldtteam.structurize.gui.tagtool.addtag", "Added tag %s to %s");
		add("com.ldtteam.structurize.gui.tagtool.anchor.notvalid", "This is not a valid anchor block, only certain tile entities can save tag data (like the Placeholder Block).");
		add("com.ldtteam.structurize.gui.tagtool.anchorsaved", "Saved anchor tile entity and loaded existing tag list. You can now apply tags to blocks!");
		add("com.ldtteam.structurize.gui.tagtool.apply", "Apply Tags");
		add("com.ldtteam.structurize.gui.tagtool.close", "Close GUI");
		add("com.ldtteam.structurize.gui.tagtool.currenttag", "Tag to apply:");
		add("com.ldtteam.structurize.gui.tagtool.discard", "Discard Changes");
		add("com.ldtteam.structurize.gui.tagtool.discard.success", "Reset tags to anchor tags");
		add("com.ldtteam.structurize.gui.tagtool.invalidread", "Tried to read tagPos data from invalid anchor TE");
		add("com.ldtteam.structurize.gui.tagtool.invalidsave", "Tried to save tagPos data to invalid anchor tile entity");
		add("com.ldtteam.structurize.gui.tagtool.noanchor", "Choose a valid anchor block first with shift + right-click.");
		add("com.ldtteam.structurize.gui.tagtool.notag", "Type in a valid tag to apply to a block first!");
		add("com.ldtteam.structurize.gui.tagtool.removed", "Tag %s removed from %s.");
		add("com.ldtteam.structurize.gui.tagtool.save", "Applied tag data successfully to anchor");
		add("com.ldtteam.structurize.network.messages.schematicsavemassage.toobig", "Schematic size is too big, it cannot be bigger than %s bytes!");
		add("com.structurize.command.playernotfound", "Couldn't find player to save the scan!");
		add("com.structurize.command.scan.no.perm", "You don't have permission to scan via commands, use the Scan Tool instead!");
		add("com.structurize.gui.buildtool.leave.tip", "Right-click the build tool on a solid block to adjust the build's position");
		add("com.structurize.gui.buildtool.tip", "Press ESC to leave the GUI to inspect the preview");
		add("item.caliper.message.1d", "line");
		add("item.caliper.message.2d", "square");
		add("item.caliper.message.3d", "cube");
		add("item.caliper.message.base", "That's a %s blocks");
		add("item.caliper.message.by", "by");
		add("item.caliper.message.same", "That's the same block!");
		add("item.possetter.anchorpos", "Anchor pos set to x=%d y=%d z=%d");
		add("item.possetter.firstpos", "First pos set to x=%d y=%d z=%d");
		add("item.possetter.secondpos", "Second pos set to x=%d y=%d z=%d");
		add("item.sceptersteel.badanchorpos", "The Scan Tool's anchor position is outside the scanned area! Please move it using shift + right-click!");
		add("item.sceptersteel.point", "Point one saved: %d %d %d");
		add("item.sceptersteel.point2", "Point two saved: %d %d %d.");
		add("item.sceptersteel.samepoint", "That was the same point!");
		add("item.sceptersteel.scanfailure", "Scan failed to save");
		add("item.sceptersteel.scanformat", "SCAN_%s-%s");
		add("item.sceptersteel.scansuccess", "Scan successfully saved as %s");
		add("item.sceptersteel.toobig", "Schematic too big, max allowed volume is %d blocks");

		add("structurize.command.ls.create.already", "You have already created a session.");
		add("structurize.command.ls.create.done", "Created session for player %s.");
		add("structurize.command.ls.destroy.done", "Destroying session of player %s.");
		add("structurize.command.ls.generic.dontexist", "You don't have a session created.");
		add("structurize.command.ls.invite.accept", "ACCEPT");
		add("structurize.command.ls.invite.accepted", "You have successfully joined %s's session.");
		add("structurize.command.ls.invite.done", "Inviting player \"%s\" to %s's session.");
		add("structurize.command.ls.invite.message", "You have been invited to %s's session, click the button to ");
		add("structurize.command.ls.invite.noopen", "You have no open invite.");
		add("structurize.command.ls.invite.timeout", "This invite does not exist anymore.");
		add("structurize.command.ls.message.head", "%s Session Message %s");
		add("structurize.command.ls.message.muted", "Your messages channel is muted.");
		add("structurize.command.ls.message.norecipient", "You are not part of a session or all other players have their messages channel muted.");
		add("structurize.command.ls.remove.done", "Removing player \"%s\" of %s's session.");
		add("structurize.command.wrong_argument", "Error: Wrong argument!");
		add("structurize.config.allowplayerschematics", "Allow Player Schematics");
		add("structurize.config.allowplayerschematics.comment", "Should player-made schematics be allowed?");
		add("structurize.config.excludeentities", "Entities Excluded from Blueprint Rendering");
		add("structurize.config.excludeentities.comment", "Excludes entities from blueprint rendering if they can't load clientside. If you are going to add a \"minecraft\" entity or you believe it's our fault that an entity couldn't be set up, please make an issue at our GitHub and add the stacktrace to it.");
		add("structurize.config.gameplay", "Gameplay");
		add("structurize.config.gameplay.comment", "All configuration items related to the core gameplay");
		add("structurize.config.ignoreschematicsfromjar", "Ignore Schematics from Jar");
		add("structurize.config.ignoreschematicsfromjar.comment", "Should the default schematics be ignored (from the jar)?");
		add("structurize.config.maxblockschecked", "Max Blocks Checked");
		add("structurize.config.maxblockschecked.comment", "Ma, ModRecipeProvider.getDefaultCriterion(block)x amount of blocks checked by a possible worker");
		add("structurize.config.maxcachedchanges", "Max Cached Changes");
		add("structurize.config.maxcachedchanges.comment", "Max amount of undos saved. A higher number requires more memory.");
		add("structurize.config.maxcachedschematics", "Max Cached Schematics");
		add("structurize.config.maxcachedschematics.comment", "Max amount of schematics to be cached on the server.");
		add("structurize.config.maxoperationspertick", "Max Operations per Tick");
		add("structurize.config.maxoperationspertick.comment", "Max world operations per tick (max blocks to place, remove, or replace).");
		add("structurize.config.windowcachecap", "Max Cached GUI Windows");
		add("structurize.config.windowcachecap.comment", "Sets the maximum number of parsed GUI window files to be stored for quick loading.");
		add("structurize.gui.buildtool.creative_only", "Structurize does not support using the build tool when in survival. Switch to creative or install MineColonies and use the MineColonies Builder.");
		add("structurize.gui.buildtool.unexpecteddatafixer", "Invalid datafixer detected! Side-effects possible! Check log for more info!");
		add("structurize.gui.replaceblock.ambiguous_properties", "Transformation from %s to %s is ambiguous because the following properties are not present in the source block: %s");
    }
}
