package com.structurize.structures.blueprints.v1;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class Settings {

	private BlockPos pos;
	
	private Mirror mirror;
	
	private Rotation rotation;
	
	private boolean placeEntities;
	
	private boolean editingMode;
	
	/**
	 * @param pos The positon the Blueprint should be placed at
	 * @param mirror Should the Blueprint be mirrored
	 * @param rotation Should the Blueprint be rotated
	 */
	public Settings(BlockPos pos, Mirror mirror, Rotation rotation) {
		this.pos = pos;
		this.mirror = mirror;
		this.rotation = rotation;
		this.placeEntities = false;
		this.editingMode = false;
	}
	
	/**
	 * @param pos The positon the Blueprint should be placed at
	 * @param mirror Should the Blueprint be mirrored
	 * @param rotation Should the Blueprint be rotated
	 * @param placeEntities should Entities be spawned
	 * @param editingMode If false blocks like "structure void" and "data blocks", if true it will place these entities.
	 */
	public Settings(BlockPos pos, Mirror mirror, Rotation rotation, boolean placeEntities, boolean editingMode) {
		this.pos = pos;
		this.mirror = mirror;
		this.rotation = rotation;
		this.placeEntities = placeEntities;
		this.editingMode = editingMode;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Mirror getMirror() {
		return mirror;
	}

	public Rotation getRotation() {
		return rotation;
	}

	public boolean shouldPlaceEntities() {
		return placeEntities;
	}

	public boolean isEditingMode() {
		return editingMode;
	}
	
	
}
