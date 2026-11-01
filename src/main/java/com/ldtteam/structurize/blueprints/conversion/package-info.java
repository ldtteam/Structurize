/**
 * AI Based file format converters, code quality does not matter here its just converting to our format. We can yeet this as needed.
 * Converts foreign schematic formats into Structurize blueprint v1 NBT.
 *
 * <p>Each format keeps its parsing and coordinate rules in a dedicated importer:</p>
 * <ul>
 *     <li>{@link com.ldtteam.structurize.blueprints.conversion.VanillaStructureImporter}: vanilla structure templates ({@code .nbt}).</li>
 *     <li>{@link com.ldtteam.structurize.blueprints.conversion.SpongeSchematicImporter}: Sponge schematic versions 1 through 3 ({@code .schem}).</li>
 *     <li>{@link com.ldtteam.structurize.blueprints.conversion.LegacySchematicImporter}: legacy MCEdit/WorldEdit Alpha schematics ({@code .schematic}).</li>
 *     <li>{@link com.ldtteam.structurize.blueprints.conversion.LitematicaSchematicImporter}: Litematica versions 1 through 7 ({@code .litematic}).</li>
 * </ul>
 *
 * <p>{@link com.ldtteam.structurize.blueprints.conversion.StructurePackImporter} dispatches files to their
 * converters for writable pack discovery and the schematic updater. Discovery creates missing sibling
 * {@code .blueprint} files, retains source files and gives existing blueprints precedence. Conversion
 * failures are logged per file so other schematics in the pack can still be imported.</p>
 *
 * <p>Converters operate on NBT without requiring a world or resolving block states against a registry.
 * Block arrays use Y/Z/X order, and serialization is delegated to
 * {@link com.ldtteam.structurize.blueprints.v1.BlueprintUtil}. Structure voids and uncovered positions
 * become light placeholders; explicit air remains air. Minecraft and Structurize are excluded from
 * the inferred required-mod list, matching native blueprint creation.</p>
 *
 * <p>Litematica regions are combined into a single blueprint. Biomes, scheduled ticks and placement
 * settings have no blueprint equivalent and are omitted. Unsupported versions, oversized structures
 * and unresolvable legacy numeric block IDs are rejected.</p>
 */
package com.ldtteam.structurize.blueprints.conversion;
