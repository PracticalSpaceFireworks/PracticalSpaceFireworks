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

import net.minecraft.block.material.Material;

import javax.annotation.Nonnull;

public class BlockLaser extends BlockMultiblockModule {
    public BlockLaser(@Nonnull String module) {
        super(Material.IRON, module);
    }

    @Override
    protected int getHeight() {
        return 2;
    }
}