package com.ldtteam.structures.blueprints.v1;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraft.util.datafix.fixes.BlockStateFlatternEntities;

import java.util.Optional;
import java.util.function.Function;

public class EntityConverter
{
    public CompoundNBT convert(final CompoundNBT nbt)
    {
        final Schema output = DataFixesManager.getDataFixer().getSchema(1451);
        final Schema input = output.getParent();

        Function<Typed<?>, Typed<?>> function = (typed) -> {
            return this.updateBlockToBlockState(typed, "DisplayTile", "DisplayData", "DisplayState");
        };

        //final Type
        /*Typed<?> typed = new Dynamic(NBTDynamicOps.INSTANCE, nbt);
        typed = updateEntity(typed, "minecraft:commandblock_minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:chest_minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:furnace_minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:tnt_minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:hopper_minecart", function, input, output);
        typed = updateEntity(typed, "minecraft:spawner_minecart", function, input, output);
        return typed;*/
        return null;
    }

    private static Typed<?> updateBlockToBlockState(Typed<?> typed, String p_211434_2_, String p_211434_3_, String p_211434_4_)
    {
        Type<Pair<String, Either<Integer, String>>> type = DSL.field(p_211434_2_, DSL.named(TypeReferences.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), DSL.namespacedString())));
        Type<Pair<String, Dynamic<?>>> type1 = DSL.field(p_211434_4_, DSL.named(TypeReferences.BLOCK_STATE.typeName(), DSL.remainderType()));

        Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
        return typed.update(type.finder(), type1, (p_211432_2_) -> {
            int i = p_211432_2_.getSecond().map((p_211435_0_) -> {
                return p_211435_0_;
            }, BlockStateFlatternEntities::getBlockId);
            int j = dynamic.get(p_211434_3_).asInt(0) & 15;
            return Pair.of(TypeReferences.BLOCK_STATE.typeName(), BlockStateFlatteningMap.getFixedNBTForID(i << 4 | j));
        }).set(DSL.remainderFinder(), dynamic.remove(p_211434_3_));
    }

    private static Typed<?> updateEntity(Typed<?> typed, final String id, Function<Typed<?>, Typed<?>> p_211431_3_, Schema input, Schema output)
    {
        Type<?> type = input.getChoiceType(TypeReferences.ENTITY, id);
        Type<?> type1 = output.getChoiceType(TypeReferences.ENTITY, id);
        return typed.updateTyped(DSL.namedChoice(id, type), type1, p_211431_3_);
    }
}
