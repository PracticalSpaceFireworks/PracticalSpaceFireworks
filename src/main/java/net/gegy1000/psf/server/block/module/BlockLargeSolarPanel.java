/*
 * Copyright (C) 2018 InsomniaKitten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.block.PSFSoundType;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockLargeSolarPanel extends BlockMultiblockModule {
    private static final AxisDirectionalBB AABB = new AxisDirectionalBB(0.0, 0.25, 0.0, 1.0, 0.75, 1.0);

    public BlockLargeSolarPanel(String module) {
        super(Material.IRON, module);
        setSoundType(PSFSoundType.SMALL_DEVICE);
    }

    @Override
    @Deprecated
    public MapColor getMapColor(IBlockState state, IBlockAccess access, BlockPos pos) {
        return state.getValue(DIRECTION).getAxis().isHorizontal() ? MapColor.LAPIS : MapColor.AIR;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return AABB.withDirection(state.getValue(DIRECTION));
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        if (state.getValue(DIRECTION).getAxis().isVertical() && Axis.X == side.getAxis()) {
            return BlockFaceShape.MIDDLE_POLE;
        }
        return BlockFaceShape.UNDEFINED;
    }
}
